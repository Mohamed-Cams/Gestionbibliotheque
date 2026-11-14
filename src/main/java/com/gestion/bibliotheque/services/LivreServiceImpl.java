package com.gestion.bibliotheque.services;

import com.gestion.bibliotheque.entities.LivreEntity;
import com.gestion.bibliotheque.repositories.EmpruntRepository;
import com.gestion.bibliotheque.repositories.LivreRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class LivreServiceImpl implements LivreService {

    private final LivreRepository livreRepository;
    private final EmpruntRepository empruntRepository;

    public LivreServiceImpl(LivreRepository livreRepository, EmpruntRepository empruntRepository) {
        this.livreRepository = livreRepository;
        this.empruntRepository = empruntRepository;
    }

    @Override
    public List<LivreEntity> obtenirListLivres() {
        return livreRepository.findAll();
    }

    @Override
    public LivreEntity obtenirLivre(int numero) {
        return livreRepository.findById(numero)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Livre introuvable : " + numero));
    }

    @Override
    public LivreEntity creerLivre(LivreEntity livre) {
        valider(livre);
        livre.setNumero(0);          // force une insertion (id genere par la base)
        livre.setDisponible(true);
        return livreRepository.save(livre);
    }

    @Override
    public LivreEntity modifierLivre(int numero, LivreEntity livre) {
        valider(livre);
        LivreEntity existant = obtenirLivre(numero);
        existant.setTitre(livre.getTitre());
        existant.setAuteur(livre.getAuteur());
        existant.setCategorie(livre.getCategorie());
        return livreRepository.save(existant);
    }

    @Override
    public void supprimerLivre(int numero) {
        LivreEntity existant = obtenirLivre(numero);
        if (empruntRepository.existsByLivreNumero(numero)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Ce livre a des emprunts associes, suppression impossible");
        }
        livreRepository.delete(existant);
    }

    private void valider(LivreEntity livre) {
        if (livre.getTitre() == null || livre.getTitre().isBlank()
                || livre.getAuteur() == null || livre.getAuteur().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le titre et l'auteur sont obligatoires");
        }
    }
}
