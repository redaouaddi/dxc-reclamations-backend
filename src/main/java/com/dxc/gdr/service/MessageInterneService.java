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

    public MessageInterneResponse envoyerMessage(MessageInterneRequest request) {
        Reclamation reclamation = reclamationRepository.findById(request.getReclamationId())
                .orElseThrow(() -> new RuntimeException("Réclamation introuvable"));

        User auteur = userRepository.findById(request.getAuteurId())
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

    public List<MessageInterneResponse> getMessagesByReclamation(Long reclamationId) {
        return messageInterneRepository.findByReclamationIdOrderByDateEnvoiAsc(reclamationId)
                .stream()
                .map(this::mapToDto)
                .toList();
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
                message.getReclamation() != null ? message.getReclamation().getId() : null
        );
    }
}