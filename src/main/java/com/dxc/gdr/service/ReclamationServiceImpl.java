package com.dxc.gdr.service;

import com.dxc.gdr.Dto.request.CreateReclamationRequest;
import com.dxc.gdr.Dto.response.ReclamationResponse;
import com.dxc.gdr.Dto.response.ReclamationStatusResponse;
import com.dxc.gdr.common.exception.BadRequestException;
import com.dxc.gdr.common.exception.NotFoundException;
import com.dxc.gdr.common.exception.UnauthorizedException;
import com.dxc.gdr.dao.ReclamationRepository;
import com.dxc.gdr.dao.UserRepository;
import com.dxc.gdr.mapper.ReclamationMapper;
import com.dxc.gdr.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ReclamationServiceImpl implements ReclamationService {

    private final ReclamationRepository reclamationRepository;
    private final UserRepository userRepository;
    private final ReclamationMapper reclamationMapper;
    public ReclamationServiceImpl(ReclamationRepository reclamationRepository,
                                  UserRepository userRepository,
                                  ReclamationMapper reclamationMapper) {
        this.reclamationRepository = reclamationRepository;
        this.userRepository = userRepository;
        this.reclamationMapper = reclamationMapper;
    }
    @Override
    public ReclamationResponse createReclamation(CreateReclamationRequest request, String userEmail) {

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

        Reclamation saved = reclamationRepository.save(reclamation);

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
}