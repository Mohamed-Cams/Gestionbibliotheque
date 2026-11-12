package com.gestion.bibliotheque.services;

import com.gestion.bibliotheque.entities.MembreEntity;

import java.util.List;

public interface MembreService {

    List<MembreEntity> obtenirListMembres();

    MembreEntity obtenirMembre(int id);

    MembreEntity creerMembre(MembreEntity membre);

    MembreEntity modifierMembre(int id, MembreEntity membre);

    void supprimerMembre(int id);
}
