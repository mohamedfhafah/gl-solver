# Plan de Travail – Phase « alwaysReduce » & Optimisation

## Légende des statuts
- ☐ À faire
- ◐ En cours
- ☑ Fait

> Mettre à jour le statut et ajouter la date/initiales dans la colonne « Notes » à chaque avancement.

---

## 1. Stratégie alwaysReduce (obligatoire)
| Tâche | Détails & impacts principaux | Fichiers pressentis | Statut | Notes |
|-------|------------------------------|----------------------|--------|-------|
| Définir `Backup` | Sauvegarde/restauration des domaines variables pour supporter des réductions multiples par décision. | `fr/univamu/solver/strategy/Backup.java` (nouveau) | ☑ | 2025-10-17 Codex |
| Étendre `IStrategy` | Ajouter `backup()`/`restore()` (et toute méthode nécessaire) + ajuster implémentations existantes. | `strategy/IStrategy.java`, `strategy/DefaultStrategy.java`, `strategy/ReduceAndCheckIntervalsStrategy.java` | ☑ | 2025-10-17 Codex |
| Implémenter `AlwaysReduceStrategy` | Stratégie dédiée qui applique `Reducer.reduce()` à chaque étape en s’appuyant sur `Backup`. | `strategy/AlwaysReduceStrategy.java` (nouveau) | ☑ | 2025-10-17 Codex (tests KO : JVM manquante) |
| Adapter `Solver` | Intégrer `alwaysReduceStrategy()`, orchestrer backup/restore dans `findSolutions`, brancher `Reducer`. | `engine/Solver.java` | ☑ | 2025-10-17 Codex (tests KO : JVM manquante) |
| Supporter la nouvelle stratégie dans l’API immuable | Ajouter la constante de stratégie et la validation associée. | `domain/Problem.java`, `api/ProblemBuilder.java` | ☑ | 2025-10-17 Codex (tests KO : JVM manquante) |
| Exposer la stratégie dans l’API publique | Déclarer `alwaysReduceStrategy()` dans `ISolver` et implémentations. | `api/ISolver.java`, `engine/Solver.java` | ☑ | 2025-10-17 Codex (tests KO : JVM manquante) |
| Couvrir par tests ciblés | Ajouter un test N-reines (8) vérifiant nb solutions = 92 et gain de nœuds vs défaut. | `test/.../TestSolverAlwaysReduce.java` (nouveau) | ☐ | |
| Mettre à jour tests existants | Adapter tests unitaires pour accepter la 3ᵉ stratégie (Problem, autres si nécessaires). | `test/domain/TestProblem.java`, autres fichiers si impacts | ☑ | 2025-10-17 Codex (tests KO : JVM manquante) |
| Documenter la fonctionnalité | Ajouter un paragraphe dans `PROJECT_ANALYSIS.md` ou `README.md` sur la stratégie. | `PROJECT_ANALYSIS.md`, `README.md` | ☐ | |

---

## 2. Stratégie d’optimisation (optionnelle mais conseillée)
| Tâche | Détails & impacts principaux | Fichiers pressentis | Statut | Notes |
|-------|------------------------------|----------------------|--------|-------|
| Créer `OptimizationStrategy` | Hérite de `AlwaysReduceStrategy`, connait la variable objectif et la meilleure valeur courante. | `strategy/OptimizationStrategy.java` (nouveau) | ☐ | |
| Adapter `Solver` pour la minimisation | Méthode `minimize(Variable)` ou équivalent + hookups (callbacks solutions, filtrage). | `engine/Solver.java`, `engine/Solutions.java` | ☐ | |
| Bloquer solutions non optimales | Réduire le domaine objectif après chaque solution & purger les solutions dominées. | `strategy/OptimizationStrategy.java`, `engine/Solutions.java` | ☐ | |
| Couvrir par test Assignment | Implémenter `buildAssignmentProblem` + test JUnit validant la solution minimale. | `test/.../TestOptimizationStrategy.java` (nouveau) | ☐ | |
| Démonstration dans Main (option) | Ajouter un scénario CLI pour illustrer la minimisation. | `Main.java` | ☐ | |

---

## 3. Intégration & qualité
| Tâche | Détails & impacts principaux | Fichiers pressentis | Statut | Notes |
|-------|------------------------------|----------------------|--------|-------|
| Vérifier compatibilité rétro | S’assurer que les stratégies existantes restent inchangées (tests régression). | Tests existants | ☐ | |
| Mettre à jour l’analyse projet | Compléter `PROJECT_ANALYSIS.md` avec les nouvelles briques et résultats de perf/tests. | `PROJECT_ANALYSIS.md` | ☐ | |
| Préparer plan de commits | Définir une séquence de commits logique (analyse → alwaysReduce → optimisation). | Journal interne | ☐ | |

---

## 4. Suivi d’exécution
- Après chaque tâche terminée : cocher ☑ et noter la date.
- Lancer `./gradlew test` avant chaque commit pour garantir la non-régression.
- Prévoir un commit dédié par grande brique (Stratégie alwaysReduce, Tests, Optimisation…).
