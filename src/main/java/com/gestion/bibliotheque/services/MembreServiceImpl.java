package com.gestion.bibliotheque.services;

import com.gestion.bibliotheque.entities.MembreEntity;
import com.gestion.bibliotheque.repositories.EmpruntRepository;
import com.gestion.bibliotheque.repositories.MembreRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class MembreServiceImpl implements MembreService {

    private final MembreRepository membreRepository;
    private final EmpruntRepository empruntRepository;

    public MembreServiceImpl(MembreRepository membreRepository, EmpruntRepository empruntRepository) {
        this.membreRepository = membreRepository;
        this.empruntRepository = empruntRepository;
    }

    @Override
    public List<MembreEntity> obtenirListMembres() {
        return membreRepository.findAll();
    }

    @Override
    public MembreEntity obtenirMembre(int id) {
        return membreRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Membre introuvable : " + id));
    }

    @Override
    public MembreEntity creerMembre(MembreEntity membre) {
        valider(membre);
        membre.setId(0);
        return membreRepository.save(membre);
    }

    @Override
    public MembreEntity modifierMembre(int id, MembreEntity membre) {
        valider(membre);
        MembreEntity existant = obtenirMembre(id);
        existant.setNom(membre.getNom());
        existant.setPrenom(membre.getPrenom());
        existant.setEmail(membre.getEmail());
        existant.setTelephone(membre.getTelephone());
        return membreRepository.save(existant);
    }

    @Override
    public void supprimerMembre(int id) {
        MembreEntity existant = obtenirMembre(id);
        if (empruntRepository.existsByMembreId(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Ce membre a des emprunts associes, suppression impossible");
        }
        membreRepository.delete(existant);
    }

    private void valider(MembreEntity membre) {
        if (membre.getNom() == null || membre.getNom().isBlank()
                || membre.getPrenom() == null || membre.getPrenom().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le nom et le prenom sont obligatoires");
        }
        if (membre.getEmail() == null || !membre.getEmail().matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Une adresse email valide est obligatoire");
        }
    }
}
