package com.dxc.gdr.service.implement;

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
import com.dxc.gdr.service.SlaCalculationService;
import com.dxc.gdr.service.interfaces.EmailService;
import com.dxc.gdr.service.interfaces.ReclamationService;
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
    private final SlaCalculationService slaCalculationService;

    public ReclamationServiceImpl(ReclamationRepository reclamationRepository,
                                  UserRepository userRepository,
                                  ReclamationMapper reclamationMapper,
                                  EmailService emailService,
                                  EquipeRepository equipeRepository,
                                  SlaCalculationService slaCalculationService) {
        this.reclamationRepository = reclamationRepository;
        this.userRepository = userRepository;
        this.reclamationMapper = reclamationMapper;
        this.emailService = emailService;
        this.equipeRepository = equipeRepository;
        this.slaCalculationService = slaCalculationService;
    }

    @Override
    public ReclamationResponse createReclamation(CreateReclamationRequest request,
                                                 org.springframework.web.multipart.MultipartFile file,
                                                 String userEmail) {
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
                String originalFilename = file.getOriginalFilename();
                reclamation.setAttachmentName(originalFilename);

                String baseUploadDir = System.getProperty("user.home")
                        + java.io.File.separator + "gdr_uploads"
                        + java.io.File.separator + "ref Reclamation"
                        + java.io.File.separator + reclamation.getNumeroReclamation();

                java.io.File dir = new java.io.File(baseUploadDir);
                if (!dir.exists()) {
                    dir.mkdirs();
                }

                String extension = "";
                if (originalFilename != null && originalFilename.lastIndexOf(".") > 0) {
                    extension = originalFilename.substring(originalFilename.lastIndexOf("."));
                }

                String uniqueFilename = java.util.UUID.randomUUID() + extension;
                String filePath = baseUploadDir + java.io.File.separator + uniqueFilename;

                file.transferTo(new java.io.File(filePath));

                reclamation.setAttachmentPath(filePath);
            } catch (java.io.IOException e) {
                throw new BadRequestException("Erreur de sauvegarde de fichier");
            }
        }

        slaCalculationService.initialiserSla(reclamation);
        slaCalculationService.recalculerSlaStatus(reclamation);

        Reclamation saved = reclamationRepository.save(reclamation);

        try {
            if (client.getEmail() != null && !client.getEmail().isBlank()) {
                emailService.sendReclamationAcknowledgment(
                        client.getEmail(),
                        client.getFirstName(),
                        saved.getNumeroReclamation(),
                        saved.getAttachmentPath(),
                        saved.getAttachmentName()
                );
            }
        } catch (Exception e) {
            System.err.println("ERREUR ENVOI EMAIL : " + e.getMessage());
            e.printStackTrace();
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
                .peek(slaCalculationService::recalculerSlaStatus)
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

        slaCalculationService.recalculerSlaStatus(reclamation);
        return reclamationMapper.toStatusResponse(reclamation);
    }

    @Override
    public long countReclamations() {
        return reclamationRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReclamationResponse> getNouvellesReclamations() {
        return reclamationRepository.findByStatutInOrderByDateCreationDesc(
                        List.of(ReclamationStatus.EN_ATTENTE, ReclamationStatus.REJETEE))
                .stream()
                .peek(slaCalculationService::recalculerSlaStatus)
                .map(reclamationMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ReclamationResponse getReclamationDetails(String numeroReclamation) {
        Reclamation reclamation = reclamationRepository.findByNumeroReclamation(numeroReclamation)
                .orElseThrow(() -> new NotFoundException("Réclamation introuvable"));

        slaCalculationService.recalculerSlaStatus(reclamation);
        return reclamationMapper.toResponse(reclamation);
    }

    @Override
    public ReclamationResponse assignerEquipe(String numeroReclamation, Long idEquipe) {
        Reclamation reclamation = reclamationRepository.findByNumeroReclamation(numeroReclamation)
                .orElseThrow(() -> new NotFoundException("Réclamation introuvable"));
        Equipe equipe = equipeRepository.findById(idEquipe)
                .orElseThrow(() -> new NotFoundException("Équipe introuvable"));

        reclamation.setEquipeAssignee(equipe);
        reclamation.setMotifRefus(null);
        reclamation.setDateMiseAJour(LocalDateTime.now());
        reclamation.setStatut(ReclamationStatus.EN_ATTENTE);

        slaCalculationService.recalculerSlaStatus(reclamation);

        Reclamation saved = reclamationRepository.save(reclamation);

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
                .peek(slaCalculationService::recalculerSlaStatus)
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

        User chef = userRepository.findByEmail(chefEmail)
                .orElseThrow(() -> new NotFoundException("Chef d'équipe introuvable"));

        if (!reclamation.getEquipeAssignee().getChefEquipe().getId().equals(chef.getId())) {
            throw new UnauthorizedException("Seul le chef de l'équipe assignée peut rejeter cette réclamation");
        }

        reclamation.setStatut(ReclamationStatus.REJETEE);
        reclamation.setMotifRefus(motif);
        reclamation.setEquipeAssignee(null);
        reclamation.setDateMiseAJour(LocalDateTime.now());
        reclamation.setDateResolution(LocalDateTime.now());

        slaCalculationService.recalculerSlaStatus(reclamation);

        return reclamationMapper.toResponse(reclamationRepository.save(reclamation));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReclamationResponse> getReclamationsParEquipe(Long equipeId) {
        return reclamationRepository.findAllByTeamId(equipeId)
                .stream()
                .peek(slaCalculationService::recalculerSlaStatus)
                .map(reclamationMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReclamationResponse> getMissionsAgent(String agentEmail) {
        User user = userRepository.findByEmail(agentEmail)
                .orElseThrow(() -> new NotFoundException("Utilisateur introuvable"));

        Equipe equipe = equipeRepository.findByChefEquipeId(user.getId()).orElse(null);

        if (equipe == null) {
            equipe = user.getEquipe();
        }

        if (equipe == null) {
            return List.of();
        }

        return reclamationRepository.findAllByTeamId(equipe.getId())
                .stream()
                .peek(slaCalculationService::recalculerSlaStatus)
                .map(reclamationMapper::toResponse)
                .toList();
    }

    @Override
    public ReclamationResponse accepterReclamation(String numeroReclamation, String userEmail) {

        Reclamation reclamation = reclamationRepository.findByNumeroReclamation(numeroReclamation)
                .orElseThrow(() -> new NotFoundException("Réclamation introuvable"));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new NotFoundException("Utilisateur introuvable"));

        Equipe equipe = equipeRepository.findByChefEquipeId(user.getId()).orElse(null);

        if (equipe == null) {
            equipe = user.getEquipe();
        }

        if (equipe == null) {
            throw new UnauthorizedException("Utilisateur sans équipe");
        }

        if (reclamation.getEquipeAssignee() == null) {
            throw new BadRequestException("Réclamation non assignée");
        }

        if (!reclamation.getEquipeAssignee().getId().equals(equipe.getId())) {
            throw new UnauthorizedException("Réclamation non liée à votre équipe");
        }

        reclamation.setStatut(ReclamationStatus.EN_COURS);
        reclamation.setDateMiseAJour(LocalDateTime.now());

        slaCalculationService.recalculerSlaStatus(reclamation);

        return reclamationMapper.toResponse(reclamationRepository.save(reclamation));
    }

    @Override
    public ReclamationResponse marquerResolue(String numeroReclamation, String userEmail) {
        Reclamation reclamation = reclamationRepository.findByNumeroReclamation(numeroReclamation)
                .orElseThrow(() -> new NotFoundException("Réclamation introuvable"));

        User agent = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new NotFoundException("Agent introuvable"));

        if (agent.getEquipe() == null) {
            throw new UnauthorizedException("L'agent n'appartient à aucune équipe");
        }

        if (reclamation.getEquipeAssignee() == null) {
            throw new BadRequestException("Cette réclamation n'est assignée à aucune équipe");
        }

        if (!reclamation.getEquipeAssignee().getId().equals(agent.getEquipe().getId())) {
            throw new UnauthorizedException("Cette réclamation n'est pas assignée à l'équipe de l'agent");
        }

        reclamation.setStatut(ReclamationStatus.TRAITEE);
        reclamation.setDateMiseAJour(LocalDateTime.now());
        reclamation.setDateResolution(LocalDateTime.now());

        slaCalculationService.recalculerSlaStatus(reclamation);

        return reclamationMapper.toResponse(reclamationRepository.save(reclamation));
    }
}