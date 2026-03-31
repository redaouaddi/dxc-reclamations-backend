package com.dxc.gdr.service;

import com.dxc.gdr.Dto.request.CreateReclamationRequest;
import com.dxc.gdr.Dto.response.ReclamationResponse;
import com.dxc.gdr.Dto.response.ReclamationStatusResponse;

import java.util.List;

public interface ReclamationService {
    long countReclamations();

    ReclamationResponse createReclamation(CreateReclamationRequest request, String userEmail);

    List<ReclamationResponse> getMyReclamations(String userEmail);

    ReclamationStatusResponse getReclamationStatus(String numeroReclamation, String userEmail);
}