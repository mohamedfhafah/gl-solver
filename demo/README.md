# Démonstrations GL-Solver (branche `demo-app`)

Ce dossier regroupe deux prototypes indépendants illustrant l'utilisation du solver :

1. **JavaFX – Planning de soirée** (`javafx-planning`)
   - Table éditable pour saisir les préférences (1 = coup de cœur, 10 = à éviter).
   - Modes « planning individuel » et « activité commune » (top 3 solutions avec description, coût, nœuds).
   - Animations fluides, bouton « Solution suivante » et « Réinitialiser ».
   - Commande : `./gradlew run` (depuis le dossier) ou `../launch-demo.sh javafx`.

2. **Spring Boot – API & interface web** (`web-planning`)
   - Endpoint `/planifier` retourne planning individuel + solution équilibrée + podium d'activité commune.
   - Interface web (http://localhost:8080) : création du groupe, matrice interactive (sliders 1→10), cartes animées, historique.
   - Commande : `./gradlew bootRun` (depuis le dossier) ou `../launch-demo.sh web`.

> Dépendances : Java 17+, gl-solver.jar (copié dans `libs/` de chaque module), et cette branche locale `demo-app`.

## Emprunts / assistants
- Contenu généré avec l'aide de ChatGPT/Cursor pour le code UI, JS et documentation.

## To-do (optionnel)
- Ajouter de véritables captures d'écran dans `docs/`.
- Emballer les démos dans un Docker Compose.
- Prévoir des objectifs supplémentaires (ex : maximiser la variété des activités).
