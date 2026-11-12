package com.gestion.bibliotheque.dto;

import com.gestion.bibliotheque.entities.MembreEntity;

public record MembreRequest(String nom, String prenom, String email, String telephone) {

    public MembreEntity toEntity() {
        MembreEntity membre = new MembreEntity();
        membre.setNom(nom);
        membre.setPrenom(prenom);
        membre.setEmail(email);
        membre.setTelephone(telephone);
        return membre;
    }
}