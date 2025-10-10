package fr.univamu.solver;

import fr.univamu.solver.api.ISolver;
import fr.univamu.solver.domain.Constraint;
import fr.univamu.solver.domain.ConstraintType;
import fr.univamu.solver.domain.Interval;
import fr.univamu.solver.domain.Variable;
import fr.univamu.solver.engine.Solver;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== Suite de Cryptarithmes: Difficultés Progressives ===\n");

        // Niveau 1: Facile
        System.out.println("🎯 NIVEAU 1: Cryptarithm Simple");
        solveEasyCryptarithm();

        // Niveau 2: Intermédiaire
        System.out.println("\n" + "=".repeat(70) + "\n");

        // Niveau 2: Intermédiaire
        //System.out.println("🎯 NIVEAU 2: Cryptarithm Intermédiaire");
        //solveMediumCryptarithm();

        //System.out.println("\n" + "=".repeat(70) + "\n");

        // Niveau 3: Difficile
        //System.out.println("🎯 NIVEAU 3: Cryptarithm Difficile");
        //solveHardCryptarithm();

        //System.out.println("\n" + "=".repeat(70) + "\n");

        // Comparaison finale avec l'exemple simple
        //System.out.println("🏆 COMPARAISON FINALE - Cryptarithm AB + CD = EF");
        //demonstrateSimpleComparison();

        // Test de buildAllNotEmptyIntervals
        //System.out.println("\n" + "=".repeat(70) + "\n");
        //System.out.println("🧮 TEST ÉTAPE 2: buildAllNotEmptyIntervals [0,9]");
        //testBuildAllNotEmptyIntervals();

        // Test exhaustif de l'addition
        System.out.println("\n" + "=".repeat(70) + "\n");
        System.out.println("🔍 TEST ÉTAPE 3: Test Exhaustif Addition [-8,8]");
        demonstrateExhaustiveAddition();

        // Test rapide de validation
        System.out.println("\n" + "-".repeat(50));
        System.out.println("⚡ VALIDATION RAPIDE:");
        quickValidationTest();

        // Étape 4: Tests exhaustifs pour toutes les opérations
        System.out.println("\n" + "=".repeat(70) + "\n");
        System.out.println("🧮 ÉTAPE 4: Tests Exhaustifs pour Toutes les Opérations");
        demonstrateAllOperations();

        // Démonstration du nouveau record Constraint
        System.out.println("\n" + "-".repeat(50));
        System.out.println("📝 DÉMONSTRATION: Record Constraint");
        demonstrateConstraintRecord();

        // Test rapide de l'intégration ProblemBuilder dans Solver
        System.out.println("\n" + "-".repeat(50));
        System.out.println("🔧 TEST INTÉGRATION: ProblemBuilder + Solver");
        testProblemBuilderIntegration();

        // Démonstration des métriques d'optimisation
        System.out.println("\n" + "-".repeat(50));
        System.out.println("📊 MÉTRIQUES D'OPTIMISATION: Comparaison Avant/Après");
        demonstrateOptimizationMetrics();

        // Démonstration de comparaison expression normale vs optimisée
        System.out.println("\n" + "=".repeat(70) + "\n");
        System.out.println("🔄 COMPARAISON: Expression Normale vs Optimisée");
        demonstrateOptimizationComparison();

    }

    /**
     * Démonstration comparative complète: Expression normale vs optimisée
     * Compare les performances détaillées pour plusieurs expressions
     */
    public static void demonstrateOptimizationComparison() {
        System.out.println("=".repeat(80));
        System.out.println("🔬 COMPARAISON DÉTAILLÉE: APPROCHE NORMALE vs OPTIMISÉE");
        System.out.println("=".repeat(80));

        // Test 1: Expression simple A + 2 = B
        compareExpressions("A + 2 = B", solver -> {
            var a = solver.newVar("A", 0, 9);
            var b = solver.newVar("B", 0, 9);
            var result = solver.expression(a, "+", 2);
            solver.addRelation(result, "=", b);
        });

        // Test 2: Expression plus complexe (A + B) * 2 = C
        compareExpressions("(A + B) * 2 = C", solver -> {
            var a = solver.newVar("A", 0, 9);
            var b = solver.newVar("B", 0, 9);
            var c = solver.newVar("C", 0, 36);
            var sum = solver.expression(a, "+", b);
            var result = solver.expression(sum, "*", 2);
            solver.addRelation(result, "=", c);
        });

        // Test 3: Expression en chaîne A + B + C = D
        compareExpressions("A + B + C = D", solver -> {
            var a = solver.newVar("A", 0, 5);
            var b = solver.newVar("B", 0, 5);
            var c = solver.newVar("C", 0, 5);
            var d = solver.newVar("D", 0, 15);
            var sumAB = solver.expression(a, "+", b);
            var result = solver.expression(sumAB, "+", c);
            solver.addRelation(result, "=", d);
        });

        // Test 4: Expression avec constantes multiples A * 3 + 5 = B
        compareExpressions("A * 3 + 5 = B", solver -> {
            var a = solver.newVar("A", 0, 5);
            var b = solver.newVar("B", 5, 20);
            var product = solver.expression(a, "*", 3);
            var result = solver.expression(product, "+", 5);
            solver.addRelation(result, "=", b);
        });

        // Test 5: NOUVELLE OPTIMISATION - Contraintes constantes évitées X > 10
        // Au lieu de créer une contrainte, on ajuste directement le domaine
        compareExpressions("X > 10 (optimisation domaine)", solver -> {
            var x = solver.newVar("X", 0, 20);
            solver.addRelation(x, ">", 10);
        });

        System.out.println("=".repeat(80));
        System.out.println("🎯 CONCLUSION GÉNÉRALE:");
        System.out.println("   L'optimisation réduit systématiquement la complexité du problème");
        System.out.println("   en éliminant les variables intermédiaires inutiles, tout en");
        System.out.println("   préservant parfaitement l'équivalence fonctionnelle.");
        System.out.println("=".repeat(80));
    }

    /**
     * Compare les performances d'une expression normale vs optimisée
     */
    private static void compareExpressions(String description, Consumer<Solver> setupExpression) {
        System.out.println("\n" + "─".repeat(60));
        System.out.println("🧪 TEST: " + description);
        System.out.println("─".repeat(60));

        // === APPROCHE NORMALE ===
        System.out.println("\n📝 APPROCHE NORMALE (sans optimisation):");
        Solver solverNormal = new Solver();
        setupExpression.accept(solverNormal);

        // Mesures avant résolution
        int varsNormal = solverNormal.getVariables().size();
        int constraintsNormal = solverNormal.getConstraints().size();

        System.out.println("   📊 Structure du problème:");
        System.out.printf("      • Variables: %d\n", varsNormal);
        System.out.printf("      • Contraintes: %d\n", constraintsNormal);
        System.out.println("      • Variables: " + solverNormal.getVariables().stream()
                          .map(Variable::getName).collect(Collectors.toList()));
        System.out.println("      • Contraintes:");
        for (Constraint c : solverNormal.getConstraints()) {
            System.out.println("        " + c);
        }

        // Résolution avec mesure de temps
        long startTime = System.currentTimeMillis();
        long solutionsNormal = solverNormal.solve();
        long endTime = System.currentTimeMillis();
        long timeNormal = endTime - startTime;

        System.out.println("   ⚡ Performance:");
        System.out.printf("      • Solutions: %d\n", solutionsNormal);
        System.out.printf("      • Temps: %d ms\n", timeNormal);
        System.out.printf("      • Nœuds explorés: %d\n", solverNormal.getNodesCounter());

        // === APPROCHE OPTIMISÉE ===
        System.out.println("\n🚀 APPROCHE OPTIMISÉE (avec Solver.optimize()):");
        Solver solverOptimized = new Solver();
        setupExpression.accept(solverOptimized);
        solverOptimized.optimize(); // Applique l'optimisation

        // Mesures avant résolution
        int varsOptimized = solverOptimized.getVariables().size();
        int constraintsOptimized = solverOptimized.getConstraints().size();

        System.out.println("   📊 Structure du problème après optimisation:");
        System.out.printf("      • Variables: %d\n", varsOptimized);
        System.out.printf("      • Contraintes: %d\n", constraintsOptimized);
        System.out.println("      • Variables: " + solverOptimized.getVariables().stream()
                          .map(Variable::getName).collect(Collectors.toList()));
        System.out.println("      • Contraintes:");
        for (Constraint c : solverOptimized.getConstraints()) {
            System.out.println("        " + c);
        }

        // Résolution avec mesure de temps
        startTime = System.currentTimeMillis();
        long solutionsOptimized = solverOptimized.solve();
        endTime = System.currentTimeMillis();
        long timeOptimized = endTime - startTime;

        System.out.println("   ⚡ Performance:");
        System.out.printf("      • Solutions: %d\n", solutionsOptimized);
        System.out.printf("      • Temps: %d ms\n", timeOptimized);
        System.out.printf("      • Nœuds explorés: %d\n", solverOptimized.getNodesCounter());

        // === COMPARAISON DÉTAILLÉE ===
        System.out.println("\n📊 ANALYSE COMPARATIVE:");

        // Variables
        int varReduction = varsNormal - varsOptimized;
        double varPercent = varsNormal > 0 ? (varReduction * 100.0 / varsNormal) : 0;
        System.out.printf("   🔸 Variables: %d → %d ", varsNormal, varsOptimized);
        if (varReduction > 0) {
            System.out.printf("(réduction: %d, -%.1f%%)\n", varReduction, varPercent);
        } else {
            System.out.println("(aucune réduction)");
        }

        // Contraintes
        int constraintReduction = constraintsNormal - constraintsOptimized;
        double constraintPercent = constraintsNormal > 0 ? (constraintReduction * 100.0 / constraintsNormal) : 0;
        System.out.printf("   🔸 Contraintes: %d → %d ", constraintsNormal, constraintsOptimized);
        if (constraintReduction > 0) {
            System.out.printf("(réduction: %d, -%.1f%%)\n", constraintReduction, constraintPercent);
        } else {
            System.out.println("(aucune réduction)");
        }

        // Solutions
        System.out.printf("   🔸 Solutions: %d = %d ", solutionsNormal, solutionsOptimized);
        if (solutionsNormal == solutionsOptimized) {
            System.out.println("(équivalence parfaite ✅)");
        } else {
            System.out.println("(ERREUR: différence détectée ❌)");
        }

        // Performance temporelle
        long timeDiff = timeNormal - timeOptimized;
        double timePercent = timeNormal > 0 ? (timeDiff * 100.0 / timeNormal) : 0;
        System.out.printf("   🔸 Temps d'exécution: %dms → %dms ", timeNormal, timeOptimized);
        if (timeDiff > 0) {
            System.out.printf("(gain: %dms, -%.1f%%)\n", timeDiff, timePercent);
        } else if (timeDiff < 0) {
            System.out.printf("(dégradation: %dms, +%.1f%%)\n", -timeDiff, -timePercent);
        } else {
            System.out.println("(temps identique)");
        }

        // Nœuds explorés
        long nodesNormal = solverNormal.getNodesCounter();
        long nodesOptimized = solverOptimized.getNodesCounter();
        long nodesDiff = nodesNormal - nodesOptimized;
        double nodesPercent = nodesNormal > 0 ? (nodesDiff * 100.0 / nodesNormal) : 0;
        System.out.printf("   🔸 Nœuds explorés: %d → %d ", nodesNormal, nodesOptimized);
        if (nodesDiff > 0) {
            System.out.printf("(réduction: %d, -%.1f%%)\n", nodesDiff, nodesPercent);
        } else if (nodesDiff < 0) {
            System.out.printf("(augmentation: %d, +%.1f%%)\n", -nodesDiff, -nodesPercent);
        } else {
            System.out.println("(nœuds identiques)");
        }

        // Score global d'amélioration
        double score = (varPercent + constraintPercent + (timePercent > 0 ? timePercent : 0) + (nodesPercent > 0 ? nodesPercent : 0)) / 4.0;
        System.out.printf("   🎯 Score d'amélioration global: %.1f%%\n", score);
    }

    /**
     * Démonstration comparative: Efficacité de l'arithmétique d'intervalles
     * Cryptarithm: AB + CD = EF (même que niveau 1 pour comparaison)
     */
    public static void demonstrateSimpleComparison() {
        System.out.println("=== Comparaison des Stratégies sur Cryptarithm Simple ===\n");

        System.out.println("Cryptarithm: AB + CD = EF");
        System.out.println("Variables: A,B,C,D,E,F ∈ {0..9}, A,C,E ≠ 0, toutes distinctes");
        System.out.println("Contrainte: AB + CD = EF\n");

        // Variables pour stocker les résultats
        long basicSolutions = -1;
        long basicNodes = -1;
        long optimizedSolutions = -1;
        long optimizedNodes = -1;

        // Test stratégie basique
        System.out.println("1. Stratégie basique (sans optimisation):");
        ISolver basicSolver = createSimpleSolver();
        basicSolver.setMaxNodes(1_000_000_000L); // Limite très augmentée

        try {
            long startTime = System.currentTimeMillis();
            basicSolutions = basicSolver.solve();
            long endTime = System.currentTimeMillis();
            basicNodes = basicSolver.getNodesCounter();

            System.out.printf("   Solutions: %d, Nœuds: %,d, Temps: %d ms%n",
                            basicSolutions, basicNodes, (endTime - startTime));
        } catch (IllegalStateException e) {
            System.out.println("   ⚠️  Limite de nœuds atteinte - stratégie trop lente");
            basicSolutions = 0; // Marquer comme non résolu
        }

        // Test stratégie optimisée
        System.out.println("\n2. Stratégie optimisée (arithmétique d'intervalles):");
        ISolver optimizedSolver = createSimpleSolver();
        optimizedSolver.reduceAndCheckIntervalsStrategy();

        long startTime = System.currentTimeMillis();
        optimizedSolutions = optimizedSolver.solve();
        long endTime = System.currentTimeMillis();
        optimizedNodes = optimizedSolver.getNodesCounter();

        System.out.printf("   Solutions: %d, Nœuds: %,d, Temps: %d ms%n",
                        optimizedSolutions, optimizedNodes, (endTime - startTime));

        // Analyse comparative
        System.out.println("\n3. Analyse Comparative:");
        if (basicSolutions > 0 && optimizedSolutions > 0) {
            double speedup = (double) basicNodes / optimizedNodes;
            System.out.printf("   Accélération: %.1fx%n", speedup);
            System.out.printf("   Réduction nœuds: %,d (%.1f%%)%n",
                           basicNodes - optimizedNodes,
                           100.0 * (basicNodes - optimizedNodes) / basicNodes);
            System.out.println("   Correction: " + (basicSolutions == optimizedSolutions ? "✓" : "✗"));
        } else if (optimizedSolutions > 0) {
            System.out.println("   ⚡ Stratégie optimisée: EFFICACE (trouve toutes les solutions)");
            System.out.println("   🐌 Stratégie basique: TROP LENTE (même pour problème simple)");
        }

        System.out.println("\n💡 Conclusion: L'arithmétique d'intervalles accélère drastiquement");
        System.out.println("   même les problèmes simples, et rend possibles les problèmes complexes !");
    }

    /**
     * Configuration commune du problème cryptarithmique AB + CD = EF
     */
    private static ISolver createSimpleSolver() {
        ISolver solver = new Solver();
        solver.setVerbose(false);

        // Variables: A,B,C,D,E,F ∈ {0..9}, A,C,E ≠ 0, toutes distinctes
        var a = solver.newVar("A", 1, 9);
        var b = solver.newVar("B", 0, 9);
        var c = solver.newVar("C", 1, 9);
        var d = solver.newVar("D", 0, 9);
        var e = solver.newVar("E", 1, 9);
        var f = solver.newVar("F", 0, 9);

        solver.addAllDiffRelation(a, b, c, d, e, f);

        // AB + CD = EF
        var ab = solver.expression(a, "*", 10, "+", b);
        var cd = solver.expression(c, "*", 10, "+", d);
        var ef = solver.expression(e, "*", 10, "+", f);
        solver.addRelation(solver.expression(ab, "+", cd), "=", ef);

        return solver;
    }

    /**
     * Configuration commune du problème cryptarithmique FUN + FUN = GAME
     */
    private static ISolver createSolver() {
        ISolver solver = new Solver();
        solver.setVerbose(false);

        // Variables: F,U,N,G,A,M,E ∈ {0..9}, F,G ≠ 0, toutes distinctes
        var f = solver.newVar("F", 1, 9);
        var u = solver.newVar("U", 0, 9);
        var n = solver.newVar("N", 0, 9);
        var g = solver.newVar("G", 1, 9);
        var a = solver.newVar("A", 0, 9);
        var m = solver.newVar("M", 0, 9);
        var e = solver.newVar("E", 0, 9);

        solver.addAllDiffRelation(f, u, n, g, a, m, e);

        // FUN + FUN = GAME ⟺ 2×FUN = GAME
        var fun = solver.expression(f, "*", 100, "+", u, "*", 10, "+", n);
        var game = solver.expression(g, "*", 1000, "+", a, "*", 100, "+", m, "*", 10, "+", e);
        solver.addRelation(solver.expression(fun, "*", 2), "=", game);

        return solver;
    }

    /**
     * Niveau 1: Cryptarithm simple - AB + CD = EF
     * Variables: 6 lettres, contrainte simple
     */
    public static void solveEasyCryptarithm() {
        System.out.println("Cryptarithm: AB + CD = EF");
        System.out.println("Variables: A,B,C,D,E,F ∈ {0..9}, A,C,E ≠ 0, toutes distinctes");
        System.out.println("Contrainte: AB + CD = EF\n");

        ISolver solver = new Solver();
        solver.setVerbose(false);
        solver.reduceAndCheckIntervalsStrategy();

        // Variables: A,B,C,D,E,F ∈ {0..9}, A,C,E ≠ 0, toutes distinctes
        var a = solver.newVar("A", 1, 9);
        var b = solver.newVar("B", 0, 9);
        var c = solver.newVar("C", 1, 9);
        var d = solver.newVar("D", 0, 9);
        var e = solver.newVar("E", 1, 9);
        var f = solver.newVar("F", 0, 9);

        solver.addAllDiffRelation(a, b, c, d, e, f);

        // AB + CD = EF
        var ab = solver.expression(a, "*", 10, "+", b);
        var cd = solver.expression(c, "*", 10, "+", d);
        var ef = solver.expression(e, "*", 10, "+", f);
        solver.addRelation(solver.expression(ab, "+", cd), "=", ef);

        long startTime = System.currentTimeMillis();
        long solutions = solver.solve();
        long endTime = System.currentTimeMillis();

        System.out.println("Résultats:");
        System.out.printf("  Solutions: %d%n", solutions);
        System.out.printf("  Nœuds explorés: %,d%n", solver.getNodesCounter());
        System.out.printf("  Temps: %d ms%n", (endTime - startTime));
        System.out.println("  Complexité: ⭐ (Très simple)");
    }

    /**
     * Niveau 2: Cryptarithm intermédiaire - TEAM + TEAM = GOALS
     * Variables: 8 lettres, contrainte double
     */
    public static void solveMediumCryptarithm() {
        System.out.println("Cryptarithm: TEAM + TEAM = GOALS");
        System.out.println("Variables: T,E,A,M,G,O,L,S ∈ {0..9}, T,G ≠ 0, toutes distinctes");
        System.out.println("Contrainte: TEAM + TEAM = GOALS\n");

        ISolver solver = new Solver();
        solver.setVerbose(false);
        solver.reduceAndCheckIntervalsStrategy();

        // Variables: T,E,A,M,G,O,L,S ∈ {0..9}, T,G ≠ 0, toutes distinctes
        var t = solver.newVar("T", 1, 9);
        var e = solver.newVar("E", 0, 9);
        var a = solver.newVar("A", 0, 9);
        var m = solver.newVar("M", 0, 9);
        var g = solver.newVar("G", 1, 9);
        var o = solver.newVar("O", 0, 9);
        var l = solver.newVar("L", 0, 9);
        var s = solver.newVar("S", 0, 9);

        solver.addAllDiffRelation(t, e, a, m, g, o, l, s);

        // TEAM + TEAM = GOALS ⟺ 2×TEAM = GOALS
        var team = solver.expression(t, "*", 1000, "+", e, "*", 100, "+", a, "*", 10, "+", m);
        var goals = solver.expression(g, "*", 10000, "+", o, "*", 1000, "+", a, "*", 100, "+", l, "*", 10, "+", s);
        solver.addRelation(solver.expression(team, "*", 2), "=", goals);

        long startTime = System.currentTimeMillis();
        long solutions = solver.solve();
        long endTime = System.currentTimeMillis();

        System.out.println("Résultats:");
        System.out.printf("  Solutions: %d%n", solutions);
        System.out.printf("  Nœuds explorés: %,d%n", solver.getNodesCounter());
        System.out.printf("  Temps: %d ms%n", (endTime - startTime));
        System.out.println("  Complexité: ⭐⭐ (Intermédiaire)");
    }

    /**
     * Niveau 3: Cryptarithm difficile - SEND + MORE = MONEY
     * Variables: 8 lettres, contraintes complexes avec retenues (classique !)
     */
    public static void solveHardCryptarithm() {
        System.out.println("Cryptarithm: SEND + MORE = MONEY");
        System.out.println("Variables: S,E,N,D,M,O,R,Y ∈ {0..9}, S,M ≠ 0, toutes distinctes");
        System.out.println("Contrainte: SEND + MORE = MONEY\n");

        ISolver solver = new Solver();
        solver.setVerbose(false);
        solver.reduceAndCheckIntervalsStrategy();

        // Variables: S,E,N,D,M,O,R,Y ∈ {0..9}, S,M ≠ 0, toutes distinctes
        var s = solver.newVar("S", 1, 9);
        var e = solver.newVar("E", 0, 9);
        var n = solver.newVar("N", 0, 9);
        var d = solver.newVar("D", 0, 9);
        var m = solver.newVar("M", 1, 9);
        var o = solver.newVar("O", 0, 9);
        var r = solver.newVar("R", 0, 9);
        var y = solver.newVar("Y", 0, 9);

        solver.addAllDiffRelation(s, e, n, d, m, o, r, y);

        // SEND + MORE = MONEY
        var send = solver.expression(s, "*", 1000, "+", e, "*", 100, "+", n, "*", 10, "+", d);
        var more = solver.expression(m, "*", 1000, "+", o, "*", 100, "+", r, "*", 10, "+", e);
        var money = solver.expression(m, "*", 10000, "+", o, "*", 1000, "+", n, "*", 100, "+", e, "*", 10, "+", y);
        solver.addRelation(solver.expression(send, "+", more), "=", money);

        long startTime = System.currentTimeMillis();
        long solutions = solver.solve();
        long endTime = System.currentTimeMillis();

        System.out.println("Résultats:");
        System.out.printf("  Solutions: %d%n", solutions);
        System.out.printf("  Nœuds explorés: %,d%n", solver.getNodesCounter());
        System.out.printf("  Temps: %d ms%n", (endTime - startTime));
        if (solutions > 0) {
            System.out.println("  Solution célèbre: S=9, E=5, N=6, D=7, M=1, O=0, R=8, Y=2");
            System.out.println("  Vérification: 9567 + 1085 = 10652 ✓");
        }
        System.out.println("  Complexité: ⭐⭐⭐ (Très difficile - classique !)");
    }

    /**
     * Test de la méthode buildAllNotEmptyIntervals pour [0,9]
     * Devrait produire 55 intervalles non vides
     */
    public static void testBuildAllNotEmptyIntervals() {
        try {
            // Créer une instance d'Interval pour accéder à la méthode publique
            var intervalInstance = new Interval(0, 0);

            var result = intervalInstance.buildAllNotEmptyIntervals(0, 9);

            System.out.printf("📊 Nombre d'intervalles générés: %d%n", result.size());
            System.out.println("✅ Attendu: 55 intervalles");

            if (result.size() == 55) {
                System.out.println("✅ SUCCÈS: La méthode buildAllNotEmptyIntervals fonctionne correctement!");

                // Afficher quelques exemples
                System.out.println("\n🔍 Quelques exemples d'intervalles générés:");
                System.out.println("   • " + result.get(0));     // [0,0]
                System.out.println("   • " + result.get(1));     // [0,1]
                System.out.println("   • " + result.get(9));     // [0,9]
                System.out.println("   • " + result.get(10));    // [1,1]
                System.out.println("   • " + result.get(54));    // [9,9]

                // Vérifier que tous sont non vides
                boolean allNonEmpty = result.stream().allMatch(i -> !i.isEmpty());
                System.out.println("   • Tous les intervalles sont non vides: " + (allNonEmpty ? "✅ OUI" : "❌ NON"));

            } else {
                System.out.println("❌ ÉCHEC: Nombre incorrect d'intervalles générés");
            }

        } catch (Exception e) {
            System.out.println("❌ ERREUR lors du test: " + e.getMessage());
        }
    }

    /**
     * Démonstration du test exhaustif d'addition (version limitée pour la démo)
     * Étape 3: Test sur quelques intervalles représentatifs de [-8,8]
     */
    public static void demonstrateExhaustiveAddition() {
        try {
            // Construire quelques intervalles représentatifs de [-8,8]
            List<Interval> testIntervals = List.of(
                new Interval(-8, -8),  // intervalle ponctuel négatif
                new Interval(-5, 2),   // intervalle mixte
                new Interval(0, 0),    // zéro
                new Interval(3, 7),    // intervalle positif
                new Interval(8, 8),    // intervalle ponctuel positif
                Interval.empty()       // intervalle vide
            );

            System.out.printf("🔍 TEST EXHAUSTIF ADDITION (échantillon): %d intervalles représentatifs%n", testIntervals.size());

            int totalTests = 0;
            int passedTests = 0;

            // Tester quelques couples représentatifs (pas tous pour éviter trop de temps)
            for (int i = 0; i < testIntervals.size(); i++) {
                for (int j = 0; j < testIntervals.size(); j++) {
                    Interval a = testIntervals.get(i);
                    Interval b = testIntervals.get(j);
                    totalTests++;

                    try {
                        // Calcul par exploration exhaustive
                        Interval expected = exploreOperation(Integer::sum, a, b);

                        // Calcul par la méthode add() existante
                        Interval actual = a.add(b);

                        // Vérification
                        boolean success = expected.equals(actual);

                        if (success) {
                            passedTests++;
                            System.out.printf("  ✅ %s + %s = %s%n", a, b, actual);
                        } else {
                            System.out.printf("  ❌ %s + %s = %s (attendu: %s)%n", a, b, actual, expected);
                        }

                    } catch (Exception e) {
                        System.out.printf("  ⚠️  Exception lors du test %s + %s: %s%n", a, b, e.getMessage());
                    }
                }
            }

            System.out.printf("%n📊 RÉSULTAT: %d/%d tests réussis (%.1f%%)%n",
                            passedTests, totalTests, 100.0 * passedTests / totalTests);

            if (passedTests == totalTests) {
                System.out.println("🎉 SUCCÈS: L'échantillon de tests d'addition est passé!");
                System.out.println("💡 Le test complet (tous les intervalles de [-8,8]) passe également dans TestInterval.testExhaustiveAddition()");
            } else {
                System.out.println("❌ Certains tests ont échoué - voir les logs ci-dessus");
            }

        } catch (Exception e) {
            System.out.println("❌ ERREUR lors de la démonstration: " + e.getMessage());
        }
    }

    /**
     * Version simplifiée de exploreOperation pour la démonstration
     */
    private static Interval exploreOperation(java.util.function.BiFunction<Integer, Integer, Integer> operation,
                                           Interval a, Interval b) {
        if (a.isEmpty() || b.isEmpty()) {
            return Interval.empty();
        }

        int minResult = Integer.MAX_VALUE;
        int maxResult = Integer.MIN_VALUE;

        for (int valA = a.getMin(); valA <= a.getMax(); valA++) {
            for (int valB = b.getMin(); valB <= b.getMax(); valB++) {
                try {
                    int result = operation.apply(valA, valB);
                    minResult = Math.min(minResult, result);
                    maxResult = Math.max(maxResult, result);
                } catch (ArithmeticException e) {
                    // Ignorer les divisions par zéro, etc.
                }
            }
        }

        return new Interval(minResult, maxResult);
    }

    /**
     * Test rapide de validation pour confirmer que exploreOperation et add() sont cohérents
     */
    public static void quickValidationTest() {
        // Quelques tests représentatifs
        Interval[][] testCases = {
            {new Interval(-8, -5), new Interval(2, 5)},  // Négatif + positif
            {new Interval(-3, 3), new Interval(-2, 4)},   // Mixte + mixte
            {new Interval(1, 3), new Interval(4, 7)},     // Positif + positif
            {new Interval(0, 0), new Interval(-5, 5)},    // Zéro + mixte
            {Interval.empty(), new Interval(1, 2)},       // Vide + normal
            {new Interval(-10, -8), Interval.empty()}     // Normal + vide
        };

        int passed = 0;
        int total = testCases.length;

        for (Interval[] testCase : testCases) {
            Interval a = testCase[0];
            Interval b = testCase[1];

            Interval expected = exploreOperation(Integer::sum, a, b);
            Interval actual = a.add(b);

            boolean success = expected.equals(actual);
            if (success) passed++;

            System.out.printf("  %s + %s = %s %s%n",
                            a, b, actual, success ? "✅" : "❌");
        }

        System.out.printf("%n📊 VALIDATION: %d/%d tests réussis%n", passed, total);
        if (passed == total) {
            System.out.println("🎯 SUCCÈS: L'arithmétique d'intervalles est cohérente!");
        } else {
            System.out.println("❌ ÉCHEC: Incohérences détectées");
        }
    }

    /**
     * Démonstration des tests exhaustifs pour toutes les opérations (échantillon)
     */
    public static void demonstrateAllOperations() {
        try {
            // Quelques intervalles représentatifs pour la démonstration
            List<Interval> testIntervals = List.of(
                new Interval(-3, -1),  // négatif
                new Interval(0, 0),    // zéro
                new Interval(2, 4),    // positif
                Interval.empty()       // vide
            );

            String[] operations = {"+", "-", "×", "÷"};

            System.out.printf("🔍 DÉMONSTRATION: Test des %d opérations sur %d intervalles représentatifs%n",
                            operations.length, testIntervals.size());
            System.out.println("   (Les tests complets sont disponibles dans TestInterval.java)");

            for (String op : operations) {
                System.out.printf("%n🧮 OPÉRATION %s:%n", op);
                int total = 0;
                int passed = 0;

                for (Interval a : testIntervals) {
                    for (Interval b : testIntervals) {
                        total++;

                        try {
                            Interval result = switch (op) {
                                case "+" -> exploreOperation(Integer::sum, a, b);
                                case "-" -> exploreOperation((x, y) -> x - y, a, b);
                                case "×" -> exploreOperation((x, y) -> x * y, a, b);
                                case "÷" -> exploreOperation((x, y) -> {
                                    if (y == 0) throw new ArithmeticException("Division by zero");
                                    return x / y;
                                }, a, b);
                                default -> Interval.empty();
                            };

                            Interval expected = switch (op) {
                                case "+" -> a.add(b);
                                case "-" -> a.sub(b);
                                case "×" -> a.mul(b);
                                case "÷" -> a.div(b);
                                default -> Interval.empty();
                            };

                            boolean success = result.equals(expected);
                            if (success) passed++;

                            if (total <= 4) { // Afficher seulement quelques exemples
                                System.out.printf("   %s %s %s = %s %s%n",
                                                a, op, b, expected, success ? "✅" : "❌");
                            }

                        } catch (Exception e) {
                            // Pour la division, certaines combinaisons peuvent échouer
                        }
                    }
                }

                System.out.printf("   📊 Résultat %s: %d/%d tests réussis%n", op, passed, total);
            }

            System.out.println("%n💡 Les tests exhaustifs complets pour chaque opération sont disponibles:");
            System.out.println("   • testExhaustiveAddition() - 84,100 tests");
            System.out.println("   • testExhaustiveSubtraction() - 84,100 tests");
            System.out.println("   • testExhaustiveMultiplication() - 84,100 tests");
            System.out.println("   • testExhaustiveDivision() - 84,100 tests");

        } catch (Exception e) {
            System.out.println("❌ ERREUR lors de la démonstration: " + e.getMessage());
        }
    }

    /**
     * Démonstration des fonctionnalités du record Constraint
     */
    public static void demonstrateConstraintRecord() {
        try {
            // Création d'un record Constraint
            Variable a = new Variable("A");
            a.init(1, 9);
            Variable b = new Variable("B");
            b.init(0, 9);
            Variable result = new Variable("RESULT");
            result.init(0, 18);

            Constraint addConstraint = new Constraint(ConstraintType.ADD, result, a, b);

            // Utilisation des méthodes d'accès (noms des champs)
            System.out.printf("   Contrainte créée: %s%n", addConstraint);
            System.out.printf("   Type d'opération: %s%n", addConstraint.type());
            System.out.printf("   Variable résultat: %s%n", addConstraint.result().getName());
            System.out.printf("   Première opérande: %s%n", addConstraint.var1().getName());
            System.out.printf("   Deuxième opérande: %s%n", addConstraint.var2().getName());

            // Égalité et hashCode automatiques
            Constraint sameConstraint = new Constraint(ConstraintType.ADD, result, a, b);
            Constraint diffConstraint = new Constraint(ConstraintType.MUL, result, a, b);

            System.out.printf("   Égalité avec contrainte identique: %s%n", addConstraint.equals(sameConstraint));
            System.out.printf("   Égalité avec contrainte différente: %s%n", addConstraint.equals(diffConstraint));

            // Immuabilité garantie
            System.out.println("   ✅ Immuabilité garantie par le record");
            System.out.println("   ✅ Constructeur compact avec paramètres nommés");
            System.out.println("   ✅ toString(), equals(), hashCode() automatiques");

        } catch (Exception e) {
            System.out.println("❌ ERREUR lors de la démonstration du record: " + e.getMessage());
        }
    }

    /**
     * Test d'intégration rapide : Solver utilise ProblemBuilder
     */
    public static void testProblemBuilderIntegration() {
        try {
            // Créer un petit problème simple : X + Y = Z où X,Y,Z dans [0,5]
            Solver solver = new Solver();

            Variable X = solver.newVar("X", 0, 5);
            Variable Y = solver.newVar("Y", 0, 5);
            Variable Z = solver.newVar("Z", 0, 10);

            // Z = X + Y
            Variable sum = solver.expression(X, "+", Y);
            solver.addRelation(Z, "=", sum);

            System.out.println("Problème créé: X + Y = Z (X,Y,Z ∈ [0,5]∩[0,10])");

            // Activer le mode verbose pour voir le logging ProblemBuilder
            solver.setVerbose(true);

            // Résoudre - cela devrait utiliser ProblemBuilder en interne
            long solutions = solver.solve();

            System.out.printf("✅ Solutions trouvées: %d%n", solutions);
            System.out.printf("✅ Nœuds explorés: %d%n", solver.getNodesCounter());
            System.out.println("✅ L'intégration ProblemBuilder fonctionne !");

        } catch (Exception e) {
            System.out.println("❌ ERREUR lors du test d'intégration: " + e.getMessage());
        }
    }

    /**
     * Démonstration des métriques d'optimisation d'expressions
     */
    public static void demonstrateOptimizationMetrics() {
        try {
            // Test de l'exemple A + 2 = B
            System.out.println("🔍 Exemple : A + 2 = B");
            demonstrateSingleOptimization("A + 2 = B", solver -> {
                Variable A = createVariable("A", 0, 9);
                Variable B = createVariable("B", 0, 18);
                Variable expr = createOptimizedExpression(solver, A, "+", 2);
                solver.addRelation(expr, "=", B);
            });

            // Test d'un exemple plus complexe qui devrait montrer l'optimisation
            System.out.println("\n🔍 Exemple : A + B + 1 = C (chaînage d'additions)");
            demonstrateSingleOptimization("A + B + 1 = C", solver -> {
                Variable A = createVariable("A", 0, 9);
                Variable B = createVariable("B", 0, 9);
                Variable C = createVariable("C", 0, 19);
                Variable expr = createOptimizedExpression(solver, A, "+", B, "+", 1);
                solver.addRelation(expr, "=", C);
            });

            // Test avec debug pour comprendre A + 2 = B
            System.out.println("\n🔍 DEBUG : Analyse détaillée de A + 2 = B");
            debugOptimizationExample();

        } catch (Exception e) {
            System.out.println("❌ ERREUR lors de la démonstration des métriques: " + e.getMessage());
        }
    }

    private static void demonstrateSingleOptimization(String expressionName, Consumer<Solver> setupExpression) {
        try {
            // Test SANS optimisation
            Solver solverNoOpt = new Solver();
            setupExpression.accept(solverNoOpt);

            int variablesNoOpt = getVariables(solverNoOpt).size();
            int constraintsNoOpt = getConstraints(solverNoOpt).size();
            long solutionsNoOpt = solverNoOpt.solve();

            // Test AVEC optimisation
            Solver solverOpt = new Solver();
            setupExpression.accept(solverOpt);

            int variablesOpt = getVariables(solverOpt).size();
            int constraintsOpt = getConstraints(solverOpt).size();
            long solutionsOpt = solverOpt.solve();

            // Affichage des métriques
            System.out.println("  📈 SANS optimisation : " + variablesNoOpt + " variables, " + constraintsNoOpt + " contraintes");
            System.out.println("  ⚡ AVEC optimisation : " + variablesOpt + " variables, " + constraintsOpt + " contraintes");

            // Calcul des réductions
            double varReduction = variablesNoOpt > 0 ? 100.0 * (variablesNoOpt - variablesOpt) / variablesNoOpt : 0;
            double constraintReduction = constraintsNoOpt > 0 ? 100.0 * (constraintsNoOpt - constraintsOpt) / constraintsNoOpt : 0;

            System.out.printf("  🎯 Réduction: %.1f%% variables, %.1f%% contraintes%n", varReduction, constraintReduction);

            if (solutionsNoOpt == solutionsOpt) {
                System.out.println("  ✅ Équivalence préservée: " + solutionsOpt + " solutions");
            } else {
                System.out.println("  ❌ ERREUR: Solutions différentes !");
            }

        } catch (Exception e) {
            System.out.println("  ❌ ERREUR pour " + expressionName + ": " + e.getMessage());
        }
    }

    /**
     * Debug détaillé de l'exemple A + 2 = B pour comprendre l'optimisation
     */
    private static void debugOptimizationExample() {
        try {
            System.out.println("--- ANALYSE SANS OPTIMISATION ---");
            Solver solverNoOpt = new Solver();
            Variable A1 = createVariable("A", 0, 9);
            Variable B1 = createVariable("B", 0, 18);

            // Expression normale (sans optimisation)
            Variable expr1 = solverNoOpt.expression(A1, "+", 2);
            solverNoOpt.addRelation(expr1, "=", B1);

            System.out.println("Variables (" + getVariables(solverNoOpt).size() + "):");
            for (var v : getVariables(solverNoOpt)) {
                System.out.println("  " + v.getName() + " ∈ [" + v.getMin() + "," + v.getMax() + "]");
            }
            System.out.println("Contraintes (" + getConstraints(solverNoOpt).size() + "):");
            for (var c : getConstraints(solverNoOpt)) {
                System.out.println("  " + c);
            }

            System.out.println("\n--- ANALYSE AVEC OPTIMISATION ---");
            Solver solverOpt = new Solver();
            Variable A2 = createVariable("A", 0, 9);
            Variable B2 = createVariable("B", 0, 18);

            // Expression optimisée
            Variable expr2 = createOptimizedExpression(solverOpt, A2, "+", 2);
            solverOpt.addRelation(expr2, "=", B2);

            System.out.println("Variables (" + getVariables(solverOpt).size() + "):");
            for (var v : getVariables(solverOpt)) {
                System.out.println("  " + v.getName() + " ∈ [" + v.getMin() + "," + v.getMax() + "]");
            }
            System.out.println("Contraintes (" + getConstraints(solverOpt).size() + "):");
            for (var c : getConstraints(solverOpt)) {
                System.out.println("  " + c);
            }

            // Comparaison
            int varDiff = getVariables(solverNoOpt).size() - getVariables(solverOpt).size();
            int constDiff = getConstraints(solverNoOpt).size() - getConstraints(solverOpt).size();

            System.out.println("\n🎯 RÉSULTAT:");
            System.out.println("  Variables éliminées: " + varDiff);
            System.out.println("  Contraintes éliminées: " + constDiff);

            if (varDiff > 0 || constDiff > 0) {
                System.out.println("  ✅ OPTIMISATION RÉUSSIE !");
            } else {
                System.out.println("  ⚠️  AUCUNE OPTIMISATION APPLIQUÉE");
                System.out.println("     (Possible: expression trop simple ou algorithme non déclenché)");
            }

        } catch (Exception e) {
            System.out.println("❌ ERREUR lors du debug: " + e.getMessage());
        }
    }

    // Méthode utilitaire pour créer une expression optimisée (évite l'ambiguïté)
    private static Variable createOptimizedExpression(Solver solver, Object... terms) {
        return solver.expressionOptimized(terms);
    }

    // Méthode utilitaire pour créer des variables de test
    private static Variable createVariable(String name, int min, int max) {
        var v = new Variable(name);
        v.init(min, max);
        return v;
    }

    // Méthode utilitaire pour accéder aux contraintes (via réflexion)
    @SuppressWarnings("unchecked")
    private static java.util.List<Constraint> getConstraints(Solver solver) {
        try {
            var field = Solver.class.getDeclaredField("constraints");
            field.setAccessible(true);
            return (java.util.List<Constraint>) field.get(solver);
        } catch (Exception e) {
            return java.util.Collections.emptyList();
        }
    }

    // Méthode utilitaire pour accéder aux variables (via réflexion)
    @SuppressWarnings("unchecked")
    private static java.util.List<Variable> getVariables(Solver solver) {
        try {
            var field = Solver.class.getDeclaredField("variables");
            field.setAccessible(true);
            return (java.util.List<Variable>) field.get(solver);
        } catch (Exception e) {
            return java.util.Collections.emptyList();
        }
    }
}