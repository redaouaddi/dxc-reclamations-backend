package com.dxc.gdr.Dto.response;

import java.time.LocalDateTime;

public class ReclamationResponse {

    private Long id;
    private String numeroReclamation;
    private String titre;
    private String description;
    private String categorie;
    private String priorite;
    private String statut;
    private LocalDateTime dateCreation;
    private LocalDateTime dateMiseAJour;
    private String equipeAssignee;
    private String clientNom;
    private String motifRefus;
    private LocalDateTime slaDeadline;
    private String slaStatus;
    private LocalDateTime dateResolution;

    public ReclamationResponse() {
    }

    public ReclamationResponse(Long id, String numeroReclamation, String titre, String description,
                               String categorie, String priorite, String statut,
                               LocalDateTime dateCreation, LocalDateTime dateMiseAJour,
                               String motifRefus) {
        this.id = id;
        this.numeroReclamation = numeroReclamation;
        this.titre = titre;
        this.description = description;
        this.categorie = categorie;
        this.priorite = priorite;
        this.statut = statut;
        this.dateCreation = dateCreation;
        this.dateMiseAJour = dateMiseAJour;
        this.motifRefus = motifRefus;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNumeroReclamation() {
        return numeroReclamation;
    }

    public void setNumeroReclamation(String numeroReclamation) {
        this.numeroReclamation = numeroReclamation;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategorie() {
        return categorie;
    }

    public void setCategorie(String categorie) {
        this.categorie = categorie;
    }

    public String getPriorite() {
        return priorite;
    }

    public void setPriorite(String priorite) {
        this.priorite = priorite;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }

    public LocalDateTime getDateMiseAJour() {
        return dateMiseAJour;
    }

    public void setDateMiseAJour(LocalDateTime dateMiseAJour) {
        this.dateMiseAJour = dateMiseAJour;
    }

    public String getEquipeAssignee() {
        return equipeAssignee;
    }

    public void setEquipeAssignee(String equipeAssignee) {
        this.equipeAssignee = equipeAssignee;
    }

    public String getClientNom() {
        return clientNom;
    }

    public void setClientNom(String clientNom) {
        this.clientNom = clientNom;
    }

    public String getMotifRefus() {
        return motifRefus;
    }

    public void setMotifRefus(String motifRefus) {
        this.motifRefus = motifRefus;
    }

    public LocalDateTime getSlaDeadline() {
        return slaDeadline;
    }

    public void setSlaDeadline(LocalDateTime slaDeadline) {
        this.slaDeadline = slaDeadline;
    }

    public String getSlaStatus() {
        return slaStatus;
    }

    public void setSlaStatus(String slaStatus) {
        this.slaStatus = slaStatus;
    }

    public LocalDateTime getDateResolution() {
        return dateResolution;
    }

    public void setDateResolution(LocalDateTime dateResolution) {
        this.dateResolution = dateResolution;
    }
}