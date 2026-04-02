package com.dxc.gdr.mapper;

import com.dxc.gdr.Dto.response.ReclamationResponse;
import com.dxc.gdr.Dto.response.ReclamationStatusResponse;
import com.dxc.gdr.model.Reclamation;
import com.dxc.gdr.model.ReclamationStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class ReclamationMapper {

    public ReclamationResponse toResponse(Reclamation reclamation) {

        ReclamationResponse response = new ReclamationResponse();

        response.setId(reclamation.getId());
        response.setNumeroReclamation(reclamation.getNumeroReclamation());
        response.setTitre(reclamation.getTitre());
        response.setDescription(reclamation.getDescription());
        
        response.setCategorie(reclamation.getCategorie() != null ? reclamation.getCategorie().name() : "NON_SPECIFIE");
        response.setPriorite(reclamation.getPriorite() != null ? reclamation.getPriorite().name() : "MOYENNE");
        response.setStatut(determineStatut(reclamation));
        response.setMotifRefus(reclamation.getMotifRefus());

        if(reclamation.getEquipeAssignee() != null){

            response.setEquipeAssignee(reclamation.getEquipeAssignee().getNom());
        }
        
        if(reclamation.getClient() != null){
            String nomComplet = (reclamation.getClient().getFirstName() != null ? reclamation.getClient().getFirstName() : "")
                    + " " + (reclamation.getClient().getLastName() != null ? reclamation.getClient().getLastName() : "");
            response.setClientNom(nomComplet.trim());
        }
        
        response.setDateCreation(reclamation.getDateCreation());
        response.setDateMiseAJour(reclamation.getDateMiseAJour());


        return response;
    }

    public ReclamationStatusResponse toStatusResponse(Reclamation reclamation) {

        ReclamationStatusResponse response = new ReclamationStatusResponse();

        response.setNumeroReclamation(reclamation.getNumeroReclamation());
        response.setStatut(determineStatut(reclamation));
        response.setDateMiseAJour(reclamation.getDateMiseAJour());

        return response;
    }

    private String determineStatut(Reclamation reclamation) {
        if (reclamation.getStatut() == null) {
            return "EN_ATTENTE";
        }

        String actualStatut = reclamation.getStatut().name();

        if (reclamation.getStatut() == ReclamationStatus.REJETEE) {
            boolean isAdminOrManager = SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_SERVICE_MANAGER"));

            if (!isAdminOrManager) {
                return "EN_ATTENTE";
            }
        }

        return actualStatut;
    }
}