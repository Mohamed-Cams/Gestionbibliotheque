package com.gestion.bibliotheque;

import com.gestion.bibliotheque.dto.*;
import com.gestion.bibliotheque.entities.*;
import com.gestion.bibliotheque.repositories.*;
import io.camunda.client.CamundaClient;
import io.camunda.client.api.search.request.UserTaskSearchRequest;
import io.camunda.client.api.search.filter.UserTaskFilter;
import io.camunda.client.api.search.response.UserTask;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.net.CookieManager;
import java.net.URI;
import java.net.http.*;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "spring.datasource.url=jdbc:h2:mem:parcours;MODE=MySQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.show-sql=false",
        "camunda.client.enabled=false",
        "bibliotheque.auth.email=admin@bibliotheque.fr",
        "bibliotheque.auth.password=admin123"
})
class ParcoursBibliothequeTest {
    @LocalServerPort int port;
    @MockitoBean(answers = org.mockito.Answers.RETURNS_DEEP_STUBS) CamundaClient camunda;
    @Autowired LivreRepository livres;
    @Autowired MembreRepository membres;
    @Autowired EmpruntRepository emprunts;

    private HttpClient navigateur() {
        return HttpClient.newBuilder().cookieHandler(new CookieManager()).build();
    }

    private HttpResponse<String> appel(HttpClient client, String methode, String chemin, String json) throws Exception {
        var request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + chemin))
                .header("Content-Type", "application/json")
                .method(methode, json == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(json))
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private void connecter(HttpClient client) throws Exception {
        assertEquals(200, appel(client, "POST", "/api/auth/login",
                "{\"email\":\"admin@bibliotheque.fr\",\"motDePasse\":\"admin123\"}").statusCode());
    }

    @Test
    void connexionSessionEtDeconnexionProtegentLesDonnees() throws Exception {
        var client = navigateur();
        assertEquals(401, appel(client, "GET", "/api/livres/list", null).statusCode());
        assertEquals(401, appel(client, "POST", "/api/auth/login",
                "{\"email\":\"admin@bibliotheque.fr\",\"motDePasse\":\"incorrect\"}").statusCode());
        connecter(client);
        assertEquals(200, appel(client, "GET", "/api/auth/session", null).statusCode());
        assertEquals(200, appel(client, "GET", "/api/livres/list", null).statusCode());
        assertEquals(204, appel(client, "POST", "/api/auth/logout", null).statusCode());
        assertEquals(401, appel(client, "GET", "/api/membres/list", null).statusCode());
    }

    @Test
    @SuppressWarnings("unchecked")
    void crudEtRetourTraversentLesControllersLesServicesEtLaBase() throws Exception {
        var client = navigateur();
        connecter(client);
        assertEquals(400, appel(client, "POST", "/api/livres", "{\"titre\":\"\",\"auteur\":\"\"}").statusCode());
        assertEquals(400, appel(client, "POST", "/api/membres", "{\"nom\":\"Test\",\"prenom\":\"Test\",\"email\":\"invalide\"}").statusCode());
        assertEquals(201, appel(client, "POST", "/api/livres",
                "{\"titre\":\"Livre parcours\",\"auteur\":\"Auteur\",\"categorie\":\"Roman\"}").statusCode());
        assertEquals(201, appel(client, "POST", "/api/membres",
                "{\"nom\":\"Test\",\"prenom\":\"Membre\",\"email\":\"membre@example.invalid\",\"telephone\":\"123\"}").statusCode());
        int numero = livres.findAll().getFirst().getNumero();
        int membreId = membres.findAll().getFirst().getId();
        assertTrue(appel(client, "GET", "/api/livres/" + numero, null).body().contains("Livre parcours"));
        assertEquals(200, appel(client, "GET", "/api/membres/list", null).statusCode());
        assertEquals(200, appel(client, "PUT", "/api/livres/" + numero,
                "{\"titre\":\"Livre modifie\",\"auteur\":\"Auteur\",\"categorie\":\"Roman\"}").statusCode());
        assertEquals(200, appel(client, "PUT", "/api/membres/" + membreId,
                "{\"nom\":\"Test\",\"prenom\":\"Modifie\",\"email\":\"membre@example.invalid\",\"telephone\":\"456\"}").statusCode());

        var creation = camunda.newCreateInstanceCommand().bpmnProcessId("processus-emprunt").latestVersion();
        when(creation.variables(anyMap()).send().join().getProcessInstanceKey()).thenReturn(123456L);
        String corps = "{\"livreNumero\":" + numero + ",\"membreId\":" + membreId + "}";
        assertEquals(201, appel(client, "POST", "/api/emprunts", corps).statusCode());
        verify(creation).variables(argThat((Map<String, Object> v) -> v != null && "Livre modifie".equals(v.get("titreLivre"))
                && "membre@example.invalid".equals(v.get("emailMembre")) && "EN_COURS".equals(v.get("statut"))));
        var emprunt = emprunts.findAll().getFirst();
        int id = emprunt.getId();
        assertFalse(livres.findById(numero).orElseThrow().isDisponible());
        assertEquals(409, appel(client, "POST", "/api/emprunts", corps).statusCode());
        assertEquals(409, appel(client, "DELETE", "/api/livres/" + numero, null).statusCode());
        assertEquals(409, appel(client, "DELETE", "/api/membres/" + membreId, null).statusCode());

        String date = LocalDate.now().plusDays(20).toString();
        assertEquals(200, appel(client, "PUT", "/api/emprunts/" + id, "{\"dateRetourPrevue\":\"" + date + "\"}").statusCode());
        verify(camunda.newSetVariablesCommand(123456L)).variables(Map.of("dateRetourPrevue", date));

        UserTaskSearchRequest recherche = mock(UserTaskSearchRequest.class, RETURNS_DEEP_STUBS);
        when(camunda.newUserTaskSearchRequest()).thenReturn(recherche);
        when(recherche.filter(any(Consumer.class))).thenReturn(recherche);
        when(recherche.sort(any(Consumer.class))).thenReturn(recherche);
        when(recherche.page(any(Consumer.class))).thenReturn(recherche);
        when(recherche.send().join().items()).thenReturn(List.of());
        assertEquals("[]", appel(client, "GET", "/api/emprunts/a-traiter", null).body());
        assertEquals(409, appel(client, "POST", "/api/emprunts/" + id + "/retour", null).statusCode());
        assertEquals(StatutEmprunt.EN_COURS, emprunts.findById(id).orElseThrow().getStatut());

        UserTask tache = mock(UserTask.class);
        when(tache.getProcessInstanceKey()).thenReturn(123456L);
        when(tache.getUserTaskKey()).thenReturn(98765L);
        when(tache.getName()).thenReturn("Retour");
        when(recherche.send().join().items()).thenReturn(List.of(tache));
        assertTrue(appel(client, "GET", "/api/emprunts/a-traiter", null).body().contains("Livre modifie"));
        assertEquals(200, appel(client, "POST", "/api/emprunts/" + id + "/retour", null).statusCode());
        verify(camunda.newCompleteUserTaskCommand(98765L)).variables(argThat((Map<String, Object> v) ->
                v != null && "RETOURNE".equals(v.get("statut")) && v.containsKey("dateRetourEffective")));
        assertEquals(StatutEmprunt.RETOURNE, emprunts.findById(id).orElseThrow().getStatut());
        assertTrue(livres.findById(numero).orElseThrow().isDisponible());
        assertEquals("[]", appel(client, "GET", "/api/emprunts/a-traiter", null).body());
        assertEquals(409, appel(client, "POST", "/api/emprunts/" + id + "/retour", null).statusCode());
        assertEquals(204, appel(client, "DELETE", "/api/emprunts/" + id, null).statusCode());

        // Une annulation refusee par Camunda conserve l'emprunt et le livre indisponible.
        assertEquals(201, appel(client, "POST", "/api/emprunts", corps).statusCode());
        int secondId = emprunts.findAll().getFirst().getId();
        when(camunda.newCancelInstanceCommand(123456L).send().join()).thenThrow(new IllegalStateException("Indisponible"));
        assertEquals(503, appel(client, "DELETE", "/api/emprunts/" + secondId, null).statusCode());
        assertTrue(emprunts.existsById(secondId));
        assertFalse(livres.findById(numero).orElseThrow().isDisponible());
        reset(camunda);
        assertEquals(204, appel(client, "DELETE", "/api/emprunts/" + secondId, null).statusCode());
        assertTrue(livres.findById(numero).orElseThrow().isDisponible());

        // Un demarrage refuse par Camunda annule aussi l'insertion SQL et le blocage du livre.
        when(camunda.newCreateInstanceCommand().bpmnProcessId("processus-emprunt").latestVersion()
                .variables(anyMap()).send().join()).thenThrow(new IllegalStateException("Indisponible"));
        assertEquals(503, appel(client, "POST", "/api/emprunts", corps).statusCode());
        assertEquals(0, emprunts.count());
        assertTrue(livres.findById(numero).orElseThrow().isDisponible());

        var filtres = org.mockito.ArgumentCaptor.forClass(Consumer.class);
        verify(recherche, atLeastOnce()).filter(filtres.capture());
        for (Consumer<UserTaskFilter> filtre : (List<Consumer<UserTaskFilter>>) (List<?>) filtres.getAllValues()) {
            UserTaskFilter criteres = mock(UserTaskFilter.class, RETURNS_SELF);
            filtre.accept(criteres);
            verify(criteres).bpmnProcessId("processus-emprunt");
            verify(criteres).elementId("Task_Retour");
            verify(criteres).state(io.camunda.client.api.search.enums.UserTaskState.CREATED);
        }
        assertEquals(204, appel(client, "DELETE", "/api/livres/" + numero, null).statusCode());
        assertEquals(204, appel(client, "DELETE", "/api/membres/" + membreId, null).statusCode());
    }
}
