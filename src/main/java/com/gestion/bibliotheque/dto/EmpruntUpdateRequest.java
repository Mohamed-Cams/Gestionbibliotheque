package com.gestion.bibliotheque.dto;

import java.time.LocalDate;

public record EmpruntUpdateRequest(LocalDate dateRetourPrevue) {
}
