package com.gestion.bibliotheque.dto;

import com.gestion.bibliotheque.entities.EmpruntEntity;
import com.gestion.bibliotheque.entities.StatutEmprunt;

import java.time.LocalDate;

public record EmpruntResponse(
        int id,
        int livreNumero,
        String livreTitre,
        int membreId,
        String membreNomComplet,
        LocalDate dateEmprunt,
        LocalDate dateRetourPrevue,
        LocalDate dateRetourEffective,
        StatutEmprunt statut,
        Long processInstanceKey) {

    public static EmpruntResponse from(EmpruntEntity e) {
        return new EmpruntResponse(
                e.getId(),
                e.getLivre().getNumero(),
                e.getLivre().getTitre(),
                e.getMembre().getId(),
                e.getMembre().getPrenom() + " " + e.getMembre().getNom(),
                e.getDateEmprunt(),
                e.getDateRetourPrevue(),
                e.getDateRetourEffective(),
                e.getStatut(),
                e.getProcessInstanceKey());
    }
}
