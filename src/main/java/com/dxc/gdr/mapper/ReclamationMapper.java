package com.dxc.gdr.mapper;

import com.dxc.gdr.Dto.response.ReclamationResponse;
import com.dxc.gdr.Dto.response.ReclamationStatusResponse;
import com.dxc.gdr.model.Reclamation;
import org.springframework.stereotype.Component;

@Component
public class ReclamationMapper {

    public ReclamationResponse toResponse(Reclamation reclamation) {

        ReclamationResponse response = new ReclamationResponse();

        response.setId(reclamation.getId());
        response.setNumeroReclamation(reclamation.getNumeroReclamation());
        response.setTitre(reclamation.getTitre());
        response.setDescription(reclamation.getDescription());
        response.setCategorie(reclamation.getCategorie().name());
        response.setPriorite(reclamation.getPriorite().name());
        response.setStatut(reclamation.getStatut().name());
        response.setDateCreation(reclamation.getDateCreation());
        response.setDateMiseAJour(reclamation.getDateMiseAJour());

        return response;
    }

    public ReclamationStatusResponse toStatusResponse(Reclamation reclamation) {

        ReclamationStatusResponse response = new ReclamationStatusResponse();

        response.setNumeroReclamation(reclamation.getNumeroReclamation());
        response.setStatut(reclamation.getStatut().name());
        response.setDateMiseAJour(reclamation.getDateMiseAJour());

        return response;
    }
}