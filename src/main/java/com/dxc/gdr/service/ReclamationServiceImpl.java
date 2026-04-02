package com.dxc.gdr.service;

import com.dxc.gdr.Dto.request.CreateReclamationRequest;
import com.dxc.gdr.Dto.response.ReclamationResponse;
import com.dxc.gdr.Dto.response.ReclamationStatusResponse;
import com.dxc.gdr.common.exception.BadRequestException;
import com.dxc.gdr.common.exception.NotFoundException;
import com.dxc.gdr.common.exception.UnauthorizedException;
import com.dxc.gdr.dao.EquipeRepository;
import com.dxc.gdr.dao.ReclamationRepository;
import com.dxc.gdr.dao.UserRepository;
import com.dxc.gdr.mapper.ReclamationMapper;
import com.dxc.gdr.model.Equipe;
import com.dxc.gdr.model.Reclamation;
import com.dxc.gdr.model.ReclamationCategory;
import com.dxc.gdr.model.ReclamationPriority;
import com.dxc.gdr.model.ReclamationStatus;
import com.dxc.gdr.model.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class ReclamationServiceImpl implements ReclamationService {

    private final ReclamationRepository reclamationRepository;
    private final UserRepository userRepository;
    private final ReclamationMapper reclamationMapper;
    private final EmailService emailService;
    private final EquipeRepository equipeRepository;

    public ReclamationServiceImpl(ReclamationRepository reclamationRepository,
                                  UserRepository userRepository,
                                  ReclamationMapper reclamationMapper,
                                  EmailService emailService,
                                  EquipeRepository equipeRepository) {
        this.reclamationRepository = reclamationRepository;
        this.userRepository = userRepository;
        this.reclamationMapper = reclamationMapper;
        this.emailService = emailService;
        this.equipeRepository = equipeRepository;
    }

    @Override
    public ReclamationResponse createReclamation(CreateReclamationRequest request, org.springframework.web.multipart.MultipartFile file, String userEmail) {

        User client = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new NotFoundException("Utilisateur introuvable"));

        Reclamation reclamation = new Reclamation();

        reclamation.setNumeroReclamation(generateNumeroReclamation());
        reclamation.setTitre(request.getTitre().trim());
        reclamation.setDescription(request.getDescription().trim());
        reclamation.setCategorie(parseCategorie(request.getCategorie()));
        reclamation.setPriorite(parsePriorite(request.getPriorite()));
        reclamation.setStatut(ReclamationStatus.EN_ATTENTE);
        reclamation.setDateCreation(LocalDateTime.now());
        reclamation.setDateMiseAJour(LocalDateTime.now());
        reclamation.setClient(client);

        if (ReclamationCategory.MAINTENANCE.equals(reclamation.getCategorie())) {
            reclamation.setTypeMaintenance(request.getTypeMaintenance());
            if ("INCIDENT".equals(request.getTypeMaintenance())) {
                reclamation.setSousCategorieIncident(request.getSousCategorieIncident());
                if ("AUTRE".equals(request.getSousCategorieIncident())) {
                    reclamation.setDetailsAutreIncident(request.getDetailsAutreIncident());
                }
            }
        }

        if (file != null && !file.isEmpty()) {
            try {
                reclamation.setAttachmentName(file.getOriginalFilename());
                reclamation.setAttachmentData(file.getBytes());
            } catch (java.io.IOException e) {
                throw new BadRequestException("Erreur de sauvegarde de fichier");
            }
        }

        Reclamation saved = reclamationRepository.save(reclamation);

        try {
            emailService.sendReclamationAcknowledgment(
                    client.getEmail(),
                    client.getFirstName(),
                    saved.getNumeroReclamation(),
                    saved.getAttachmentData(),
                    saved.getAttachmentName()
            );
        } catch (Exception e) {
            System.out.println("ERREUR ENVOI EMAIL : " + e.getMessage());
        }

        return reclamationMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReclamationResponse> getMyReclamations(String userEmail) {
        User client = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new NotFoundException("Utilisateur introuvable"));

        return reclamationRepository.findByClientIdOrderByDateCreationDesc(client.getId())
                .stream()
                .map(reclamationMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ReclamationStatusResponse getReclamationStatus(String numeroReclamation, String userEmail) {
        Reclamation reclamation = reclamationRepository.findByNumeroReclamation(numeroReclamation)
                .orElseThrow(() -> new NotFoundException("Réclamation introuvable"));

        if (!reclamation.getClient().getEmail().equals(userEmail)) {
            throw new UnauthorizedException("Vous n'êtes pas autorisé à consulter cette réclamation");
        }

        return reclamationMapper.toStatusResponse(reclamation);
    }

    @Override
    public long countReclamations() {
        return reclamationRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReclamationResponse> getNouvellesReclamations() {
        return reclamationRepository.findByStatutInOrderByDateCreationDesc(List.of(ReclamationStatus.EN_ATTENTE, ReclamationStatus.REJETEE))
                .stream()
                .map(reclamationMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ReclamationResponse getReclamationDetails(String numeroReclamation) {
        Reclamation reclamation = reclamationRepository.findByNumeroReclamation(numeroReclamation)
                .orElseThrow(() -> new NotFoundException("Réclamation introuvable"));

        return reclamationMapper.toResponse(reclamation);
    }

    @Override
    public ReclamationResponse assignerEquipe(String numeroReclamation, Long idEquipe) {
        Reclamation reclamation = reclamationRepository.findByNumeroReclamation(numeroReclamation)
                .orElseThrow(() -> new NotFoundException("Réclamation introuvable"));

        Equipe equipe = equipeRepository.findById(idEquipe)
                .orElseThrow(() -> new NotFoundException("Équipe introuvable"));

        reclamation.setEquipeAssignee(equipe);
        reclamation.setMotifRefus(null); // On efface le motif de refus précédent
        reclamation.setDateMiseAJour(LocalDateTime.now());
        reclamation.setStatut(ReclamationStatus.EN_COURS);

        Reclamation saved = reclamationRepository.save(reclamation);


        // Notification du Chef d'équipe si présent
        if (equipe.getChefEquipe() != null) {
            emailService.sendAssignmentNotification(
                    equipe.getChefEquipe().getEmail(),
                    equipe.getNom(),
                    saved.getNumeroReclamation(),
                    saved.getTitre()
            );
        }

        return reclamationMapper.toResponse(saved);
    }


    private String generateNumeroReclamation() {
        String numero;
        do {
            long count = reclamationRepository.count() + 1;
            int year = LocalDate.now().getYear();
            numero = String.format("REC-%d-%04d", year, count);
        } while (reclamationRepository.existsByNumeroReclamation(numero));

        return numero;
    }

    private ReclamationCategory parseCategorie(String categorie) {
        try {
            return ReclamationCategory.valueOf(categorie.trim().toUpperCase());
        } catch (Exception e) {
            throw new BadRequestException("Catégorie invalide");
        }
    }

    private ReclamationPriority parsePriorite(String priorite) {
        try {
            return ReclamationPriority.valueOf(priorite.trim().toUpperCase());
        } catch (Exception e) {
            throw new BadRequestException("Priorité invalide");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReclamationResponse> getAllReclamations() {
        return reclamationRepository.findAll()
                .stream()
                .map(reclamationMapper::toResponse)
                .toList();
    }

    @Override
    public ReclamationResponse rejeterReclamation(String numeroReclamation, String motif, String chefEmail) {
        Reclamation reclamation = reclamationRepository.findByNumeroReclamation(numeroReclamation)
                .orElseThrow(() -> new NotFoundException("Réclamation introuvable"));

        if (reclamation.getEquipeAssignee() == null) {
            throw new BadRequestException("Cette réclamation n'est assignée à aucune équipe");
        }

        // Vérifier que c'est bien le chef de l'équipe assignée qui rejette
        User chef = userRepository.findByEmail(chefEmail)
                .orElseThrow(() -> new NotFoundException("Chef d'équipe introuvable"));

        if (!reclamation.getEquipeAssignee().getChefEquipe().getId().equals(chef.getId())) {
            throw new UnauthorizedException("Seul le chef de l'équipe assignée peut rejeter cette réclamation");
        }

        reclamation.setStatut(ReclamationStatus.REJETEE);
        reclamation.setMotifRefus(motif);
        reclamation.setEquipeAssignee(null); // Retombe dans la liste "Nouvelles" pour le Manager
        reclamation.setDateMiseAJour(LocalDateTime.now());

        Reclamation saved = reclamationRepository.save(reclamation);
        return reclamationMapper.toResponse(saved);
    }
}


