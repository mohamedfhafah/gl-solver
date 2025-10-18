# Démo JavaFX – Planning de soirée

Prototype d'interface JavaFX illustrant l'utilisation du solver GL pour organiser une soirée entre amis.

## Pré-requis
- Java 17+
- Gradle 8+
- Jar du solver : `demo/javafx-planning/libs/gl-solver.jar`

## Installation
1. Copier le jar généré (`build/libs/GL-Solver-1.0-SNAPSHOT.jar`) dans `demo/javafx-planning/libs/gl-solver.jar`.
2. Depuis ce répertoire :
   ```bash
   ./gradlew run
   ```

## Fonctionnement actuel
- Les préférences sont chargées depuis `src/main/resources/preferences.json` (amis, activités, coûts).
- Le solver utilise `alwaysReduceStrategy` + `minimize(cout)` pour trouver la meilleure affectation.
- Le résultat est affiché dans un `TextArea`.

## Pistes d'amélioration
- Ajouter une table éditable pour modifier les préférences.
- Afficher plusieurs solutions optimales (top 3) ou proposer une option « Voir une autre soirée ».
- Ajouter des visuels (avatars, icônes) et un export PDF/texte.
