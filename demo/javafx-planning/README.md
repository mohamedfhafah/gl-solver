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
- Les préférences sont chargées depuis `src/main/resources/preferences.json`.
- Le tableau est éditable : modifier les coûts directement dans l'interface.
- Le solver (`alwaysReduceStrategy` + `minimize(cout)`) calcule jusqu'à trois solutions optimales.
- Bouton « Réinitialiser » pour revenir aux valeurs par défaut du JSON.

## Pistes d'amélioration
- Ajouter curseurs/avatars pour rendre l'édition plus attractive.
- Proposer un bouton « Voir une autre option » en parcourant toutes les solutions optimales.
- Exporter le plan (PDF, texte) ou ajouter un historique.
