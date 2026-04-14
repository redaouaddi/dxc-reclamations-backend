package com.dxc.gdr.Dto.response;

import java.time.LocalDateTime;

public class MessageInterneResponse {

    private Long id;
    private String contenu;
    private LocalDateTime dateEnvoi;
    private Boolean lu;
    private Long auteurId;
    private String auteurNom;
    private Long reclamationId;

    public MessageInterneResponse() {
    }

    public MessageInterneResponse(Long id, String contenu, LocalDateTime dateEnvoi,
                                  Boolean lu, Long auteurId, String auteurNom, Long reclamationId) {
        this.id = id;
        this.contenu = contenu;
        this.dateEnvoi = dateEnvoi;
        this.lu = lu;
        this.auteurId = auteurId;
        this.auteurNom = auteurNom;
        this.reclamationId = reclamationId;
    }

    public Long getId() {
        return id;
    }

    public String getContenu() {
        return contenu;
    }

    public LocalDateTime getDateEnvoi() {
        return dateEnvoi;
    }

    public Boolean getLu() {
        return lu;
    }

    public Long getAuteurId() {
        return auteurId;
    }

    public String getAuteurNom() {
        return auteurNom;
    }

    public Long getReclamationId() {
        return reclamationId;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setContenu(String contenu) {
        this.contenu = contenu;
    }

    public void setDateEnvoi(LocalDateTime dateEnvoi) {
        this.dateEnvoi = dateEnvoi;
    }

    public void setLu(Boolean lu) {
        this.lu = lu;
    }

    public void setAuteurId(Long auteurId) {
        this.auteurId = auteurId;
    }

    public void setAuteurNom(String auteurNom) {
        this.auteurNom = auteurNom;
    }

    public void setReclamationId(Long reclamationId) {
        this.reclamationId = reclamationId;
    }
}