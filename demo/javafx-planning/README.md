# Démo JavaFX – Planning de soirée

Prototype d'interface JavaFX illustrant l'utilisation du solver GL pour organiser une soirée entre amis.

## Pré-requis
- Java 17+
- Gradle 8+
- Jar du solver (`gl-solver.jar`) compilé via `./gradlew jar` depuis le projet principal.

## Installation
1. Copier le jar généré dans `demo/javafx-planning/libs/gl-solver.jar` (le dossier `libs/` est ignoré par Git).
2. Depuis ce répertoire, lancer :
   ```bash
   ./gradlew run
   ```
3. Adapter `SolverBridge` pour modéliser réellement le problème d'affectation (amis vs activités, préférences, coûts).

## Étapes suivantes
- Ajouter des contrôles pour modifier les préférences depuis l'UI.
- Afficher plusieurs solutions optimales (pas uniquement la meilleure).
- Ajouter des visuels/avatars pour rendre la démo plus ludique.
