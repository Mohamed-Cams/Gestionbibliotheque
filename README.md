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

## Pages

- `/a-traiter` : retours en attente, lus depuis Camunda (rafraîchis toutes les 8 s)
- `/emprunts` : créer, changer la date de retour, enregistrer le retour, supprimer
- `/livres` : CRUD + recherche
- `/membres` : CRUD
