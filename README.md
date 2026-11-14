# Bibliothèque – frontend Angular

Interface de gestion des prêts pour `gestion-bibliotheque-backend`.

## Lancer

1. Démarrer Camunda 8, puis le backend (port 8081).
2. Dans ce dossier :

```bash
npm install
npm start        # http://localhost:4200
```

Les appels `/api/...` sont redirigés vers `http://localhost:8081` par `proxy.conf.json`
(pas de configuration CORS nécessaire en développement).

Le frontend et le backend peuvent rester sur les branches `Frontend` et `Backend` du même dépôt : leur communication dépend de l'API HTTP, pas des branches Git. GitHub stocke le code ; il ne lance pas le backend, MySQL ou Camunda.
Pour un hébergement, servir Angular et rediriger `/api` vers le backend sur le même domaine permet de conserver les URL et la session actuelles. `proxy.conf.json` s'applique uniquement à `ng serve`.

## Pages

- `/login` : connexion réelle au backend, session et déconnexion (compte de démonstration : `admin@bibliotheque.fr` / `admin123`)
- `/a-traiter` : retours en attente, lus depuis Camunda (rafraîchis toutes les 8 s), accès à la finalisation
- `/emprunts` : liste, changement de date, accès à la finalisation et suppression
- `/emprunts/nouveau` : formulaire de création ; démarrage du processus par le backend
- `/emprunts/:id/finaliser` : consultation de l'emprunt et confirmation du retour lorsque la tâche Camunda est disponible
- `/livres` : CRUD + recherche
- `/livres/:numero` : détail chargé depuis le backend et accès à l'emprunt du livre
- `/membres` : CRUD

Toutes les pages métier exigent une session backend. Les styles et la navigation de l'application existante sont conservés.

`npm run build` vérifie le code et les templates Angular. Le parcours Camunda/Gmail complet est décrit dans le README du backend.
