package com.dxc.gdr.Dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CreateEquipeRequest {

    @NotBlank(message = "Le nom de l'équipe est obligatoire")
    @Size(max = 100, message = "Le nom ne peut pas dépasser 100 caractères")
    private String nom;

    @NotBlank(message = "L'email du chef d'équipe est obligatoire")
    private String chefEmail;

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getChefEmail() {
        return chefEmail;
    }

    public void setChefEmail(String chefEmail) {
        this.chefEmail = chefEmail;
    }
}

