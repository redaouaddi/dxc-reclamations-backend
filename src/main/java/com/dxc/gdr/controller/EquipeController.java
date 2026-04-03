package com.dxc.gdr.controller;

import com.dxc.gdr.Dto.request.CreateEquipeRequest;
import com.dxc.gdr.Dto.request.UpdateEquipeRequest;
import com.dxc.gdr.Dto.response.EquipeResponse;
import com.dxc.gdr.service.EquipeService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/equipes")
public class EquipeController {

    private final EquipeService equipeService;

    public EquipeController(EquipeService equipeService) {
        this.equipeService = equipeService;
    }

    // ─── ADMIN : Créer une équipe ─────────────────────────────────────────────

    @PostMapping
    @PreAuthorize("hasAuthority('CREER_EQUIPE') or hasRole('ADMIN')")
    public EquipeResponse creerEquipe(@RequestBody @Valid CreateEquipeRequest request) {
        return equipeService.creerEquipe(request);
    }

    // ─── ADMIN : Lister toutes les équipes ────────────────────────────────────

    @GetMapping
    @PreAuthorize("hasAuthority('CREER_EQUIPE') or hasAuthority('ASSIGNER_RECLAMATIONS') or hasRole('ADMIN')")
    public List<EquipeResponse> listerEquipes() {
        return equipeService.listerEquipes();
    }

    // ─── ADMIN : Modifier une équipe ──────────────────────────────────────────

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('CREER_EQUIPE') or hasRole('ADMIN')")
    public EquipeResponse modifierEquipeAdmin(
            @PathVariable Long id,
            @RequestBody @Valid com.dxc.gdr.Dto.request.UpdateEquipeAdminRequest request) {
        return equipeService.modifierEquipeAdmin(id, request);
    }

    // ─── CHEF_EQUIPE : Voir mon équipe ────────────────────────────────────────


    @GetMapping("/ma-gestion")
    @PreAuthorize("hasAuthority('GERER_EQUIPE') or hasRole('SERVICE_MANAGER') or hasRole('CHEF_EQUIPE') or hasRole('AGENT')")
    public EquipeResponse getMonEquipe(Authentication authentication) {
        return equipeService.getMonEquipe(authentication.getName());
    }

    // ─── CHEF_EQUIPE : Modifier le nom du service ────────────────────────────

    @PutMapping("/ma-gestion")
    @PreAuthorize("hasAuthority('GERER_EQUIPE') or hasRole('SERVICE_MANAGER') or hasRole('CHEF_EQUIPE')")
    public EquipeResponse mettreAJourNom(Authentication authentication,
                                          @RequestBody @Valid UpdateEquipeRequest request) {
        return equipeService.mettreAJourNom(authentication.getName(), request);
    }

    // ─── CHEF_EQUIPE : Ajouter un agent libre ─────────────────────────────────

    @PostMapping("/ma-gestion/agents/{agentId}")
    @PreAuthorize("hasAuthority('GERER_EQUIPE') or hasRole('SERVICE_MANAGER') or hasRole('CHEF_EQUIPE')")
    public EquipeResponse ajouterAgent(Authentication authentication,
                                        @PathVariable Long agentId) {
        return equipeService.ajouterAgent(authentication.getName(), agentId);
    }

    // ─── CHEF_EQUIPE : Retirer un agent ───────────────────────────────────────

    @DeleteMapping("/ma-gestion/agents/{agentId}")
    @PreAuthorize("hasAuthority('GERER_EQUIPE') or hasRole('SERVICE_MANAGER') or hasRole('CHEF_EQUIPE')")
    public EquipeResponse retirerAgent(Authentication authentication,
                                        @PathVariable Long agentId) {
        return equipeService.retirerAgent(authentication.getName(), agentId);
    }

    // ─── Agents libres (sans équipe) ──────────────────────────────────────────

    @GetMapping("/agents-libres")
    @PreAuthorize("hasAuthority('GERER_EQUIPE') or hasRole('SERVICE_MANAGER') or hasRole('CHEF_EQUIPE') or hasRole('AGENT') or hasAuthority('CREER_EQUIPE') or hasRole('ADMIN')")
    public List<EquipeResponse.AgentResponse> listerAgentsLibres() {
        return equipeService.listerAgentsLibres();
    }
}
