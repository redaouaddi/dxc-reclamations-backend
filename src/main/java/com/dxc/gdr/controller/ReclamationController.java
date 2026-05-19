package com.dxc.gdr.controller;

import com.dxc.gdr.Dto.request.CreateReclamationRequest;
import com.dxc.gdr.Dto.response.ReclamationResponse;
import com.dxc.gdr.Dto.response.ReclamationStatusResponse;
import com.dxc.gdr.service.interfaces.ReclamationService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/reclamations")

public class ReclamationController {

    private final ReclamationService reclamationService;

    public ReclamationController(ReclamationService reclamationService) {
        this.reclamationService = reclamationService;
    }

    @PostMapping(consumes = { "multipart/form-data" })
    @PreAuthorize("hasRole('CLIENT')")
    public ReclamationResponse createReclamation(@ModelAttribute @Valid CreateReclamationRequest request,
            @RequestParam(value = "file", required = false) MultipartFile file,
            Authentication authentication) {
        return reclamationService.createReclamation(request, file, authentication.getName());
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('SERVICE_MANAGER') or hasAuthority('CONSULTER_RAPPORTS')")
    public Page<ReclamationResponse> getAllReclamations(
            @RequestParam(name = "statut", required = false) com.dxc.gdr.model.ReclamationStatus statut,
            @org.springframework.data.web.PageableDefault(size = 10) Pageable pageable) {
        return reclamationService.getAllReclamations(statut, pageable);
    }

    @GetMapping("/mes-reclamations")
    @PreAuthorize("hasRole('CLIENT')")
    public Page<ReclamationResponse> getMyReclamations(
            Authentication authentication,
            @org.springframework.data.web.PageableDefault(size = 10) Pageable pageable) {
        return reclamationService.getMyReclamations(authentication.getName(), pageable);
    }

    @GetMapping("/mes-missions")
    @PreAuthorize("hasAuthority('AGENT') or hasAuthority('CHEF_EQUIPE') or hasAuthority('ADMIN') or hasAuthority('SERVICE_MANAGER')")
    public Page<ReclamationResponse> getMyMissions(
            Authentication authentication,
            @org.springframework.data.web.PageableDefault(size = 10) Pageable pageable) {
        return reclamationService.getMissionsAgent(authentication.getName(), pageable);
    }

    @GetMapping("/{numeroReclamation}/statut")
    @PreAuthorize("hasRole('CLIENT')")
    public ReclamationStatusResponse getReclamationStatus(@PathVariable String numeroReclamation,
            Authentication authentication) {
        return reclamationService.getReclamationStatus(numeroReclamation, authentication.getName());
    }

    @GetMapping("/count")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('CONSULTER_RAPPORTS')")
    public long countReclamations() {
        return reclamationService.countReclamations();
    }

    @GetMapping("/nouvelles")
    @PreAuthorize("hasAuthority('VOIR_NOUVELLES_RECLAMATIONS') or hasRole('ADMIN')")
    public Page<ReclamationResponse> getNouvellesReclamations(
            @org.springframework.data.web.PageableDefault(size = 10) Pageable pageable) {
        return reclamationService.getNouvellesReclamations(pageable);
    }

    @GetMapping("/{numeroReclamation}")
    @PreAuthorize("hasAuthority('ASSIGNER_RECLAMATIONS') or hasRole('ADMIN')")
    public ReclamationResponse getReclamationDetails(@PathVariable String numeroReclamation) {
        return reclamationService.getReclamationDetails(numeroReclamation);
    }

    @PutMapping("/{numeroReclamation}/assigner-equipe")
    @PreAuthorize("hasAuthority('ASSIGNER_RECLAMATIONS') or hasRole('ADMIN')")
    public ReclamationResponse assignerEquipe(
            @PathVariable String numeroReclamation,
            @RequestParam Long idEquipe) {
        return reclamationService.assignerEquipe(numeroReclamation, idEquipe);
    }

    @PutMapping("/{numeroReclamation}/rejeter")
    @PreAuthorize("hasAuthority('GERER_EQUIPE') or hasRole('ADMIN') or hasRole('CHEF_EQUIPE')")
    public ReclamationResponse rejeterReclamation(
            @PathVariable String numeroReclamation,
            @RequestParam String motif,
            Authentication authentication) {
        return reclamationService.rejeterReclamation(numeroReclamation, motif, authentication.getName());
    }

    @GetMapping("/equipe/{equipeId}")
    @PreAuthorize("hasRole('CHEF_EQUIPE') or hasRole('AGENT') or hasRole('ADMIN')")
    public Page<ReclamationResponse> getReclamationsParEquipe(
            @PathVariable Long equipeId,
            @org.springframework.data.web.PageableDefault(size = 10) Pageable pageable) {
        return reclamationService.getReclamationsParEquipe(equipeId, pageable);
    }

    @PutMapping("/{numeroReclamation}/accepter")
    @PreAuthorize("hasAuthority('GERER_EQUIPE') or hasRole('CHEF_EQUIPE') or hasRole('ADMIN')")
    public ReclamationResponse accepterReclamation(
            @PathVariable String numeroReclamation,
            Authentication authentication) {
        return reclamationService.accepterReclamation(numeroReclamation, authentication.getName());
    }

    @PutMapping("/{numeroReclamation}/resoudre")
    @PreAuthorize("hasAuthority('GERER_EQUIPE') or hasRole('CHEF_EQUIPE') or hasRole('AGENT') or hasRole('ADMIN')")
    public ReclamationResponse marquerResolue(
            @PathVariable String numeroReclamation,
            @RequestParam(value = "cause", required = false) String cause,
            @RequestParam(value = "action", required = false) String action,
            @RequestParam(value = "solution", required = false) String solution,
            Authentication authentication) {
        return reclamationService.marquerResolue(numeroReclamation, authentication.getName(), cause, action, solution);
    }

    @PutMapping(value = "/{numeroReclamation}/reouvrir", consumes = { "multipart/form-data" })
    @PreAuthorize("hasRole('CLIENT')")
    public ReclamationResponse reouvrirReclamation(
            @PathVariable String numeroReclamation,
            @RequestParam("motif") String motif,
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        return reclamationService.reouvrirReclamation(numeroReclamation, motif, file, authentication.getName());
    }

    @PostMapping("/json")
    @PreAuthorize("hasRole('CLIENT')")
    public ReclamationResponse createReclamationJson(
            @RequestBody @Valid CreateReclamationRequest request,
            Authentication authentication) {

        return reclamationService.createReclamation(request, null, authentication.getName());
    }

    @GetMapping("/{numeroReclamation}/telecharger-piece-jointe")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SERVICE_MANAGER') or hasRole('CLIENT') or hasRole('CHEF_EQUIPE') or hasRole('AGENT')")
    public org.springframework.http.ResponseEntity<org.springframework.core.io.Resource> downloadAttachment(
            @PathVariable String numeroReclamation) {
        return reclamationService.downloadAttachment(numeroReclamation);
    }

    @GetMapping("/{numeroReclamation}/telecharger-piece-jointe-reouverture")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SERVICE_MANAGER') or hasRole('CLIENT') or hasRole('CHEF_EQUIPE') or hasRole('AGENT')")
    public org.springframework.http.ResponseEntity<org.springframework.core.io.Resource> downloadReouvertureAttachment(
            @PathVariable String numeroReclamation) {
        return reclamationService.downloadReouvertureAttachment(numeroReclamation);
    }
}