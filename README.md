# Solver

## Présentation

Projet universitaire de programmation par contraintes (CSP) en Java.
Le solveur s’appuie sur l’arithmétique d’intervalles et propose plusieurs stratégies de recherche :
- stratégie par défaut (vérification par intervalles) ;
- stratégie `reduceAndCheckIntervals` (propagation avant exploration) ;
- stratégie `alwaysReduce` pour une propagation systématique ;
- stratégie d’optimisation pour la minimisation d’une variable objectif.

## Membres du projet

- Mohamed Fhafah / F22021762 / mohamed.fhafah@etu.univ-amu.fr

## Description des emprunts

- Utilisation de ChatGPT/Cursor pour les classes : `Main.java`, `Solver.java`, `OptimizationStrategy.java`, `Solutions.java`, `TestSolverAlwaysReduce.java`, `TestOptimizationStrategy.java`
- Utilisation de ChatGPT pour la documentation : `DOCUMENTATION_ACADEMIQUE.md`

## Commandes utiles

- Lancer tous les tests : `./gradlew test`
- Générer le rapport de couverture : `./gradlew jacocoTestReport` (puis ouvrir `build/reports/jacoco/test/html/index.html`)
- Exécuter la démonstration CLI : `./gradlew run`

## Documentation

- Documentation académique détaillée : `DOCUMENTATION_ACADEMIQUE.md`
- Rapport de couverture : `build/reports/jacoco/test/html/index.html`
- Rapport de tests JUnit : `build/reports/tests/test/index.html`
