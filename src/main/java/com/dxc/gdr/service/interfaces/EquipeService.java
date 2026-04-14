package com.dxc.gdr.service.interfaces;

import com.dxc.gdr.Dto.request.CreateEquipeRequest;
import com.dxc.gdr.Dto.request.UpdateEquipeRequest;
import com.dxc.gdr.Dto.response.EquipeResponse;

import java.util.List;

public interface EquipeService {

    /** ADMIN uniquement : crée une équipe et lui associe un chef */
    EquipeResponse creerEquipe(CreateEquipeRequest request);

    /** ADMIN : liste toutes les équipes */
    List<EquipeResponse> listerEquipes();

    /** ADMIN : modifie une équipe (nom et chef) */
    EquipeResponse modifierEquipeAdmin(Long id, com.dxc.gdr.Dto.request.UpdateEquipeAdminRequest request);


    /** CHEF_EQUIPE : renvoie l'équipe du chef connecté */
    EquipeResponse getMonEquipe(String chefEmail);

    /** CHEF_EQUIPE : modifie le nom/service de son équipe */
    EquipeResponse mettreAJourNom(String chefEmail, UpdateEquipeRequest request);

    /** CHEF_EQUIPE : ajoute un agent libre à son équipe */
    EquipeResponse ajouterAgent(String chefEmail, Long agentId);

    /** CHEF_EQUIPE : retire un agent de son équipe */
    EquipeResponse retirerAgent(String chefEmail, Long agentId);

    /** SERVICE_MANAGER / utilitaire : liste les users sans équipe (pour choisir dans UI) */
    List<EquipeResponse.AgentResponse> listerAgentsLibres();
}
