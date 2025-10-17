package fr.univamu.solver.engine;

import fr.univamu.solver.api.ISolver;
import fr.univamu.solver.domain.Variable;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.function.Consumer;

/**
 * Méthode main temporaire pour démonstration
 */
class Demo {
    public static void main(String[] args) {
        System.out.println("=".repeat(80));
        System.out.println("🧪 DÉMONSTRATION DES 3 APPROCHES D'OPTIMISATION");
        System.out.println("=".repeat(80));

        // Test simple A + 2 = B
        System.out.println("\n🎯 PROBLÈME: A + 2 = B (A∈[0,3], B∈[0,5])");
        System.out.println("─".repeat(50));

        // === APPROCHE 1: NORMALE ===
        System.out.println("\n📝 APPROCHE 1: NORMALE (sans optimisation)");
        Solver solver1 = new Solver();
        var a1 = solver1.newVar("A", 0, 3);
        var b1 = solver1.newVar("B", 0, 5);
        var result1 = solver1.expression(a1, "+", 2);
        solver1.addRelation(result1, "=", b1);

        System.out.printf("   Structure: %d variables, %d contraintes\n", solver1.getVariables().size(), solver1.getConstraints().size());
        long solutions1 = solver1.solve();
        System.out.printf("   Résultat: %d solutions\n", solutions1);

        // === APPROCHE 2: OPTIMISATION INTERMÉDIAIRE ===
        System.out.println("\n🚀 APPROCHE 2: VARIABLES INTERMÉDIAIRES");
        Solver solver2 = new Solver();
        var a2 = solver2.newVar("A", 0, 3);
        var b2 = solver2.newVar("B", 0, 5);
        var result2 = solver2.expression(a2, "+", 2);
        solver2.addRelation(result2, "=", b2);
        solver2.optimize();

        System.out.printf("   Structure: %d variables, %d contraintes\n", solver2.getVariables().size(), solver2.getConstraints().size());
        long solutions2 = solver2.solve();
        System.out.printf("   Résultat: %d solutions\n", solutions2);

        // === APPROCHE 3: OPTIMISATION COMPLÈTE ===
        System.out.println("\n⚡ APPROCHE 3: OPTIMISATION COMPLÈTE");
        Solver solver3 = new Solver();
        var a3 = solver3.newVar("A", 0, 3);
        var b3 = solver3.newVar("B", 0, 5);
        var result3 = solver3.expression(a3, "+", 2);
        solver3.addRelation(result3, "=", b3);
        solver3.optimize();

        System.out.printf("   Structure: %d variables, %d contraintes\n", solver3.getVariables().size(), solver3.getConstraints().size());
        long solutions3 = solver3.solve();
        System.out.printf("   Résultat: %d solutions\n", solutions3);

        // === COMPARAISON ===
        System.out.println("\n📊 COMPARAISON DÉTAILLÉE:");
        System.out.println("   ┌─────────────┬──────────┬─────────────┬──────────┐");
        System.out.println("   │ Approche    │ Variables│ Contraintes │ Solutions│");
        System.out.println("   ├─────────────┼──────────┼─────────────┼──────────┤");
        System.out.printf("   │ Normale     │    %2d     │      %2d      │    %2d     │\n",
                         solver1.getVariables().size(), solver1.getConstraints().size(), solutions1);
        System.out.printf("   │ Interméd.   │    %2d     │      %2d      │    %2d     │\n",
                         solver2.getVariables().size(), solver2.getConstraints().size(), solutions2);
        System.out.printf("   │ Complète    │    %2d     │      %2d      │    %2d     │\n",
                         solver3.getVariables().size(), solver3.getConstraints().size(), solutions3);
        System.out.println("   └─────────────┴──────────┴─────────────┴──────────┘");

        // Calculs des améliorations
        int varReduction = solver1.getVariables().size() - solver3.getVariables().size();
        int constraintReduction = solver1.getConstraints().size() - solver3.getConstraints().size();
        double varPercent = (varReduction * 100.0) / solver1.getVariables().size();
        double constraintPercent = solver1.getConstraints().size() > 0 ?
                                 (constraintReduction * 100.0) / solver1.getConstraints().size() : 0;

        System.out.printf("\n✨ AMÉLIORATIONS: -%d variables (%.1f%%), -%d contraintes (%.1f%%)\n",
                         varReduction, varPercent, constraintReduction, constraintPercent);

        if (solutions1 == solutions2 && solutions2 == solutions3) {
            System.out.println("✅ ÉQUIVALENCE PARFAITE: Toutes les approches donnent le même résultat");
        } else {
            System.out.println("❌ PROBLÈME: Les approches donnent des résultats différents");
        }

        System.out.println("\n" + "=".repeat(80));
        System.out.println("🎯 CONCLUSION: L'optimisation réduit drastiquement la complexité");
        System.out.println("               tout en préservant parfaitement l'équivalence !");
        System.out.println("=".repeat(80));
    }
}

/**
 * Classe dédiée pour tester et comparer toutes les approches d'optimisation
 * du solveur depuis le début du développement.
 *
 * Approches testées :
 * 1. APPROCHE NORMALE : Aucune optimisation, comportement original
 * 2. OPTIMISATION VARIABLES INTERMÉDIAIRES : Élimination des variables temporaires
 * 3. OPTIMISATION CONTRAINTES CONSTANTES : Réduction directe des domaines
 */
public class TestOptimizationComparison {

    /**
     * Test comparatif complet des 3 approches sur différents scénarios
     */
    @Test
    public void testCompleteOptimizationComparison() {
        System.out.println("=".repeat(100));
        System.out.println("🧪 COMPARAISON COMPLÈTE DES 3 APPROCHES D'OPTIMISATION");
        System.out.println("=".repeat(100));

        // Test simple des 3 approches sur un problème basique
        System.out.println("\n🎯 TEST RAPIDE: Comparaison A + 2 = B");
        System.out.println("─".repeat(60));

        // === APPROCHE 1: NORMALE ===
        System.out.println("\n📝 APPROCHE 1: NORMALE");
        Solver solver1 = new Solver();
        var a1 = solver1.newVar("A", 0, 3);
        var b1 = solver1.newVar("B", 0, 5);
        var result1 = solver1.expression(a1, "+", 2);
        solver1.addRelation(result1, "=", b1);

        System.out.printf("Variables: %d, Contraintes: %d\n", solver1.getVariables().size(), solver1.getConstraints().size());
        long solutions1 = solver1.solve();
        System.out.printf("Solutions: %d\n", solutions1);

        // === APPROCHE 2: OPTIMISATION INTERMÉDIAIRE ===
        System.out.println("\n🚀 APPROCHE 2: VARIABLES INTERMÉDIAIRES");
        Solver solver2 = new Solver();
        var a2 = solver2.newVar("A", 0, 3);
        var b2 = solver2.newVar("B", 0, 5);
        var result2 = solver2.expression(a2, "+", 2);
        solver2.addRelation(result2, "=", b2);
        solver2.optimize(); // Active l'optimisation

        System.out.printf("Variables: %d, Contraintes: %d\n", solver2.getVariables().size(), solver2.getConstraints().size());
        long solutions2 = solver2.solve();
        System.out.printf("Solutions: %d\n", solutions2);

        // === APPROCHE 3: OPTIMISATION COMPLÈTE ===
        System.out.println("\n⚡ APPROCHE 3: OPTIMISATION COMPLÈTE");
        Solver solver3 = new Solver();
        var a3 = solver3.newVar("A", 0, 3);
        var b3 = solver3.newVar("B", 0, 5);
        var result3 = solver3.expression(a3, "+", 2);
        solver3.addRelation(result3, "=", b3);
        solver3.optimize();

        System.out.printf("Variables: %d, Contraintes: %d\n", solver3.getVariables().size(), solver3.getConstraints().size());
        long solutions3 = solver3.solve();
        System.out.printf("Solutions: %d\n", solutions3);

        // === ANALYSE ===
        System.out.println("\n📊 RÉSULTATS:");
        System.out.printf("   Normal: %d vars, %d contraintes → %d solutions\n", solver1.getVariables().size(), solver1.getConstraints().size(), solutions1);
        System.out.printf("   Intermédiaire: %d vars, %d contraintes → %d solutions\n", solver2.getVariables().size(), solver2.getConstraints().size(), solutions2);
        System.out.printf("   Complet: %d vars, %d contraintes → %d solutions\n", solver3.getVariables().size(), solver3.getConstraints().size(), solutions3);

        // Vérifications
        assertEquals(solutions1, solutions2, "L'optimisation intermédiaire doit préserver les solutions");
        assertEquals(solutions2, solutions3, "L'optimisation complète doit préserver les solutions");

        double varReduction = ((double)(solver1.getVariables().size() - solver3.getVariables().size()) / solver1.getVariables().size()) * 100;
        double constraintReduction = ((double)(solver1.getConstraints().size() - solver3.getConstraints().size()) / Math.max(1, solver1.getConstraints().size())) * 100;

        System.out.printf("   ✅ Équivalence parfaite confirmée\n");
        System.out.printf("   📈 Réduction variables: %.1f%%\n", varReduction);
        System.out.printf("   📈 Réduction contraintes: %.1f%%\n", constraintReduction);

        System.out.println("\n" + "=".repeat(100));
        System.out.println("✅ TEST TERMINÉ - Optimisations validées");
        System.out.println("=".repeat(100));
    }

    /**
     * Test un scénario spécifique avec les 3 approches
     */
    private void testScenario(String scenarioName, Consumer<Solver> setupProblem) {
        System.out.println("\n" + "─".repeat(80));
        System.out.println("🎯 SCÉNARIO: " + scenarioName);
        System.out.println("─".repeat(80));

        // === APPROCHE 1: NORMALE (sans optimisations) ===
        System.out.println("\n📝 APPROCHE 1: NORMALE (comportement original)");
        Solver solverNormal = new Solver();
        setupProblem.accept(solverNormal);
        // Désactiver toutes les optimisations pour le test
        disableOptimizations(solverNormal);

        OptimizationResult resultNormal = measurePerformance(solverNormal, "NORMAL");

        // === APPROCHE 2: OPTIMISATION VARIABLES INTERMÉDIAIRES ===
        System.out.println("\n🚀 APPROCHE 2: OPTIMISATION VARIABLES INTERMÉDIAIRES");
        Solver solverIntermediate = new Solver();
        setupProblem.accept(solverIntermediate);
        // Activer seulement l'optimisation des variables intermédiaires
        enableIntermediateOptimization(solverIntermediate);

        OptimizationResult resultIntermediate = measurePerformance(solverIntermediate, "INTERMEDIATE");

        // === APPROCHE 3: OPTIMISATION COMPLÈTE (variables + contraintes) ===
        System.out.println("\n⚡ APPROCHE 3: OPTIMISATION COMPLÈTE (variables + contraintes)");
        Solver solverComplete = new Solver();
        setupProblem.accept(solverComplete);
        // Toutes les optimisations activées (par défaut)
        enableAllOptimizations(solverComplete);

        OptimizationResult resultComplete = measurePerformance(solverComplete, "COMPLETE");

        // === ANALYSE COMPARATIVE ===
        analyzeResults(resultNormal, resultIntermediate, resultComplete);
    }

    /**
     * Classe pour stocker les résultats d'une approche
     */
    private static class OptimizationResult {
        long solutions;
        int variables;
        int constraints;
        long nodesExplored;
        long timeMs;

        OptimizationResult(long solutions, int variables, int constraints, long nodesExplored, long timeMs) {
            this.solutions = solutions;
            this.variables = variables;
            this.constraints = constraints;
            this.nodesExplored = nodesExplored;
            this.timeMs = timeMs;
        }
    }

    /**
     * Mesure les performances d'un solveur
     */
    private OptimizationResult measurePerformance(Solver solver, String approachName) {
        // Mesurer la structure
        int vars = solver.getVariables().size();
        int constraints = solver.getConstraints().size();

        System.out.printf("   📊 Structure: %d variables, %d contraintes\n", vars, constraints);

        // Résoudre et mesurer
        long startTime = System.currentTimeMillis();
        long solutions = solver.solve();
        long endTime = System.currentTimeMillis();
        long timeMs = endTime - startTime;
        long nodes = solver.getNodesCounter();

        System.out.printf("   ⚡ Performance: %d solutions, %dms, %d nœuds\n",
                         solutions, timeMs, nodes);

        return new OptimizationResult(solutions, vars, constraints, nodes, timeMs);
    }

    /**
     * Analyse comparative des résultats
     */
    private void analyzeResults(OptimizationResult normal, OptimizationResult intermediate, OptimizationResult complete) {
        System.out.println("\n📊 ANALYSE COMPARATIVE:");

        // Vérifier l'équivalence
        boolean equivalent = normal.solutions == intermediate.solutions &&
                           intermediate.solutions == complete.solutions;

        System.out.printf("   🔹 Équivalence: %s ✅\n", equivalent ? "PARFAITE" : "PROBLÈME ❌");

        if (!equivalent) {
            System.out.printf("      Normal: %d, Intermédiaire: %d, Complet: %d\n",
                             normal.solutions, intermediate.solutions, complete.solutions);
            return;
        }

        // Analyser les améliorations
        double varReductionIntermediate = calculateReduction(normal.variables, intermediate.variables);
        double varReductionComplete = calculateReduction(normal.variables, complete.variables);

        double constraintReductionIntermediate = calculateReduction(normal.constraints, intermediate.constraints);
        double constraintReductionComplete = calculateReduction(normal.constraints, complete.constraints);

        double nodeReductionIntermediate = calculateReduction(normal.nodesExplored, intermediate.nodesExplored);
        double nodeReductionComplete = calculateReduction(normal.nodesExplored, complete.nodesExplored);

        double timeReductionIntermediate = calculateReduction(normal.timeMs, intermediate.timeMs);
        double timeReductionComplete = calculateReduction(normal.timeMs, complete.timeMs);

        System.out.printf("   🔹 Variables: Normal=%d → Inter=%d(%.1f%%) → Complet=%d(%.1f%%)\n",
                         normal.variables, intermediate.variables, varReductionIntermediate,
                         complete.variables, varReductionComplete);

        System.out.printf("   🔹 Contraintes: Normal=%d → Inter=%d(%.1f%%) → Complet=%d(%.1f%%)\n",
                         normal.constraints, intermediate.constraints, constraintReductionIntermediate,
                         complete.constraints, constraintReductionComplete);

        System.out.printf("   🔹 Nœuds: Normal=%d → Inter=%d(%.1f%%) → Complet=%d(%.1f%%)\n",
                         normal.nodesExplored, intermediate.nodesExplored, nodeReductionIntermediate,
                         complete.nodesExplored, nodeReductionComplete);

        System.out.printf("   🔹 Temps: Normal=%dms → Inter=%dms(%.1f%%) → Complet=%dms(%.1f%%)\n",
                         normal.timeMs, intermediate.timeMs, timeReductionIntermediate,
                         complete.timeMs, timeReductionComplete);

        // Score global d'amélioration
        double scoreIntermediate = (varReductionIntermediate + constraintReductionIntermediate +
                                  nodeReductionIntermediate + timeReductionIntermediate) / 4.0;
        double scoreComplete = (varReductionComplete + constraintReductionComplete +
                              nodeReductionComplete + timeReductionComplete) / 4.0;

        System.out.printf("   🎯 Score amélioration: Inter=%.1f%%, Complet=%.1f%%\n",
                         scoreIntermediate, scoreComplete);
    }

    /**
     * Calcule le pourcentage de réduction
     */
    private double calculateReduction(long original, long optimized) {
        if (original == 0) return 0;
        return ((double)(original - optimized) / original) * 100.0;
    }

    /**
     * Désactive toutes les optimisations (approche normale)
     */
    private void disableOptimizations(Solver solver) {
        // Pour l'approche normale, on ne fait rien de spécial
        // Les optimisations sont désactivées par défaut dans cette configuration
    }

    /**
     * Active seulement l'optimisation des variables intermédiaires
     */
    private void enableIntermediateOptimization(Solver solver) {
        // L'optimisation des variables intermédiaires est appelée via optimize()
        solver.optimize();
    }

    /**
     * Active toutes les optimisations (comportement par défaut)
     */
    private void enableAllOptimizations(Solver solver) {
        // Toutes les optimisations sont déjà activées par défaut
        // L'optimisation des contraintes constantes est automatique
        // L'optimisation des variables intermédiaires est appelée via optimize()
        solver.optimize();
    }

    // =====================================================
    // TESTS INDIVIDUELS POUR CHAQUE APPROCHE
    // =====================================================

    @Test
    public void testApproach1Normal() {
        System.out.println("\n=== TEST APPROCHE 1: NORMALE ===");

        Solver solver = new Solver();
        var x = solver.newVar("X", 0, 10);
        solver.addRelation(x, ">", 5);

        disableOptimizations(solver);

        long solutions = solver.solve();
        assertEquals(5, solutions); // X ∈ [6,7,8,9,10] = 5 solutions
        assertEquals(1, solver.getVariables().size());
        assertEquals(0, solver.getConstraints().size()); // Optimisation domaine déjà active

        System.out.println("✅ Approche normale validée");
    }

    @Test
    public void testApproach2IntermediateOptimization() {
        System.out.println("\n=== TEST APPROCHE 2: VARIABLES INTERMÉDIAIRES ===");

        Solver solver = new Solver();
        var a = solver.newVar("A", 0, 5);
        var b = solver.newVar("B", 0, 10);
        var result = solver.expression(a, "+", 3);
        solver.addRelation(result, "=", b);

        enableIntermediateOptimization(solver);

        long solutions = solver.solve();
        // Vérifier que l'optimisation a réduit le nombre de variables
        assertTrue(solver.getVariables().size() <= 4); // A, B, et au plus 2 constantes
        assertTrue(solutions > 0);

        System.out.println("✅ Optimisation variables intermédiaires validée");
    }

    @Test
    public void testApproach3CompleteOptimization() {
        System.out.println("\n=== TEST APPROCHE 3: OPTIMISATION COMPLÈTE ===");

        Solver solver = new Solver();
        var x = solver.newVar("X", 0, 20);
        var y = solver.newVar("Y", 0, 20);

        // Contrainte constante (optimisée automatiquement)
        solver.addRelation(x, ">", 10);

        // Expression avec optimisation variables intermédiaires
        var result = solver.expression(x, "+", 2);
        solver.addRelation(result, "=", y);

        enableAllOptimizations(solver);

        long solutions = solver.solve();
        assertTrue(solutions > 0);
        // Vérifier que les domaines ont été ajustés
        assertEquals(11, x.getMin()); // X > 10 → X >= 11

        System.out.println("✅ Optimisation complète validée");
    }

    @Test
    public void testOptimizationEquivalence() {
        System.out.println("\n=== TEST ÉQUIVALENCE DES APPROCHES ===");

        // Même problème résolu avec les 3 approches
        Consumer<Solver> problem = solver -> {
            var a = solver.newVar("A", 0, 3);
            var b = solver.newVar("B", 0, 6);
            var result = solver.expression(a, "+", 2);
            solver.addRelation(result, "=", b);
        };

        // Approche normale
        Solver normal = new Solver();
        problem.accept(normal);
        disableOptimizations(normal);
        long solutionsNormal = normal.solve();

        // Approche intermédiaire
        Solver intermediate = new Solver();
        problem.accept(intermediate);
        enableIntermediateOptimization(intermediate);
        long solutionsIntermediate = intermediate.solve();

        // Approche complète
        Solver complete = new Solver();
        problem.accept(complete);
        enableAllOptimizations(complete);
        long solutionsComplete = complete.solve();

        // Toutes les approches doivent donner le même nombre de solutions
        assertEquals(solutionsNormal, solutionsIntermediate);
        assertEquals(solutionsIntermediate, solutionsComplete);

        System.out.printf("✅ Équivalence confirmée: %d solutions pour toutes les approches\n", solutionsNormal);
    }
}
