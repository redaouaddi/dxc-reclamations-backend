package com.dxc.gdr.Dto.response;

import java.time.LocalDateTime;

public class ReclamationStatusResponse {

    private String numeroReclamation;
    private String statut;
    private LocalDateTime dateMiseAJour;

    // ===== CONSTRUCTEUR VIDE =====
    public ReclamationStatusResponse() {
    }

    // ===== GETTERS =====

    public String getNumeroReclamation() {
        return numeroReclamation;
    }

    public String getStatut() {
        return statut;
    }

    public LocalDateTime getDateMiseAJour() {
        return dateMiseAJour;
    }

    // ===== SETTERS =====

    public void setNumeroReclamation(String numeroReclamation) {
        this.numeroReclamation = numeroReclamation;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public void setDateMiseAJour(LocalDateTime dateMiseAJour) {
        this.dateMiseAJour = dateMiseAJour;
    }
}