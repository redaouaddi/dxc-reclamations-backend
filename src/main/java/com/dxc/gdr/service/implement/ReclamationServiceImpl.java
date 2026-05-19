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
import com.dxc.gdr.model.*;
import com.dxc.gdr.service.SlaCalculationService;
import com.dxc.gdr.service.interfaces.EmailService;
import com.dxc.gdr.service.interfaces.ReclamationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    public Page<ReclamationResponse> getMyReclamations(String userEmail, Pageable pageable) {
        User client = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new NotFoundException("Utilisateur introuvable"));

        return reclamationRepository.findByClientIdOrderByDateCreationDesc(client.getId(), pageable)
                .map(r -> {
                    slaCalculationService.recalculerSlaStatus(r);
                    return reclamationMapper.toResponse(r);
                });
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
    public Page<ReclamationResponse> getNouvellesReclamations(Pageable pageable) {
        return reclamationRepository.findByStatutInOrderByDateCreationDesc(
                        List.of(ReclamationStatus.EN_ATTENTE, ReclamationStatus.REJETEE), pageable)
                .map(r -> {
                    slaCalculationService.recalculerSlaStatus(r);
                    return reclamationMapper.toResponse(r);
                });
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
        reclamation.setAgentAssigne(null);
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

    @Override
    @Transactional(readOnly = true)
    public Page<ReclamationResponse> getAllReclamations(ReclamationStatus statut, Pageable pageable) {
        Page<Reclamation> page;
        if (statut != null) {
            page = reclamationRepository.findByStatut(statut, pageable);
        } else {
            page = reclamationRepository.findAll(pageable);
        }

        return page.map(r -> {
            slaCalculationService.recalculerSlaStatus(r);
            return reclamationMapper.toResponse(r);
        });
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
        reclamation.setAgentAssigne(null);
        reclamation.setEquipeAssignee(null);
        reclamation.setDateMiseAJour(LocalDateTime.now());
        reclamation.setDateResolution(LocalDateTime.now());

        slaCalculationService.recalculerSlaStatus(reclamation);

        return reclamationMapper.toResponse(reclamationRepository.save(reclamation));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReclamationResponse> getReclamationsParEquipe(Long equipeId, Pageable pageable) {
        return reclamationRepository.findAllByTeamId(equipeId, pageable)
                .map(r -> {
                    slaCalculationService.recalculerSlaStatus(r);
                    return reclamationMapper.toResponse(r);
                });
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReclamationResponse> getMissionsAgent(String agentEmail, Pageable pageable) {
        User user = userRepository.findByEmail(agentEmail)
                .orElseThrow(() -> new NotFoundException("Utilisateur introuvable"));

        Equipe equipe = equipeRepository.findByChefEquipeId(user.getId()).orElse(null);

        if (equipe == null) {
            equipe = user.getEquipe();
        }

        if (equipe == null) {
            return org.springframework.data.domain.Page.empty();
        }

        // Si l'utilisateur est le chef de cette équipe, il voit tout (pour pouvoir confirmer)
        if (equipe.getChefEquipe() != null && equipe.getChefEquipe().getId().equals(user.getId())) {
            return reclamationRepository.findAllByTeamId(equipe.getId(), pageable)
                    .map(r -> {
                        slaCalculationService.recalculerSlaStatus(r);
                        return reclamationMapper.toResponse(r);
                    });
        }

        // Sinon (Agent simple), il ne voit que les réclamations confirmées ou déjà traitées ou réouvertes
        return reclamationRepository.findByEquipeAssigneeIdAndStatutIn(
                equipe.getId(),
                List.of(ReclamationStatus.EN_COURS, ReclamationStatus.TRAITEE, ReclamationStatus.REOUVERTE),
                pageable)
                .map(r -> {
                    slaCalculationService.recalculerSlaStatus(r);
                    return reclamationMapper.toResponse(r);
                });
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
    public ReclamationResponse marquerResolue(String numeroReclamation, String userEmail, String cause, String action, String solution) {
        Reclamation reclamation = reclamationRepository.findByNumeroReclamation(numeroReclamation)
                .orElseThrow(() -> new NotFoundException("Réclamation introuvable"));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new NotFoundException("Utilisateur introuvable"));

        // Vérifier si l'utilisateur est ADMIN
        boolean isAdmin = user.getRoles().stream()
                .anyMatch(r -> "ROLE_ADMIN".equals(r.getName()) || "ADMIN".equals(r.getName()));

        if (!isAdmin) {
            Equipe equipeMembre = user.getEquipe();
            Equipe equipeGeree = equipeRepository.findByChefEquipeId(user.getId()).orElse(null);
            
            if (reclamation.getEquipeAssignee() == null) {
                throw new BadRequestException("Cette réclamation n'est assignée à aucune équipe");
            }

            Long targetTeamId = reclamation.getEquipeAssignee().getId();
            boolean authorized = false;

            if (equipeMembre != null && equipeMembre.getId().equals(targetTeamId)) {
                authorized = true;
            }
            if (!authorized && equipeGeree != null && equipeGeree.getId().equals(targetTeamId)) {
                authorized = true;
            }

            if (!authorized) {
                throw new UnauthorizedException("Cette réclamation n'est pas assignée à une équipe que vous dirigez ou dont vous faites partie");
            }
        }

        // Vérification du statut : une réclamation doit être confirmée (EN_COURS) avant d'être résolue
        if (reclamation.getStatut() == ReclamationStatus.EN_ATTENTE) {
            throw new BadRequestException("Cette réclamation doit d'abord être confirmée par le chef d'équipe avant d'être résolue.");
        }

        // On garde l’utilisateur actuel comme agent responsable
        reclamation.setAgentAssigne(user);

        reclamation.setStatut(ReclamationStatus.TRAITEE);
        reclamation.setDateMiseAJour(LocalDateTime.now());
        reclamation.setDateResolution(LocalDateTime.now());
        
        reclamation.setCauseIdentifiee(cause);
        reclamation.setActionRealisee(action);
        reclamation.setSolutionProposee(solution);

        slaCalculationService.recalculerSlaStatus(reclamation);

        return reclamationMapper.toResponse(reclamationRepository.save(reclamation));
    }

    @Override
    public ReclamationResponse reouvrirReclamation(String numeroReclamation, String motif, org.springframework.web.multipart.MultipartFile file, String userEmail) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Une pièce jointe est obligatoire pour réouvrir la réclamation.");
        }

        Reclamation reclamation = reclamationRepository.findByNumeroReclamation(numeroReclamation)
                .orElseThrow(() -> new NotFoundException("Réclamation introuvable"));

        // Vérifier que c'est bien le client propriétaire qui réouvre
        if (!reclamation.getClient().getEmail().equals(userEmail)) {
            throw new UnauthorizedException("Seul le client ayant créé la réclamation peut la réouvrir");
        }

        // Vérifier le statut (on ne peut réouvrir qu'une réclamation TRAITEE)
        if (reclamation.getStatut() != ReclamationStatus.TRAITEE) {
            throw new BadRequestException("Seules les réclamations traitées peuvent être réouvertes");
        }

        // Sauvegarde de la pièce jointe de réouverture
        try {
            String originalFilename = file.getOriginalFilename();
            reclamation.setReouvertureAttachmentName(originalFilename);

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
            reclamation.setReouvertureAttachmentPath(filePath);

        } catch (java.io.IOException e) {
            throw new BadRequestException("Erreur de sauvegarde de fichier");
        }

        reclamation.setStatut(ReclamationStatus.REOUVERTE);
        reclamation.setMotifReouverture(motif);
        reclamation.setDateMiseAJour(LocalDateTime.now());
        
        // On remet la date de résolution à null car elle est à nouveau "en cours"
        reclamation.setDateResolution(null);

        slaCalculationService.recalculerSlaStatus(reclamation);

        return reclamationMapper.toResponse(reclamationRepository.save(reclamation));
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
    public org.springframework.http.ResponseEntity<org.springframework.core.io.Resource> downloadAttachment(String numeroReclamation) {
        Reclamation reclamation = reclamationRepository.findByNumeroReclamation(numeroReclamation)
                .orElseThrow(() -> new NotFoundException("Réclamation introuvable"));

        String filePath = reclamation.getAttachmentPath();
        String fileName = reclamation.getAttachmentName();

        return getFileResponseEntity(filePath, fileName);
    }

    @Override
    public org.springframework.http.ResponseEntity<org.springframework.core.io.Resource> downloadReouvertureAttachment(String numeroReclamation) {
        Reclamation reclamation = reclamationRepository.findByNumeroReclamation(numeroReclamation)
                .orElseThrow(() -> new NotFoundException("Réclamation introuvable"));

        String filePath = reclamation.getReouvertureAttachmentPath();
        String fileName = reclamation.getReouvertureAttachmentName();

        return getFileResponseEntity(filePath, fileName);
    }

    private org.springframework.http.ResponseEntity<org.springframework.core.io.Resource> getFileResponseEntity(String filePath, String fileName) {
        if (filePath == null || filePath.isEmpty()) {
            throw new BadRequestException("Aucun fichier n'est associé à cette réclamation.");
        }

        try {
            java.nio.file.Path path = java.nio.file.Paths.get(filePath);
            org.springframework.core.io.Resource resource = new org.springframework.core.io.UrlResource(path.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                throw new NotFoundException("Le fichier physique est introuvable sur le serveur.");
            }

            String contentType = "application/octet-stream";
            try {
                contentType = java.nio.file.Files.probeContentType(path);
                if (contentType == null) {
                    contentType = "application/octet-stream";
                }
            } catch (Exception ex) {
                // fall back to default
            }

            return org.springframework.http.ResponseEntity.ok()
                    .contentType(org.springframework.http.MediaType.parseMediaType(contentType))
                    .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                    .body(resource);

        } catch (java.net.MalformedURLException e) {
            throw new BadRequestException("Erreur lors de la lecture du fichier : " + e.getMessage());
        }
    }
}