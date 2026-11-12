package com.gestion.bibliotheque.controllers;

import com.gestion.bibliotheque.entities.LivreEntity;
import com.gestion.bibliotheque.services.LivreService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import com.gestion.bibliotheque.dto.LivreRequest;

import java.util.List;

@RestController
@RequestMapping(value = "/api/livres")
public class  LivreControllers {

    private final LivreService livreService;

    public LivreControllers(LivreService livreService) {
        this.livreService = livreService;
    }

    // Lister
    @GetMapping("/list")
    public List<LivreEntity> obtenirListLivres() {
        return livreService.obtenirListLivres();
    }

    // Voir les informations d'un livre
    @GetMapping("/{numero}")
    public LivreEntity obtenirLivre(@PathVariable int numero) {
        return livreService.obtenirLivre(numero);
    }

    // Creer
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LivreEntity creerLivre(@RequestBody LivreRequest livre) {
        return livreService.creerLivre(livre.toEntity());
    }

    // Modifier
    @PutMapping("/{numero}")
    public LivreEntity modifierLivre(@PathVariable int numero, @RequestBody LivreRequest livre) {
        return livreService.modifierLivre(numero, livre.toEntity());
    }

    // Supprimer
    @DeleteMapping("/{numero}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimerLivre(@PathVariable int numero) {
        livreService.supprimerLivre(numero);
    }
}
