package com.gestion.bibliotheque.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> erreur(ResponseStatusException erreur) {
        return ResponseEntity.status(erreur.getStatusCode())
                .body(Map.of("message", erreur.getReason() == null ? "Requete refusee" : erreur.getReason()));
    }
}
