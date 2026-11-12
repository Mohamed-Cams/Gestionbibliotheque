package com.gestion.bibliotheque.dto;

import com.gestion.bibliotheque.entities.LivreEntity;

public record LivreRequest(String titre, String auteur, String categorie) {

    public LivreEntity toEntity() {
        LivreEntity livre = new LivreEntity();
        livre.setTitre(titre);
        livre.setAuteur(auteur);
        livre.setCategorie(categorie);
        return livre;
    }
}