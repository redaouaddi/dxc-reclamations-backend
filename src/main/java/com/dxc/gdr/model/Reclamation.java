package com.dxc.gdr.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reclamations")
public class Reclamation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_reclamation", nullable = false, unique = true, length = 30)
    private String numeroReclamation;

    @Column(nullable = false, length = 150)
    private String titre;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReclamationCategory categorie;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReclamationPriority priorite;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReclamationStatus statut;

    @Column(nullable = false)
    private LocalDateTime dateCreation;

    @Column(nullable = false)
    private LocalDateTime dateMiseAJour;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private User client;

    @Column(name = "type_maintenance")
    private String typeMaintenance;

    @Column(name = "sous_categorie_incident")
    private String sousCategorieIncident;

    @Column(name = "details_autre_incident")
    private String detailsAutreIncident;

    @Column(name = "attachment_name")
    private String attachmentName;

    @Lob
    @Column(name = "attachment_data")
    private byte[] attachmentData;

    // ===== CONSTRUCTEURS =====

    public Reclamation() {
    }

    public Reclamation(Long id, String numeroReclamation, String titre, String description,
                       ReclamationCategory categorie, ReclamationPriority priorite,
                       ReclamationStatus statut, LocalDateTime dateCreation,
                       LocalDateTime dateMiseAJour, User client) {
        this.id = id;
        this.numeroReclamation = numeroReclamation;
        this.titre = titre;
        this.description = description;
        this.categorie = categorie;
        this.priorite = priorite;
        this.statut = statut;
        this.dateCreation = dateCreation;
        this.dateMiseAJour = dateMiseAJour;
        this.client = client;
    }

    // ===== GETTERS & SETTERS =====

    public Long getId() {
        return id;
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

    public ReclamationCategory getCategorie() {
        return categorie;
    }

    public void setCategorie(ReclamationCategory categorie) {
        this.categorie = categorie;
    }

    public ReclamationPriority getPriorite() {
        return priorite;
    }

    public void setPriorite(ReclamationPriority priorite) {
        this.priorite = priorite;
    }

    public ReclamationStatus getStatut() {
        return statut;
    }

    public void setStatut(ReclamationStatus statut) {
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

    public User getClient() {
        return client;
    }

    public void setClient(User client) {
        this.client = client;
    }

    public String getTypeMaintenance() {
        return typeMaintenance;
    }

    public void setTypeMaintenance(String typeMaintenance) {
        this.typeMaintenance = typeMaintenance;
    }

    public String getAttachmentName() {
        return attachmentName;
    }

    public void setAttachmentName(String attachmentName) {
        this.attachmentName = attachmentName;
    }

    public byte[] getAttachmentData() {
        return attachmentData;
    }

    public void setAttachmentData(byte[] attachmentData) {
        this.attachmentData = attachmentData;
    }

    public String getSousCategorieIncident() {
        return sousCategorieIncident;
    }

    public void setSousCategorieIncident(String sousCategorieIncident) {
        this.sousCategorieIncident = sousCategorieIncident;
    }

    public String getDetailsAutreIncident() {
        return detailsAutreIncident;
    }

    public void setDetailsAutreIncident(String detailsAutreIncident) {
        this.detailsAutreIncident = detailsAutreIncident;
    }
}