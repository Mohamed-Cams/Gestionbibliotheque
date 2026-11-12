package com.gestion.bibliotheque.controllers;

import com.gestion.bibliotheque.dto.EmpruntATraiterResponse;
import com.gestion.bibliotheque.services.EmpruntService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/api/emprunts/a-traiter")
public class EmpruntATraiterControllers {

    private final EmpruntService empruntService;

    public EmpruntATraiterControllers(EmpruntService empruntService) {
        this.empruntService = empruntService;
    }

    // Liste issue de Camunda : taches utilisateur en attente (retours de livres)
    @GetMapping
    public List<EmpruntATraiterResponse> listerEmpruntsATraiter() {
        return empruntService.listerEmpruntsATraiter();
    }
}
