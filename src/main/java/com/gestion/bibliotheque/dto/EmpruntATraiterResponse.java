package com.gestion.bibliotheque.dto;

/**
 * Une tache utilisateur Camunda en attente, rattachee a son emprunt.
 */
public record EmpruntATraiterResponse(
        long userTaskKey,
        String tache,
        String creeLe,
        EmpruntResponse emprunt) {
}
