package com.gestion.bibliotheque.services;

import com.gestion.bibliotheque.dto.EmpruntATraiterResponse;
import com.gestion.bibliotheque.dto.EmpruntRequest;
import com.gestion.bibliotheque.dto.EmpruntResponse;
import com.gestion.bibliotheque.dto.EmpruntUpdateRequest;
import com.gestion.bibliotheque.entities.EmpruntEntity;
import com.gestion.bibliotheque.entities.LivreEntity;
import com.gestion.bibliotheque.entities.MembreEntity;
import com.gestion.bibliotheque.entities.StatutEmprunt;
import com.gestion.bibliotheque.repositories.EmpruntRepository;
import com.gestion.bibliotheque.repositories.LivreRepository;
import com.gestion.bibliotheque.repositories.MembreRepository;
import io.camunda.client.CamundaClient;
import io.camunda.client.api.search.enums.UserTaskState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EmpruntServiceImpl implements EmpruntService {

    private static final Logger log = LoggerFactory.getLogger(EmpruntServiceImpl.class);

    /** Doit correspondre a l'id du process dans emprunt.bpmn */
    public static final String PROCESSUS_EMPRUNT_ID = "processus-emprunt";

    private static final int DUREE_PAR_DEFAUT_JOURS = 14;

    private final EmpruntRepository empruntRepository;
    private final LivreRepository livreRepository;
    private final MembreRepository membreRepository;
    private final CamundaClient camundaClient;

    public EmpruntServiceImpl(EmpruntRepository empruntRepository,
                              LivreRepository livreRepository,
                              MembreRepository membreRepository,
                              CamundaClient camundaClient) {
        this.empruntRepository = empruntRepository;
        this.livreRepository = livreRepository;
        this.membreRepository = membreRepository;
        this.camundaClient = camundaClient;
    }

    @Override
    public List<EmpruntResponse> obtenirListEmprunts() {
        return empruntRepository.findAll().stream().map(EmpruntResponse::from).toList();
    }

    @Override
    public EmpruntResponse obtenirEmprunt(int id) {
        return EmpruntResponse.from(trouver(id));
    }

    @Override
    @Transactional
    public EmpruntResponse creerEmprunt(EmpruntRequest request) {
        if (request.livreNumero() <= 0 || request.membreId() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le livre et le membre sont obligatoires");
        }
        LivreEntity livre = livreRepository.findById(request.livreNumero())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Livre introuvable : " + request.livreNumero()));
        MembreEntity membre = membreRepository.findById(request.membreId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Membre introuvable : " + request.membreId()));

        if (!livre.isDisponible()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ce livre est deja emprunte");
        }

        LocalDate aujourdhui = LocalDate.now();
        LocalDate retourPrevu = request.dateRetourPrevue() != null
                ? request.dateRetourPrevue()
                : aujourdhui.plusDays(DUREE_PAR_DEFAUT_JOURS);
        if (retourPrevu.isBefore(aujourdhui)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La date de retour prevue ne peut pas etre dans le passe");
        }

        livre.setDisponible(false);
        livreRepository.save(livre);

        EmpruntEntity emprunt = new EmpruntEntity();
        emprunt.setLivre(livre);
        emprunt.setMembre(membre);
        emprunt.setDateEmprunt(aujourdhui);
        emprunt.setDateRetourPrevue(retourPrevu);
        emprunt.setStatut(StatutEmprunt.EN_COURS);
        emprunt = empruntRepository.save(emprunt);

        // Demarrage automatique du processus Camunda.
        // Toutes les donnees utiles au worker sont passees en variables : il n'a pas besoin de relire la base.
        Map<String, Object> variables = new HashMap<>();
        variables.put("empruntId", emprunt.getId());
        variables.put("livreNumero", livre.getNumero());
        variables.put("membreId", membre.getId());
        variables.put("dateEmprunt", aujourdhui.toString());
        variables.put("statut", StatutEmprunt.EN_COURS.name());
        variables.put("emailMembre", membre.getEmail());
        variables.put("nomMembre", membre.getPrenom() + " " + membre.getNom());
        variables.put("titreLivre", livre.getTitre());
        variables.put("dateRetourPrevue", retourPrevu.toString());

        try {
            var instance = camundaClient.newCreateInstanceCommand()
                    .bpmnProcessId(PROCESSUS_EMPRUNT_ID)
                    .latestVersion()
                    .variables(variables)
                    .send()
                    .join();
            emprunt.setProcessInstanceKey(instance.getProcessInstanceKey());
            emprunt = empruntRepository.save(emprunt);
        } catch (Exception e) {
            // L'exception annule la transaction : ni emprunt, ni livre bloque si Camunda est indisponible
            log.error("Impossible de demarrer le processus {}", PROCESSUS_EMPRUNT_ID, e);
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Impossible de demarrer le processus Camunda, emprunt annule", e);
        }

        return EmpruntResponse.from(emprunt);
    }

    @Override
    @Transactional
    public EmpruntResponse modifierEmprunt(int id, EmpruntUpdateRequest request) {
        EmpruntEntity emprunt = trouver(id);
        if (emprunt.getStatut() != StatutEmprunt.EN_COURS) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cet emprunt est deja cloture");
        }
        if (request.dateRetourPrevue() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "dateRetourPrevue est obligatoire");
        }
        if (request.dateRetourPrevue().isBefore(emprunt.getDateEmprunt())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La date de retour prevue ne peut pas preceder la date d'emprunt");
        }
        if (emprunt.getProcessInstanceKey() == null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Aucun processus Camunda associe a cet emprunt");
        }
        try {
            camundaClient.newSetVariablesCommand(emprunt.getProcessInstanceKey())
                    .variables(Map.of("dateRetourPrevue", request.dateRetourPrevue().toString()))
                    .send().join();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Camunda n'a pas pu modifier la date, modification annulee", e);
        }
        emprunt.setDateRetourPrevue(request.dateRetourPrevue());
        return EmpruntResponse.from(empruntRepository.save(emprunt));
    }

    @Override
    @Transactional
    public void supprimerEmprunt(int id) {
        EmpruntEntity emprunt = trouver(id);
        if (emprunt.getStatut() == StatutEmprunt.EN_COURS) {
            annulerProcessus(emprunt);
            LivreEntity livre = emprunt.getLivre();
            livre.setDisponible(true);
            livreRepository.save(livre);
        }
        empruntRepository.delete(emprunt);
    }

    @Override
    @Transactional
    public EmpruntResponse enregistrerRetour(int id) {
        EmpruntEntity emprunt = trouver(id);
        if (emprunt.getStatut() != StatutEmprunt.EN_COURS) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cet emprunt est deja cloture");
        }
        if (emprunt.getProcessInstanceKey() == null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Aucun processus Camunda associe a cet emprunt");
        }

        try {
            var taches = camundaClient.newUserTaskSearchRequest()
                    .filter(f -> f.processInstanceKey(emprunt.getProcessInstanceKey())
                            .bpmnProcessId(PROCESSUS_EMPRUNT_ID).elementId("Task_Retour")
                            .state(UserTaskState.CREATED))
                    .page(p -> p.limit(1))
                    .send()
                    .join();

            if (taches.items().isEmpty()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "La tache 'Retour' n'est pas encore disponible dans Camunda "
                                + "(notification en cours ou indexation en retard), reessayez dans un instant");
            }

            camundaClient.newCompleteUserTaskCommand(taches.items().get(0).getUserTaskKey())
                    .variables(Map.of("statut", StatutEmprunt.RETOURNE.name(),
                            "dateRetourEffective", LocalDate.now().toString()))
                    .send()
                    .join();
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Echec de la completion de la tache Retour pour l'emprunt {}", id, e);
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Camunda est indisponible ou a refuse la completion de la tache", e);
        }

        emprunt.setStatut(StatutEmprunt.RETOURNE);
        emprunt.setDateRetourEffective(LocalDate.now());
        LivreEntity livre = emprunt.getLivre();
        livre.setDisponible(true);
        livreRepository.save(livre);
        return EmpruntResponse.from(empruntRepository.save(emprunt));
    }

    @Override
    public List<EmpruntATraiterResponse> listerEmpruntsATraiter() {
        try {
            List<EmpruntATraiterResponse> resultat = new ArrayList<>();
            int offset = 0;
            while (true) {
                final int debut = offset;
                var taches = camundaClient.newUserTaskSearchRequest()
                        .filter(f -> f.state(UserTaskState.CREATED)
                                .bpmnProcessId(PROCESSUS_EMPRUNT_ID).elementId("Task_Retour"))
                        .sort(s -> s.creationDate().asc())
                        .page(p -> p.from(debut).limit(100))
                        .send().join();
                for (var tache : taches.items()) {
                    // Rattachement par la cle du processus, pas par une simple liste SQL.
                    empruntRepository.findByProcessInstanceKey(tache.getProcessInstanceKey())
                            .filter(emprunt -> emprunt.getStatut() == StatutEmprunt.EN_COURS)
                            .ifPresent(emprunt -> resultat.add(new EmpruntATraiterResponse(
                                    tache.getUserTaskKey(),
                                    tache.getName(),
                                    String.valueOf(tache.getCreationDate()),
                                    EmpruntResponse.from(emprunt))));
                }
                if (taches.items().size() < 100) break;
                offset += taches.items().size();
            }
            return resultat;
        } catch (Exception e) {
            log.error("Impossible de lire les taches Camunda", e);
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Impossible de lire les taches depuis Camunda", e);
        }
    }

    private EmpruntEntity trouver(int id) {
        return empruntRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Emprunt introuvable : " + id));
    }

    private void annulerProcessus(EmpruntEntity emprunt) {
        if (emprunt.getProcessInstanceKey() == null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Aucun processus Camunda associe a cet emprunt");
        }
        try {
            camundaClient.newCancelInstanceCommand(emprunt.getProcessInstanceKey()).send().join();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Camunda n'a pas pu annuler le processus, suppression annulee", e);
        }
    }
}
