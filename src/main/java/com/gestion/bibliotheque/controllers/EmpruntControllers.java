package com.gestion.bibliotheque.controllers;

import com.gestion.bibliotheque.dto.EmpruntRequest;
import com.gestion.bibliotheque.dto.EmpruntResponse;
import com.gestion.bibliotheque.dto.EmpruntUpdateRequest;
import com.gestion.bibliotheque.services.EmpruntService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/emprunts")
public class EmpruntControllers {

    private final EmpruntService empruntService;

    public EmpruntControllers(EmpruntService empruntService) {
        this.empruntService = empruntService;
    }

    @GetMapping("/list")
    public List<EmpruntResponse> obtenirListEmprunts() {
        return empruntService.obtenirListEmprunts();
    }

    @GetMapping("/{id}")
    public EmpruntResponse obtenirEmprunt(@PathVariable int id) {
        return empruntService.obtenirEmprunt(id);
    }

    // Creer un emprunt : demarre aussi le processus Camunda
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EmpruntResponse creerEmprunt(@RequestBody EmpruntRequest request) {
        return empruntService.creerEmprunt(request);
    }

    @PutMapping("/{id}")
    public EmpruntResponse modifierEmprunt(@PathVariable int id, @RequestBody EmpruntUpdateRequest request) {
        return empruntService.modifierEmprunt(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimerEmprunt(@PathVariable int id) {
        empruntService.supprimerEmprunt(id);
    }

    // Le livre est rendu : termine la tache "Retour" dans Camunda
    @PostMapping("/{id}/retour")
    public EmpruntResponse enregistrerRetour(@PathVariable int id) {
        return empruntService.enregistrerRetour(id);
    }
}
