package com.dxc.gdr.service.interfaces;

import com.dxc.gdr.Dto.request.CreateEquipeRequest;
import com.dxc.gdr.Dto.request.UpdateEquipeRequest;
import com.dxc.gdr.Dto.response.EquipeResponse;

import java.util.List;

public interface EquipeService {

    /** ADMIN : crée une équipe et lui associe un chef */
    EquipeResponse creerEquipe(CreateEquipeRequest request);

    /** ADMIN : liste toutes les équipes */
    List<EquipeResponse> listerEquipes();

    /** ADMIN : modifie le nom et le chef d'une équipe */
    EquipeResponse modifierEquipeAdmin(Long id, com.dxc.gdr.Dto.request.UpdateEquipeAdminRequest request);

    /** ADMIN : ajoute un agent libre à une équipe */
    EquipeResponse ajouterAgent(Long equipeId, Long agentId);

    /** ADMIN : retire un agent d'une équipe */
    EquipeResponse retirerAgent(Long equipeId, Long agentId);

    /** CHEF_EQUIPE : renvoie l'équipe du chef connecté */
    EquipeResponse getMonEquipe(String chefEmail);

    /** CHEF_EQUIPE : modifie le nom/service de son équipe */
    EquipeResponse mettreAJourNom(String chefEmail, UpdateEquipeRequest request);

    /** Utilitaire : liste les agents sans équipe */
    List<EquipeResponse.AgentResponse> listerAgentsLibres();
}