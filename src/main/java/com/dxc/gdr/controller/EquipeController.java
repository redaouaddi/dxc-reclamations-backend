package com.dxc.gdr.controller;

import com.dxc.gdr.Dto.request.CreateEquipeRequest;
import com.dxc.gdr.Dto.request.UpdateEquipeRequest;
import com.dxc.gdr.Dto.response.EquipeResponse;
import com.dxc.gdr.service.interfaces.EquipeService;
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

    // ─── ADMIN : Modifier une équipe (nom + chef) ─────────────────────────────

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('CREER_EQUIPE') or hasRole('ADMIN')")
    public EquipeResponse modifierEquipeAdmin(
            @PathVariable Long id,
            @RequestBody @Valid com.dxc.gdr.Dto.request.UpdateEquipeAdminRequest request) {
        return equipeService.modifierEquipeAdmin(id, request);
    }

    // ─── ADMIN : Ajouter un agent à une équipe ────────────────────────────────

    @PostMapping("/{id}/agents/{agentId}")
    @PreAuthorize("hasAuthority('CREER_EQUIPE') or hasRole('ADMIN')")
    public EquipeResponse ajouterAgent(
            @PathVariable Long id,
            @PathVariable Long agentId) {
        return equipeService.ajouterAgent(id, agentId);
    }

    // ─── ADMIN : Retirer un agent d'une équipe ────────────────────────────────

    @DeleteMapping("/{id}/agents/{agentId}")
    @PreAuthorize("hasAuthority('CREER_EQUIPE') or hasRole('ADMIN')")
    public EquipeResponse retirerAgent(
            @PathVariable Long id,
            @PathVariable Long agentId) {
        return equipeService.retirerAgent(id, agentId);
    }

    // ─── CHEF_EQUIPE : Voir mon équipe ────────────────────────────────────────

    @GetMapping("/ma-gestion")
    @PreAuthorize("hasAuthority('GERER_EQUIPE') or hasRole('SERVICE_MANAGER') or hasRole('CHEF_EQUIPE') or hasRole('AGENT')")
    public EquipeResponse getMonEquipe(Authentication authentication) {
        return equipeService.getMonEquipe(authentication.getName());
    }

    // ─── CHEF_EQUIPE : Modifier le nom du service ─────────────────────────────

    @PutMapping("/ma-gestion")
    @PreAuthorize("hasAuthority('GERER_EQUIPE') or hasRole('SERVICE_MANAGER') or hasRole('CHEF_EQUIPE')")
    public EquipeResponse mettreAJourNom(Authentication authentication,
                                         @RequestBody @Valid UpdateEquipeRequest request) {
        return equipeService.mettreAJourNom(authentication.getName(), request);
    }

    // ─── Agents libres (sans équipe) ──────────────────────────────────────────

    @GetMapping("/agents-libres")
    @PreAuthorize("hasAuthority('CREER_EQUIPE') or hasRole('ADMIN')")
    public List<EquipeResponse.AgentResponse> listerAgentsLibres() {
        return equipeService.listerAgentsLibres();
    }
}