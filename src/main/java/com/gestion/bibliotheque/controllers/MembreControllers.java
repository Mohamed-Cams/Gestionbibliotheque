package com.gestion.bibliotheque.controllers;

import com.gestion.bibliotheque.dto.MembreRequest;
import com.gestion.bibliotheque.entities.MembreEntity;
import com.gestion.bibliotheque.services.MembreService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/membres")
public class MembreControllers {

    private final MembreService membreService;

    public MembreControllers(MembreService membreService) {
        this.membreService = membreService;
    }

    @GetMapping("/list")
    public List<MembreEntity> obtenirListMembres() {
        return membreService.obtenirListMembres();
    }

    @GetMapping("/{id}")
    public MembreEntity obtenirMembre(@PathVariable int id) {
        return membreService.obtenirMembre(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MembreEntity creerMembre(@RequestBody MembreRequest membre) {
        return membreService.creerMembre(membre.toEntity());
    }
    @PutMapping("/{id}")
    public MembreEntity modifierMembre(@PathVariable int id, @RequestBody MembreRequest membre) {
        return membreService.modifierMembre(id, membre.toEntity());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimerMembre(@PathVariable int id) {
        membreService.supprimerMembre(id);
    }
}
