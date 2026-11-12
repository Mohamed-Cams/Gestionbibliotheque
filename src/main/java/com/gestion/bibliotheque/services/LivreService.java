package com.gestion.bibliotheque.services;

import com.gestion.bibliotheque.entities.LivreEntity;

import java.util.List;

public interface LivreService {

    List<LivreEntity> obtenirListLivres();

    LivreEntity obtenirLivre(int numero);

    LivreEntity creerLivre(LivreEntity livre);

    LivreEntity modifierLivre(int numero, LivreEntity livre);

    void supprimerLivre(int numero);
}
