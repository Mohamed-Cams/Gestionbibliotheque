package com.gestion.bibliotheque.controllers;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    public static final String SESSION_UTILISATEUR = "bibliotheque.utilisateur";
    private final String email;
    private final String motDePasse;

    public AuthController(@Value("${bibliotheque.auth.email}") String email,
                          @Value("${bibliotheque.auth.password}") String motDePasse) {
        this.email = email;
        this.motDePasse = motDePasse;
    }

    public record Connexion(String email, String motDePasse) {}
    public record Utilisateur(String email) {}

    @PostMapping("/login")
    public Utilisateur connecter(@RequestBody Connexion connexion, HttpServletRequest request) {
        if (connexion.email() == null || connexion.motDePasse() == null
                || !email.equalsIgnoreCase(connexion.email().trim())
                || !MessageDigest.isEqual(motDePasse.getBytes(StandardCharsets.UTF_8),
                    connexion.motDePasse().getBytes(StandardCharsets.UTF_8))) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email ou mot de passe incorrect");
        }
        var ancienneSession = request.getSession(false);
        if (ancienneSession != null) ancienneSession.invalidate();
        var utilisateur = new Utilisateur(email);
        request.getSession(true).setAttribute(SESSION_UTILISATEUR, utilisateur);
        return utilisateur;
    }

    @GetMapping("/session")
    public Utilisateur session(HttpServletRequest request) {
        var session = request.getSession(false);
        if (session == null || !(session.getAttribute(SESSION_UTILISATEUR) instanceof Utilisateur utilisateur)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Veuillez vous connecter");
        }
        return utilisateur;
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deconnecter(HttpServletRequest request) {
        var session = request.getSession(false);
        if (session != null) session.invalidate();
    }
}
