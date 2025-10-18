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
Test API (avec matrice personnalisée) :
```bash
curl -X POST http://localhost:8080/planifier \
     -H "Content-Type: application/json" \
     -d '{"friends":["Alice","Bruno"],"activities":["Cinéma","Escape"],"preferences":{"Alice":{"Cinéma":1,"Escape":5},"Bruno":{"Cinéma":4,"Escape":1}}}'
```

## Fonctionnement
- `SolverService` centralise la construction du problème (amis vs activités).
- Deux objectifs sont proposés : coût minimal et solution équilibrée (écarts plus homogènes).
- Une sortie commune (activité unique) est également suggérée (top 3 des activités partagées).
- La réponse JSON inclut affectation, coûts, nœuds explorés, et détails par activité.

## UI Web
- Étapes guidées : création du groupe, choix des activités, saisie des préférences (1 = adoré ; 10 = à éviter).
- Table dynamique avec sliders interactifs pour chaque couple ami/activité.
- Cartes animées présentant les plans individuels, la solution équilibrée et le podium des sorties communes.
- Historique des requêtes et jauge des nœuds explorés.

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
