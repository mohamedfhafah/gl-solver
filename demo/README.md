# Démonstrations GL-Solver (branche `demo-app`)

Ce dossier regroupe deux prototypes indépendants illustrant l'utilisation du solver :

1. **JavaFX – Planning de soirée** (`javafx-planning`)
   - Table éditable pour saisir les préférences (coûts) des amis.
   - Bouton « Planifier » pour lancer le solver et bouton « Solution suivante » pour parcourir les 3 meilleures affectations.
   - Animation de mise à jour + bouton « Réinitialiser » pour revenir aux valeurs par défaut.
   - Commande : `./gradlew run` (depuis le dossier) ou `../launch-demo.sh javafx`.

2. **Spring Boot – API & interface web** (`web-planning`)
   - Endpoint `/planifier` qui retourne deux objectifs : coût minimal et solution équilibrée.
   - Interface web (http://localhost:8080) avec cartes de résultats, jauge de nœuds explorés et historique des requêtes.
   - Commande : `./gradlew bootRun` (depuis le dossier) ou `../launch-demo.sh web`.

> Dépendances : Java 17+, gl-solver.jar (copié dans `libs/` de chaque module), et cette branche locale `demo-app`.

## Emprunts / assistants
- Contenu généré avec l'aide de ChatGPT/Cursor pour le code UI, JS et documentation.

## To-do (optionnel)
- Ajouter de véritables captures d'écran dans `docs/`.
- Emballer les démos dans un Docker Compose.
- Prévoir des objectifs supplémentaires (ex : maximiser la variété des activités).
