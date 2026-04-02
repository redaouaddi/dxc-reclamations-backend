package com.dxc.gdr.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "equipes")
public class Equipe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String nom;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chef_equipe_id", unique = true)
    private User chefEquipe;

    @OneToMany(mappedBy = "equipe")
    private List<User> agents = new ArrayList<>();

    public Equipe() {
    }

    public Equipe(String nom, User chefEquipe) {
        this.nom = nom;
        this.chefEquipe = chefEquipe;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public User getChefEquipe() {
        return chefEquipe;
    }

    public void setChefEquipe(User chefEquipe) {
        this.chefEquipe = chefEquipe;
    }

    public List<User> getAgents() {
        return agents;
    }

    public void setAgents(List<User> agents) {
        this.agents = agents;
    }
}
