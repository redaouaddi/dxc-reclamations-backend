package com.dxc.gdr.Dto.request;

public class CreateReclamationRequest {

    private String titre;
    private String description;
    private String typeMaintenance;
    private String sousCategorieIncident;
    private String detailsAutreIncident;
    private String categorie;
    private String priorite;

    public String getTitre() {
        return titre;
    }

    public String getTypeMaintenance() {
        return typeMaintenance;
    }

    public String getSousCategorieIncident() {
        return sousCategorieIncident;
    }

    public String getDetailsAutreIncident() {
        return detailsAutreIncident;
    }

    public String getDescription() {
        return description;
    }

    public String getCategorie() {
        return categorie;
    }

    public String getPriorite() {
        return priorite;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public void setTypeMaintenance(String typeMaintenance) {
        this.typeMaintenance = typeMaintenance;
    }

    public void setSousCategorieIncident(String sousCategorieIncident) {
        this.sousCategorieIncident = sousCategorieIncident;
    }

    public void setDetailsAutreIncident(String detailsAutreIncident) {
        this.detailsAutreIncident = detailsAutreIncident;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCategorie(String categorie) {
        this.categorie = categorie;
    }

    public void setPriorite(String priorite) {
        this.priorite = priorite;
    }
}