# Gestion de bibliothèque — backend

Exercice Spring Boot / Angular / Camunda 8. Java 21, MySQL et Camunda doivent être disponibles.

## Démarrage

1. Créer la base MySQL `gestion_bibliotheque` et adapter les accès dans `src/main/resources/application.yaml`.
2. Démarrer Camunda : REST `http://localhost:8080`, gRPC `http://localhost:26500`.
3. Copier `application-local.properties.example` vers `application-local.properties`, puis renseigner le compte Gmail et son mot de passe d'application. Ce fichier local est ignoré par Git. On peut aussi utiliser les variables d'environnement `SPRING_MAIL_USERNAME` et `SPRING_MAIL_PASSWORD`.
4. Lancer `mvn spring-boot:run` depuis ce dossier. Le backend écoute sur le port **8081** et déploie `bpmn/emprunt.bpmn`.
5. Lancer `npm start` dans `../Gestionbibliotheque-Frontend`, puis ouvrir `http://localhost:4200`.

Compte de démonstration de l'exercice : **admin@bibliotheque.fr / admin123**.
Les variables `BIBLIOTHEQUE_ADMIN_EMAIL` et `BIBLIOTHEQUE_ADMIN_PASSWORD` permettent de personnaliser ce compte avant le démarrage.
La connexion crée une session serveur de 30 minutes ; toutes les routes métier `/api` exigent cette session.

## GitHub et communication frontend/backend

Le même dépôt GitHub contient deux branches : `Backend` pour ce projet et `Frontend` pour Angular. Chaque dossier local suit sa branche et conserve ses propres fichiers.
Pour récupérer les deux projets sur un autre poste :

```sh
git clone --single-branch --branch Backend https://github.com/Mohamed-Cams/Gestionbibliotheque.git Gestionbibliotheque
git clone --single-branch --branch Frontend https://github.com/Mohamed-Cams/Gestionbibliotheque.git Gestionbibliotheque-Frontend
```

Les branches ne sont pas un hébergement. En local, Angular (`localhost:4200`) appelle `/api/...` ; le serveur de développement redirige ces requêtes vers Spring Boot (`localhost:8081`) via `proxy.conf.json`. Le backend accède ensuite à MySQL et Camunda.
Sur Internet, il faut héberger Angular et Spring Boot. Une configuration avec le même domaine peut servir Angular et rediriger `/api` vers Spring Boot, ce qui conserve les URL et les cookies de session actuels. Le proxy Angular ne fonctionne que pendant `ng serve`.

## Correspondance avec l'énoncé

| Fonction | API | Effet |
|---|---|---|
| Connexion / session / déconnexion | `POST /api/auth/login`, `GET /api/auth/session`, `POST /api/auth/logout` | Session vérifiée par le backend |
| CRUD livres | `GET /api/livres/list`, `GET /api/livres/{numero}`, `POST /api/livres`, `PUT /api/livres/{numero}`, `DELETE /api/livres/{numero}` | Catalogue MySQL |
| CRUD membres | `GET /api/membres/list`, `GET /api/membres/{id}`, `POST /api/membres`, `PUT /api/membres/{id}`, `DELETE /api/membres/{id}` | Membres MySQL |
| Créer un emprunt | `POST /api/emprunts` | Enregistre l'emprunt, rend le livre indisponible et démarre `processus-emprunt` |
| Lister / consulter | `GET /api/emprunts/list`, `GET /api/emprunts/{id}` | Emprunts MySQL |
| Modifier l'échéance | `PUT /api/emprunts/{id}` | Met à jour la date en base et la variable du processus |
| Supprimer un emprunt | `DELETE /api/emprunts/{id}` | Annule le processus actif puis libère le livre ; refuse la suppression si Camunda refuse l'annulation |
| Lister les emprunts à traiter | `GET /api/emprunts/a-traiter` | Recherche paginée des tâches `Task_Retour` du processus dans Camunda, rattachées aux emprunts |
| Finaliser un emprunt | `POST /api/emprunts/{id}/retour` | Termine la tâche Camunda avec les variables de retour, clôture l'emprunt et libère le livre |

Le BPMN reste : **démarrage → notification Gmail par worker → tâche utilisateur « Retour » → fin**.
La tâche utilisateur représente l'intervention manuelle et attend une confirmation de retour. Si Gmail échoue, Camunda retente le job puis crée un incident : la tâche Retour reste indisponible jusqu'à résolution.
La liste des retours ne provient pas d'un simple filtre MySQL.

## Vérification

`mvn test` utilise H2 et un client Camunda simulé, sans MySQL local ni envoi Gmail. Les tests HTTP couvrent la session, le CRUD, les protections métier, la modification des variables du processus, le retour et le refus de suppression lorsque Camunda échoue.
Ces tests ne remplacent pas une vérification avec Camunda et Gmail réels.

Pour la démonstration complète : créer un livre et un membre avec une adresse accessible, ouvrir le détail du livre, créer l'emprunt, vérifier la réception du mail et la tâche Retour dans Camunda, puis ouvrir « À traiter » et finaliser. Le livre doit redevenir disponible et le processus doit être terminé.

MySQL et Camunda n'ont pas de transaction commune : une panne après une commande acceptée par Camunda peut encore nécessiter une réconciliation. L'exercice conserve son architecture actuelle.
