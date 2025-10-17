# Documentation Académique du Projet GL-Solver

## 1. Présentation Générale
Le projet **GL-Solver** implémente en Java un solveur de satisfaction de contraintes (CSP) reposant sur l’arithmétique d’intervalles. Il permet de déclarer des variables entières, d’exprimer des contraintes arithmétiques ou combinatoires (ex. all-diff) et d’explorer exhaustivement l’espace de recherche pour énumérer les solutions admissibles.

Les objectifs initiaux du refactoring 2025 étaient :
- factoriser la gestion des stratégies de recherche (before/choose/step/check) ;
- introduire une stratégie avancée `alwaysReduce` fondée sur une sauvegarde/restauration des domaines ;
- ajouter une stratégie d’optimisation pour minimiser la valeur d’une variable ;
- enrichir la couverture de tests et la démonstration CLI afin de mesurer l’impact fonctionnel et algorithmique.

## 2. Architecture Fonctionnelle

### 2.1 Couche Domaine
- `Interval` : opérations d’arithmétique (add, sub, mul, div, inter, reduce…) et représentation des domaines.
- `Variable` : étend `Interval`, ajoute la notion de nom, un observateur et des méthodes utilitaires (ex. `getFixedValue()`).
- `Constraint` & `ConstraintType` : modélisent les relations arithmétiques (ADD, MUL, DIV, DIFF, etc.).
- `Assignment`, `Problem` : respectivement instantané d’une solution et structure immuable d’un problème (copies défensives, validations).

### 2.2 Couche API & Construction
- `ISolver` : contrat public (création de variables, expressions, ajout de contraintes, stratégies, solve, configuration).
- `ProblemBuilder` : assemble un `Problem` immuable à partir des variables/contraintes et paramètres (stratégie, limite de nœuds, verbosité).

### 2.3 Couche Moteur
- `Solver` : implémentation de l’API. Maintient listes de variables/contraintes, déclenche `before/choose/step/check`, explore par récursion (`findSolutions()`), gère les stratégies (`reduceAndCheckIntervals`, `alwaysReduceStrategy`, `minimize`).
- `Reducer` : propagation des contraintes via observateurs (réduction intelligente des domaines).
- `Checker` : vérification par intervalles (détecte incohérences sans modifier les domaines).
- `Solutions` : comptage, stockage optionnel, callbacks (`setOnSolutionFound`, `setOnSolutionSnapshot`, `replaceStoredSolutions`) utilisés par la stratégie d’optimisation.

### 2.4 Couche Stratégies
- `IStrategy` : interface unifiée (before/chooseVariable/step/check/backup/restore).
- `DefaultStrategy`, `ReduceAndCheckIntervalsStrategy` : héritage progressif pour activer la propagation initiale.
- `Backup` : utilitaire de snapshots des domaines (pile de `VariableSnapshot`).
- `AlwaysReduceStrategy` : applique `Reducer.reduce()` à chaque étape, sauvegarde/restaure l’état de toutes les variables via `Backup`, coupe immédiatement les branches incohérentes.
- `OptimizationStrategy` : hérite d’`AlwaysReduceStrategy`, cible une variable objectif, enregistre chaque solution via `Solutions.setOnSolutionSnapshot()`, resserre la borne supérieure dès qu’un meilleur coût est trouvé, purifie les solutions dominées et ne conserve que les optima.

### 2.5 Démonstrations
`Main` orchestre plusieurs scénarios :
- cryptarithmes multi-niveaux ;
- comparaison d’expression avec/sans optimisation ;
- alwaysReduce vs reduceAndCheck sur les N-reines ;
- minimisation de coût d’affectation (unique optimum et cas multi-optima) ;
- streaming de solutions limité ;
- exploration progressive avec plafonds de nœuds ;
- ProblemBuilder, opérations sur intervalles, réduction de la contrainte DIFF.

## 3. Stratégies & Algorithmes

### 3.1 Stratégie `alwaysReduce`
1. sauvegarde des domaines avant chaque exploration ;
2. propagation complète (`Reducer.reduce()`) après chaque décision ;
3. restauration des domaines au retour arrière ;
4. détection immédiate des contradictions (domaine vide) pour couper la recherche.

**Impact mesuré** : sur les 8 reines, le nombre de nœuds passe de ~87k (reduceAndCheck) à < 1k (gain ≈ 99 %) sans perte de solutions.

### 3.2 Stratégie d’Optimisation
1. objectif nommé (variable) enregistré via `Solver.minimize()` ;
2. solutions explorées avec `alwaysReduceStrategy` ;
3. à chaque meilleure solution, mise à jour `bestValue` et réduction de la borne supérieure du domaine objectif ;
4. l’algorithme continue avec le domaine resserré, éliminant les solutions non optimales ;
5. seules les affectations optimales sont conservées via `Solutions.replaceStoredSolutions()`.

**Exemples** :
- Affectation 3×4 (tableau fourni) : coût minimal 15, `alwaysReduce + minimize` n’enregistre qu’une solution optimale (34 % de nœuds économisés vs recherche brute).
- Affectation 3×3 : deux optima de coût 3 détectés et affichés dans la démo CLI.

## 4. Tests & Validation

### 4.1 Tests unitaires
- `TestInterval`, `TestVariable` : cohérence des opérations, gestion des bornes, intersections vides.
- `TestProblem`, `TestProblemBuilder` : immutabilité, validations de stratégie/maxNodes.
- `TestSolutions` (via `TestSolver`) : stockage, accès, callbacks.

### 4.2 Tests d’intégration
- `TestSolver` : contraintes simples, cryptarithmes (performance), intégration des stratégies.
- `TestSolverAlwaysReduce` : 8 reines, vérifie le nombre de solutions (92) et la diminution des nœuds.
- `TestOptimizationStrategy` : affectation 3×4, garantit le respect du coût minimal et l’unicité des solutions stockées.
- `TestOptimizationComparison` : comparaisons normal/optimisé pour s’assurer de la non-régression des API historiques.

### 4.3 Exécution Automatisée
Toutes les suites sont orchestrées par Gradle (`./gradlew test`). Le pipeline repose sur JUnit 5 ; les tests de régression valident la compatibilité ascendante avant après chaque ajout.

## 5. Documentation & Diffusion

- **Démos CLI** : `Main` sert de vitrine technique, avec logs détaillés (structures, nœuds/nombre de solutions, temps, exemples).
- **Callbacks** : `Solutions.setOnSolutionSnapshot` illustre le streaming de solutions (limite configurable).
- **Exploration progressive** : `demoProgressiveSearch` montre comment ajuster dynamiquement `maxNodes` pour de grands problèmes.

## 6. Perspectives & Recommandations
1. **Extensions de stratégies** : introduire d’autres heuristiques (ex. `most-constrained-variable`, `split domain`), basées sur l’interface `IStrategy`.
2. **Optimisation avancée** : intégrer d’autres critères (maximisation, multi-objectifs) en réutilisant la pile de sauvegarde.
3. **Performance** : paralléliser l’exploration (`ForkJoinPool`) ou ajouter des heuristiques de coupure inspirées des SAT/CP modernes.
4. **Interface utilisateur** : proposer une API fluide de modélisation (DSL) ou un front-end graphique léger pour visualiser les domaines.
5. **CI/CD** : automatiser `./gradlew test` (GitLab CI), produire rapports JaCoCo pour suivre la couverture.

## 7. Conclusion
Le refactoring a transformé GL-Solver en une plateforme plus modulaire :
- la gestion des stratégies est généralisée (support de nouvelles politiques sans altérer `Solver`) ;
- la combinaison `alwaysReduce + OptimizationStrategy` prouve l’efficacité de la propagation couplée à la minimisation ;
- la documentation CLI et le code de test offrent un support pédagogique complet pour l’évaluation académique.

Le projet est prêt pour :
- des analyses de performance plus poussées ;
- l’extension des fonctionnalités de modélisation ;
- la mise en place d’un pipeline de livraison académique (CI, packaging, déploiement sur etulab).

**Auteur·rice du refactoring** : équipe Projet GL-Solver (2025).  
**Dernière mise à jour** : 17 octobre 2025.
