# Démo Web – Planning via API

Prototype Spring Boot exposant le solver GL via une API `/planifier`.

## Pré-requis
- Java 17+
- Gradle 8+
- Jar du solver : `demo/web-planning/libs/gl-solver.jar`

## Installation
1. Copier le jar généré (`build/libs/GL-Solver-1.0-SNAPSHOT.jar`) dans `demo/web-planning/libs/gl-solver.jar`.
2. Lancer l'application :
   ```bash
   ./gradlew bootRun
   ```
3. Ouvrir l'interface web : [http://localhost:8080/](http://localhost:8080/)
4. Tester l'API directement (exemple) :
   ```bash
   curl -X POST http://localhost:8080/planifier \
        -H "Content-Type: application/json" \
        -d '{"friends":["Alice","Bruno"],"activities":["Cinéma","Escape"]}'
   ```

## Fonctionnement
- `SolverService` construit un problème d'affectation (amis vs activités).
- Utilise `alwaysReduceStrategy()` et `minimize(cost)` pour trouver un coût minimal.
- Retourne l'affectation optimale ainsi que le coût dans la réponse JSON.

## Tests
- `./gradlew test` exécute le test unitaire `SolverServiceTest`.

## Améliorations possibles
- Permettre de passer une matrice de coûts personnalisée via la requête.
- Ajouter des contraintes supplémentaires (ex: préférences, disponibilités).
- Ajouter une interface web pour saisir les données et visualiser le résultat.
