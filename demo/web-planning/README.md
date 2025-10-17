# Démo Web – Planning via API

Prototype Spring Boot qui expose le solver GL à travers une API `/planifier`.

## Pré-requis
- Java 17+
- Gradle 8+
- Jar du solver (`gl-solver.jar`) généré avec `./gradlew jar` dans le projet principal.

## Installation
1. Copier le jar dans `demo/web-planning/libs/gl-solver.jar` (dossier ignoré par Git).
2. Depuis ce répertoire :
   ```bash
   ./gradlew bootRun
   ```
3. Appeler l'API :
   ```bash
   curl -X POST http://localhost:8080/planifier \
        -H "Content-Type: application/json" \
        -d '{"friends":["Alice","Bruno"],"activities":["Cinéma","Escape Game"]}'
   ```

## Personnalisation
- Implémenter `SolverService.solve()` pour construire le vrai modèle d'affectation.
- Ajouter des contraintes (disponibilités, préférences, coûts).
- Développer un front-end (React/Vue/HTML) pour consommer l'API.
