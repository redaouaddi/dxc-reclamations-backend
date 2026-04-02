package com.dxc.gdr.service;

import com.dxc.gdr.Dto.request.CreateReclamationRequest;
import com.dxc.gdr.Dto.response.ReclamationResponse;
import com.dxc.gdr.Dto.response.ReclamationStatusResponse;

import java.util.List;

public interface ReclamationService {
    long countReclamations();

    ReclamationResponse createReclamation(CreateReclamationRequest request, org.springframework.web.multipart.MultipartFile file, String userEmail);

    List<ReclamationResponse> getMyReclamations(String userEmail);

    ReclamationStatusResponse getReclamationStatus(String numeroReclamation, String userEmail);

    List<ReclamationResponse> getNouvellesReclamations();

    ReclamationResponse getReclamationDetails(String numeroReclamation);

    ReclamationResponse assignerEquipe(String numeroReclamation, Long idEquipe);
    List<ReclamationResponse> getAllReclamations();
    ReclamationResponse rejeterReclamation(String numeroReclamation, String motif, String chefEmail);
}


