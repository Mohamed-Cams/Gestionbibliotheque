package com.gestion.bibliotheque.dto;

import java.time.LocalDate;

/**
 * Corps de POST /api/emprunts.
 * dateRetourPrevue est optionnelle (14 jours par defaut).
 */
public record EmpruntRequest(int livreNumero, int membreId, LocalDate dateRetourPrevue) {
}
