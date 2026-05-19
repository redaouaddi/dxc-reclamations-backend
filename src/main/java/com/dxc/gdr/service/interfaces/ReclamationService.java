package com.dxc.gdr.service.interfaces;

import com.dxc.gdr.Dto.request.CreateReclamationRequest;
import com.dxc.gdr.Dto.response.ReclamationResponse;
import com.dxc.gdr.Dto.response.ReclamationStatusResponse;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ReclamationService {
    long countReclamations();

    ReclamationResponse createReclamation(CreateReclamationRequest request, org.springframework.web.multipart.MultipartFile file, String userEmail);

    Page<ReclamationResponse> getMyReclamations(String userEmail, Pageable pageable);

    ReclamationStatusResponse getReclamationStatus(String numeroReclamation, String userEmail);

    Page<ReclamationResponse> getNouvellesReclamations(Pageable pageable);

    ReclamationResponse getReclamationDetails(String numeroReclamation);

    ReclamationResponse assignerEquipe(String numeroReclamation, Long idEquipe);
    Page<ReclamationResponse> getAllReclamations(com.dxc.gdr.model.ReclamationStatus statut, Pageable pageable);
    ReclamationResponse rejeterReclamation(String numeroReclamation, String motif, String chefEmail);

    Page<ReclamationResponse> getReclamationsParEquipe(Long equipeId, Pageable pageable);
    Page<ReclamationResponse> getMissionsAgent(String agentEmail, Pageable pageable);
    ReclamationResponse accepterReclamation(String numeroReclamation, String userEmail);
    ReclamationResponse marquerResolue(String numeroReclamation, String userEmail, String cause, String action, String solution);
    ReclamationResponse reouvrirReclamation(String numeroReclamation, String motif, org.springframework.web.multipart.MultipartFile file, String userEmail);
    org.springframework.http.ResponseEntity<org.springframework.core.io.Resource> downloadAttachment(String numeroReclamation);
    org.springframework.http.ResponseEntity<org.springframework.core.io.Resource> downloadReouvertureAttachment(String numeroReclamation);
}





