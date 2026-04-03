package com.dxc.gdr.Dto.response;

import java.util.List;

public class EquipeResponse {

    private Long id;
    private String nom;
    private String chefEquipeNom;
    private Long chefEquipeId;
    private int nombreAgents;
    private List<AgentResponse> agents;

    public static class AgentResponse {
        private Long id;
        private String prenom;
        private String nom;
        private String email;

        public AgentResponse() {}

        public AgentResponse(Long id, String prenom, String nom, String email) {
            this.id = id;
            this.prenom = prenom;
            this.nom = nom;
            this.email = email;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getPrenom() { return prenom; }
        public void setPrenom(String prenom) { this.prenom = prenom; }

        public String getNom() { return nom; }
        public void setNom(String nom) { this.nom = nom; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getChefEquipeNom() { return chefEquipeNom; }
    public void setChefEquipeNom(String chefEquipeNom) { this.chefEquipeNom = chefEquipeNom; }

    public Long getChefEquipeId() { return chefEquipeId; }
    public void setChefEquipeId(Long chefEquipeId) { this.chefEquipeId = chefEquipeId; }

    public int getNombreAgents() { return nombreAgents; }
    public void setNombreAgents(int nombreAgents) { this.nombreAgents = nombreAgents; }

    public List<AgentResponse> getAgents() { return agents; }
    public void setAgents(List<AgentResponse> agents) { this.agents = agents; }
}
