package com.gestion.bibliotheque.services;

import com.gestion.bibliotheque.dto.EmpruntATraiterResponse;
import com.gestion.bibliotheque.dto.EmpruntRequest;
import com.gestion.bibliotheque.dto.EmpruntResponse;
import com.gestion.bibliotheque.dto.EmpruntUpdateRequest;

import java.util.List;

public interface EmpruntService {

    List<EmpruntResponse> obtenirListEmprunts();

    EmpruntResponse obtenirEmprunt(int id);

    /** Enregistre l'emprunt puis demarre automatiquement le processus Camunda. */
    EmpruntResponse creerEmprunt(EmpruntRequest request);

    EmpruntResponse modifierEmprunt(int id, EmpruntUpdateRequest request);

    void supprimerEmprunt(int id);

    /** Termine la tache manuelle "Retour" dans Camunda et cloture l'emprunt. */
    EmpruntResponse enregistrerRetour(int id);

    /** Emprunts dont une tache est en attente dans Camunda (liste issue de Camunda). */
    List<EmpruntATraiterResponse> listerEmpruntsATraiter();
}
