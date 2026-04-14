package com.dxc.gdr.service.implement;

import com.dxc.gdr.Dto.request.CreateEquipeRequest;
import com.dxc.gdr.Dto.request.UpdateEquipeRequest;
import com.dxc.gdr.Dto.response.EquipeResponse;
import com.dxc.gdr.common.exception.BadRequestException;
import com.dxc.gdr.common.exception.NotFoundException;
import com.dxc.gdr.dao.EquipeRepository;
import com.dxc.gdr.dao.UserRepository;
import com.dxc.gdr.model.Equipe;
import com.dxc.gdr.model.User;
import com.dxc.gdr.service.interfaces.EquipeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class EquipeServiceImpl implements EquipeService {

    private final EquipeRepository equipeRepository;
    private final UserRepository userRepository;

    public EquipeServiceImpl(EquipeRepository equipeRepository, UserRepository userRepository) {
        this.equipeRepository = equipeRepository;
        this.userRepository = userRepository;
    }

    // ─── ADMIN : Créer une équipe et lui associer un chef ─────────────────────

    @Override
    public EquipeResponse creerEquipe(CreateEquipeRequest request) {
        User chef = userRepository.findByEmail(request.getChefEmail())
                .orElseThrow(() -> new NotFoundException("Utilisateur avec l'email [" + request.getChefEmail() + "] introuvable"));

        // Un utilisateur ne peut être chef que d'une seule équipe
        equipeRepository.findByChefEquipeId(chef.getId()).ifPresent(e -> {
            throw new BadRequestException("Cet utilisateur est déjà chef de l'équipe : " + e.getNom());
        });

        Equipe equipe = new Equipe(request.getNom().trim(), chef);
        return toResponse(equipeRepository.save(equipe));
    }

    // ─── ADMIN : Lister toutes les équipes ────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<EquipeResponse> listerEquipes() {
        return equipeRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ─── ADMIN : Modifier une équipe ──────────────────────────────────────────

    @Override
    public EquipeResponse modifierEquipeAdmin(Long id, com.dxc.gdr.Dto.request.UpdateEquipeAdminRequest request) {
        Equipe equipe = equipeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Equipe introuvable"));

        User nouveauChef = userRepository.findByEmail(request.getChefEmail())
                .orElseThrow(() -> new NotFoundException("Utilisateur avec l'email [" + request.getChefEmail() + "] introuvable"));

        // Un utilisateur ne peut être chef que d'une seule équipe
        if (equipe.getChefEquipe() == null || !nouveauChef.getId().equals(equipe.getChefEquipe().getId())) {
            equipeRepository.findByChefEquipeId(nouveauChef.getId()).ifPresent(e -> {
                throw new BadRequestException("Cet utilisateur est déjà chef de l'équipe : " + e.getNom());
            });
            equipe.setChefEquipe(nouveauChef);
        }

        equipe.setNom(request.getNom().trim());

        return toResponse(equipeRepository.save(equipe));
    }


    // ─── CHEF_EQUIPE : Récupérer son équipe ───────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public EquipeResponse getMonEquipe(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new NotFoundException("Utilisateur introuvable"));

        // First check if the user is a Chef
        Equipe equipe = equipeRepository.findByChefEquipeId(user.getId()).orElse(null);
        
        // If not a chef, check if they are a member (Agent)
        if (equipe == null) {
            equipe = user.getEquipe();
        }

        if (equipe == null) {
            throw new NotFoundException("Aucune équipe ne vous est assignée");
        }

        return toResponse(equipe);
    }

    // ─── CHEF_EQUIPE : Mettre à jour le nom du service ────────────────────────

    @Override
    public EquipeResponse mettreAJourNom(String chefEmail, UpdateEquipeRequest request) {
        Equipe equipe = getEquipeByChefEmail(chefEmail);
        equipe.setNom(request.getNom().trim());
        return toResponse(equipeRepository.save(equipe));
    }

    // ─── CHEF_EQUIPE : Ajouter un agent libre ─────────────────────────────────

    @Override
    public EquipeResponse ajouterAgent(String chefEmail, Long agentId) {
        Equipe equipe = getEquipeByChefEmail(chefEmail);
        User agent = userRepository.findByIdAndDeletedFalse(agentId)
                .orElseThrow(() -> new NotFoundException("Agent " + agentId + " introuvable"));

        // Synchronisation manuelle des deux côtés pour mise à jour immédiate du DTO
        agent.setEquipe(equipe);
        if (!equipe.getAgents().contains(agent)) {
            equipe.getAgents().add(agent);
        }
        
        userRepository.saveAndFlush(agent);

        return toResponse(equipe);
    }

    @Override
    public EquipeResponse retirerAgent(String chefEmail, Long agentId) {
        Equipe equipe = getEquipeByChefEmail(chefEmail);
        User agent = userRepository.findByIdAndDeletedFalse(agentId)
                .orElseThrow(() -> new NotFoundException("Agent " + agentId + " introuvable"));

        // Libération de l'agent et nettoyage de la liste équipe en mémoire
        agent.setEquipe(null);
        equipe.getAgents().removeIf(a -> a.getId().equals(agentId));
        
        userRepository.saveAndFlush(agent);

        return toResponse(equipe);
    }

    // ─── Agents libres (sans équipe) ──────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<EquipeResponse.AgentResponse> listerAgentsLibres() {
        return userRepository.findByRoleAndEquipeIsNull("ROLE_AGENT")
                .stream()
                .map(this::toAgentResponse)
                .collect(java.util.stream.Collectors.toList());
    }

    // ─── Helpers privés ───────────────────────────────────────────────────────

    private Equipe getEquipeByChefEmail(String email) {
        User chef = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Utilisateur introuvable"));

        return equipeRepository.findByChefEquipeId(chef.getId())
                .orElseThrow(() -> new NotFoundException("Aucune équipe ne vous est assignée"));
    }

    private EquipeResponse toResponse(Equipe equipe) {
        EquipeResponse response = new EquipeResponse();
        response.setId(equipe.getId());
        response.setNom(equipe.getNom());

        if (equipe.getChefEquipe() != null) {
            User chef = equipe.getChefEquipe();
            response.setChefEquipeId(chef.getId());
            response.setChefEquipeNom(chef.getFirstName() + " " + chef.getLastName());
        }

        List<EquipeResponse.AgentResponse> agents = equipe.getAgents()
                .stream()
                .map(this::toAgentResponse)
                .collect(Collectors.toList());

        response.setAgents(agents);
        response.setNombreAgents(agents.size());
        return response;
    }

    private EquipeResponse.AgentResponse toAgentResponse(User user) {
        EquipeResponse.AgentResponse ar = new EquipeResponse.AgentResponse();
        ar.setId(user.getId());
        ar.setPrenom(user.getFirstName());
        ar.setNom(user.getLastName());
        ar.setEmail(user.getEmail());
        return ar;
    }
}
