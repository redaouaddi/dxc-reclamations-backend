package com.dxc.gdr.service;

import com.dxc.gdr.Dto.request.MessageInterneRequest;
import com.dxc.gdr.Dto.response.MessageInterneResponse;
import com.dxc.gdr.dao.MessageInterneRepository;
import com.dxc.gdr.dao.ReclamationRepository;
import com.dxc.gdr.dao.UserRepository;
import com.dxc.gdr.model.MessageInterne;
import com.dxc.gdr.model.Reclamation;
import com.dxc.gdr.model.User;
import org.springframework.stereotype.Service;

import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class MessageInterneService {

    private final MessageInterneRepository messageInterneRepository;
    private final ReclamationRepository reclamationRepository;
    private final UserRepository userRepository;

    public MessageInterneService(MessageInterneRepository messageInterneRepository,
                                 ReclamationRepository reclamationRepository,
                                 UserRepository userRepository) {
        this.messageInterneRepository = messageInterneRepository;
        this.reclamationRepository = reclamationRepository;
        this.userRepository = userRepository;
    }

    public MessageInterneResponse envoyerMessage(MessageInterneRequest request, String userEmail) {
        Reclamation reclamation = reclamationRepository.findById(request.getReclamationId())
                .orElseThrow(() -> new RuntimeException("Réclamation introuvable"));

        User auteur = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Auteur introuvable"));

        MessageInterne message = new MessageInterne();
        message.setContenu(request.getContenu());
        message.setDateEnvoi(LocalDateTime.now());
        message.setLu(false);
        message.setAuteur(auteur);
        message.setReclamation(reclamation);

        MessageInterne saved = messageInterneRepository.save(message);

        return mapToDto(saved);
    }

    public org.springframework.data.domain.Page<MessageInterneResponse> getMessagesByReclamation(Long reclamationId, org.springframework.data.domain.Pageable pageable) {
        return messageInterneRepository.findByReclamationIdOrderByDateEnvoiAsc(reclamationId, pageable)
                .map(this::mapToDto);
    }

    public MessageInterneResponse envoyerMessageAvecFichier(Long reclamationId, String contenu, MultipartFile file, String userEmail) {
        Reclamation reclamation = reclamationRepository.findById(reclamationId)
                .orElseThrow(() -> new RuntimeException("Réclamation introuvable"));

        User auteur = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Auteur introuvable"));

        MessageInterne message = new MessageInterne();
        message.setContenu(contenu);
        message.setDateEnvoi(LocalDateTime.now());
        message.setLu(false);
        message.setAuteur(auteur);
        message.setReclamation(reclamation);

        if (file != null && !file.isEmpty()) {
            try {
                String originalFilename = file.getOriginalFilename();
                message.setAttachmentName(originalFilename);

                String baseUploadDir = System.getProperty("user.home")
                        + File.separator + "gdr_uploads"
                        + File.separator + "messages"
                        + File.separator + reclamation.getNumeroReclamation();

                File dir = new File(baseUploadDir);
                if (!dir.exists()) {
                    dir.mkdirs();
                }

                String extension = "";
                if (originalFilename != null && originalFilename.lastIndexOf(".") > 0) {
                    extension = originalFilename.substring(originalFilename.lastIndexOf("."));
                }

                String uniqueFilename = UUID.randomUUID().toString() + extension;
                String filePath = baseUploadDir + File.separator + uniqueFilename;

                file.transferTo(new File(filePath));
                message.setAttachmentPath(filePath);

            } catch (IOException e) {
                throw new RuntimeException("Erreur de sauvegarde de fichier : " + e.getMessage());
            }
        }

        MessageInterne saved = messageInterneRepository.save(message);
        return mapToDto(saved);
    }

    private MessageInterneResponse mapToDto(MessageInterne message) {
        String auteurNom = "";

        if (message.getAuteur() != null) {
            String firstName = message.getAuteur().getFirstName() != null ? message.getAuteur().getFirstName() : "";
            String lastName = message.getAuteur().getLastName() != null ? message.getAuteur().getLastName() : "";
            auteurNom = (firstName + " " + lastName).trim();
        }

        return new MessageInterneResponse(
                message.getId(),
                message.getContenu(),
                message.getDateEnvoi(),
                message.getLu(),
                message.getAuteur() != null ? message.getAuteur().getId() : null,
                auteurNom,
                message.getReclamation() != null ? message.getReclamation().getId() : null,
                message.getAttachmentName(),
                message.getAttachmentPath()
        );
    }
}