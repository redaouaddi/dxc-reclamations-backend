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
    private String attachmentName;
    private String attachmentPath;

    public MessageInterneResponse() {
    }

    public MessageInterneResponse(Long id, String contenu, LocalDateTime dateEnvoi,
                                  Boolean lu, Long auteurId, String auteurNom, Long reclamationId,
                                  String attachmentName, String attachmentPath) {
        this.id = id;
        this.contenu = contenu;
        this.dateEnvoi = dateEnvoi;
        this.lu = lu;
        this.auteurId = auteurId;
        this.auteurNom = auteurNom;
        this.reclamationId = reclamationId;
        this.attachmentName = attachmentName;
        this.attachmentPath = attachmentPath;
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

    public String getAttachmentName() {
        return attachmentName;
    }

    public void setAttachmentName(String attachmentName) {
        this.attachmentName = attachmentName;
    }

    public String getAttachmentPath() {
        return attachmentPath;
    }

    public void setAttachmentPath(String attachmentPath) {
        this.attachmentPath = attachmentPath;
    }
}