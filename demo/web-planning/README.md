# Démo Web – Planning via API

Prototype Spring Boot exposant le solver GL via une API `/planifier`.

## Pré-requis
- Java 17+
- Gradle 8+
- Jar du solver : `demo/web-planning/libs/gl-solver.jar`

## Installation / Lancement
```bash
# depuis la racine du repo
./gradlew -p demo/web-planning bootRun
# ou via le script commun
./demo/launch-demo.sh web
```
Interface : [http://localhost:8080/](http://localhost:8080/)
Test API :
```bash
curl -X POST http://localhost:8080/planifier \
     -H "Content-Type: application/json" \
     -d '{"friends":["Alice","Bruno"],"activities":["Cinéma","Escape"]}'
```

## Fonctionnement
- `SolverService` centralise la construction du problème (amis vs activités).
- Deux objectifs sont proposés : coût minimal et solution équilibrée (écarts plus homogènes).
- La réponse JSON inclut affectation, coût optimum, nombre de nœuds explorés.

## UI Web
- Champs pour saisir amis/activités (laisser vide pour les données par défaut).
- Cartes animées présentant les deux objectifs, jauge du nombre de nœuds, historique des requêtes.
- Échelle des préférences rappelée dans la page (1 = adoré ; 10 = à éviter).

## Tests
```bash
./gradlew -p demo/web-planning test
```
Le test `SolverServiceTest` vérifie l'intégration du solver.

## Améliorations possibles
- Permettre de passer une matrice de coûts personnalisée via la requête.
- Ajouter des contraintes supplémentaires (ex: préférences, disponibilités).
- Ajouter une interface web pour saisir les données et visualiser le résultat.

## Captures
- Déposer vos captures d'écran dans `docs/screenshots` si vous souhaitez documenter la démo web.
