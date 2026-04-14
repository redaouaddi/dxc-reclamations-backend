package com.dxc.gdr.controller;

import com.dxc.gdr.Dto.request.CreateReclamationRequest;
import com.dxc.gdr.Dto.response.ReclamationResponse;
import com.dxc.gdr.Dto.response.ReclamationStatusResponse;
import com.dxc.gdr.service.interfaces.ReclamationService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
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
    @PostMapping(consumes = {"multipart/form-data"})
    @PreAuthorize("hasRole('CLIENT')")
    public ReclamationResponse createReclamation(@ModelAttribute @Valid CreateReclamationRequest request,
                                                 @RequestParam(value = "file", required = false) MultipartFile file,
                                                 Authentication authentication) {
        return reclamationService.createReclamation(request, file, authentication.getName());
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('SERVICE_MANAGER') or hasAuthority('CONSULTER_RAPPORTS')")
    public List<ReclamationResponse> getAllReclamations() {
        return reclamationService.getAllReclamations();
    }

    @GetMapping("/mes-reclamations")
    @PreAuthorize("hasRole('CLIENT')")
    public List<ReclamationResponse> getMyReclamations(Authentication authentication) {
        return reclamationService.getMyReclamations(authentication.getName());
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
    public List<ReclamationResponse> getNouvellesReclamations() {
        return reclamationService.getNouvellesReclamations();
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
    public List<ReclamationResponse> getReclamationsParEquipe(@PathVariable Long equipeId) {
        return reclamationService.getReclamationsParEquipe(equipeId);
    }

    @PutMapping("/{numeroReclamation}/accepter")
    @PreAuthorize("hasAuthority('GERER_EQUIPE') or hasRole('CHEF_EQUIPE') or hasRole('AGENT') or hasRole('ADMIN')")
    public ReclamationResponse accepterReclamation(@PathVariable String numeroReclamation) {
        return reclamationService.accepterReclamation(numeroReclamation);
    }

    @PutMapping("/{numeroReclamation}/resoudre")
    @PreAuthorize("hasAuthority('GERER_EQUIPE') or hasRole('CHEF_EQUIPE') or hasRole('AGENT') or hasRole('ADMIN')")
    public ReclamationResponse marquerResolue(@PathVariable String numeroReclamation) {
        return reclamationService.marquerResolue(numeroReclamation);
    }

    @PostMapping("/json")
    @PreAuthorize("hasRole('CLIENT')")
    public ReclamationResponse createReclamationJson(
            @RequestBody @Valid CreateReclamationRequest request,
            Authentication authentication) {

        return reclamationService.createReclamation(request, null, authentication.getName());
    }
}


