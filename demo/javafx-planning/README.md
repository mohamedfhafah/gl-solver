# Démo JavaFX – Planning de soirée

Prototype JavaFX qui s’appuie sur GL-Solver pour organiser une soirée optimale entre amis.

## Pré-requis
- Java 17+
- Gradle 8+
- Jar du solver : `demo/javafx-planning/libs/gl-solver.jar`

## Installation
```bash
# depuis la racine du repo
./gradlew -p demo/javafx-planning run
# ou via le script commun
./demo/launch-demo.sh javafx
```

## Utilisation
- Chaque **ligne représente un ami**, chaque **colonne une activité**.
- Les nombres indiquent l’ordre de préférence : **1 = coup de cœur**, **5 = indifférent**, **10 = à éviter**.
- Modifiez les valeurs pour adapter la soirée, puis cliquez sur **Planifier la soirée**.
- Utilisez **Solution suivante** pour faire défiler les meilleures propositions ; le compteur de nœuds rattache la performance du solver.
- Bouton **Réinitialiser** → recharge les préférences par défaut (`preferences.json`).

## Fonctionnalités visuelles
- Interface modernisée (cards, dégradé, chips de légende, animation lors de l’affichage des solutions).
- Legend intégrée rappelant l’échelle de préférences.
- Résultats présentés de façon concise avec navigation entre les solutions.

## À faire (optionnel)
- Ajouter avatars/photos pour chaque ami.
- Export textuel ou PDF du plan retenu.
- Intégrer un historique des solutions testées.

## Captures
- Ajouter vos captures d'écran dans `docs/screenshots` (optionnel).
