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
    private final com.dxc.gdr.dao.ReclamationRepository reclamationRepository;

    public EquipeServiceImpl(EquipeRepository equipeRepository, 
                             UserRepository userRepository,
                             com.dxc.gdr.dao.ReclamationRepository reclamationRepository) {
        this.equipeRepository = equipeRepository;
        this.userRepository = userRepository;
        this.reclamationRepository = reclamationRepository;
    }

    // ─── ADMIN : Créer une équipe et lui associer un chef ─────────────────────

    @Override
    public EquipeResponse creerEquipe(CreateEquipeRequest request) {
        User chef = userRepository.findByEmail(request.getChefEmail())
                .orElseThrow(() -> new NotFoundException("Utilisateur avec l'email [" + request.getChefEmail() + "] introuvable"));

        equipeRepository.findByChefEquipeId(chef.getId()).ifPresent(e -> {
            throw new BadRequestException("Cet utilisateur est déjà chef de l'équipe : " + e.getNom());
        });

        // NOUVEAU : Vérifier que le chef n'est pas un agent dans une équipe
        if (chef.getEquipe() != null) {
            throw new BadRequestException("Cet utilisateur est déjà un agent dans l'équipe : " + chef.getEquipe().getNom() + ". Retirez-le d'abord de son équipe actuelle.");
        }

        Equipe equipe = new Equipe(request.getNom().trim(), chef);
        equipe = equipeRepository.save(equipe);

        if (request.getAgentIds() != null && !request.getAgentIds().isEmpty()) {
            List<User> agentsToAssign = userRepository.findAllById(request.getAgentIds());
            for (User agent : agentsToAssign) {
                // Vérifier que l'agent n'est pas déjà chef d'une équipe
                final Long agentId = agent.getId();
                equipeRepository.findByChefEquipeId(agentId).ifPresent(e -> {
                    throw new BadRequestException("L'utilisateur " + agent.getEmail() + " est déjà chef de l'équipe : " + e.getNom());
                });

                if (agent.getEquipe() == null) {
                    agent.setEquipe(equipe);
                    if (!equipe.getAgents().contains(agent)) {
                        equipe.getAgents().add(agent);
                    }
                    userRepository.save(agent);
                }
            }
        }

        return toResponse(equipe);
    }

    // ─── ADMIN : Lister toutes les équipes ────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<EquipeResponse> listerEquipes(org.springframework.data.domain.Pageable pageable) {
        return equipeRepository.findAll(pageable)
                .map(this::toResponse);
    }

    // ─── ADMIN : Modifier le nom et le chef d'une équipe ─────────────────────

    @Override
    public EquipeResponse modifierEquipeAdmin(Long id, com.dxc.gdr.Dto.request.UpdateEquipeAdminRequest request) {
        Equipe equipe = equipeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Equipe introuvable"));

        User nouveauChef = userRepository.findByEmail(request.getChefEmail())
                .orElseThrow(() -> new NotFoundException("Utilisateur avec l'email [" + request.getChefEmail() + "] introuvable"));

        if (equipe.getChefEquipe() == null || !nouveauChef.getId().equals(equipe.getChefEquipe().getId())) {
            equipeRepository.findByChefEquipeId(nouveauChef.getId()).ifPresent(e -> {
                throw new BadRequestException("Cet utilisateur est déjà chef de l'équipe : " + e.getNom());
            });

            // NOUVEAU : Vérifier que le nouveau chef n'est pas un agent
            if (nouveauChef.getEquipe() != null) {
                throw new BadRequestException("Cet utilisateur est déjà un agent dans l'équipe : " + nouveauChef.getEquipe().getNom() + ". Retirez-le d'abord de son équipe actuelle.");
            }

            equipe.setChefEquipe(nouveauChef);
        }

        equipe.setNom(request.getNom().trim());
        return toResponse(equipeRepository.save(equipe));
    }

    // ─── ADMIN : Ajouter un agent à une équipe ────────────────────────────────

    @Override
    public EquipeResponse ajouterAgent(Long equipeId, Long agentId) {
        Equipe equipe = equipeRepository.findById(equipeId)
                .orElseThrow(() -> new NotFoundException("Equipe introuvable"));

        User agent = userRepository.findByIdAndDeletedFalse(agentId)
                .orElseThrow(() -> new NotFoundException("Agent " + agentId + " introuvable"));

        if (agent.getEquipe() != null && !agent.getEquipe().getId().equals(equipeId)) {
            throw new BadRequestException("Cet agent appartient déjà à l'équipe : " + agent.getEquipe().getNom());
        }

        // NOUVEAU : Vérifier que l'agent n'est pas chef d'une équipe
        equipeRepository.findByChefEquipeId(agentId).ifPresent(e -> {
            throw new BadRequestException("Cet utilisateur est déjà chef de l'équipe : " + e.getNom() + ". Il ne peut pas être ajouté comme agent.");
        });

        agent.setEquipe(equipe);
        if (!equipe.getAgents().contains(agent)) {
            equipe.getAgents().add(agent);
        }

        userRepository.saveAndFlush(agent);
        return toResponse(equipe);
    }

    // ─── ADMIN : Retirer un agent d'une équipe ────────────────────────────────

    @Override
    public EquipeResponse retirerAgent(Long equipeId, Long agentId) {
        Equipe equipe = equipeRepository.findById(equipeId)
                .orElseThrow(() -> new NotFoundException("Equipe introuvable"));

        User agent = userRepository.findByIdAndDeletedFalse(agentId)
                .orElseThrow(() -> new NotFoundException("Agent " + agentId + " introuvable"));

        if (agent.getEquipe() == null || !agent.getEquipe().getId().equals(equipeId)) {
            throw new BadRequestException("Cet agent n'appartient pas à cette équipe");
        }

        agent.setEquipe(null);
        equipe.getAgents().removeIf(a -> a.getId().equals(agentId));

        userRepository.saveAndFlush(agent);
        return toResponse(equipe);
    }

    // ─── CHEF_EQUIPE : Récupérer son équipe ───────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public EquipeResponse getMonEquipe(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new NotFoundException("Utilisateur introuvable"));

        Equipe equipe = equipeRepository.findByChefEquipeId(user.getId()).orElse(null);

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

    // ─── Agents libres (sans équipe) ──────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<EquipeResponse.AgentResponse> listerAgentsLibres(org.springframework.data.domain.Pageable pageable) {
        return userRepository.findByRoleAndEquipeIsNull("AGENT", pageable)
                .map(this::toAgentResponse);
    }

    @Override
    public void supprimerEquipe(Long id, Long targetTeamId) {
        Equipe equipe = equipeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Equipe introuvable"));

        // Récupérer les réclamations assignées
        List<com.dxc.gdr.model.Reclamation> reclamations = reclamationRepository.findByEquipeAssigneeId(id);

        if (!reclamations.isEmpty()) {
            if (targetTeamId == null) {
                throw new BadRequestException("Impossible de supprimer cette équipe : elle possède encore " + reclamations.size() + " réclamation(s) assignée(s). Veuillez les réassigner d'abord.");
            }

            // Réassigner les réclamations à l'équipe cible
            Equipe targetTeam = equipeRepository.findById(targetTeamId)
                    .orElseThrow(() -> new NotFoundException("Équipe cible introuvable"));

            for (com.dxc.gdr.model.Reclamation reclamation : reclamations) {
                reclamation.setEquipeAssignee(targetTeam);
                reclamationRepository.save(reclamation);
            }
        }

        // Délier les agents
        List<User> agents = userRepository.findByEquipeId(id);
        for (User agent : agents) {
            agent.setEquipe(null);
            userRepository.save(agent);
        }

        equipeRepository.delete(equipe);
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
            response.setChefEquipeEmail(chef.getEmail());
        }

        List<EquipeResponse.AgentResponse> agents = equipe.getAgents()
                .stream()
                .map(this::toAgentResponse)
                .collect(Collectors.toList());

        response.setAgents(agents);
        response.setNombreAgents(agents.size());

        // Compter les réclamations
        List<com.dxc.gdr.model.Reclamation> recs = reclamationRepository.findByEquipeAssigneeId(equipe.getId());
        response.setNombreReclamations(recs.size());

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