package fr.univamu.solver;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== Suite de Cryptarithmes: Difficultés Progressives ===\n");

        // Niveau 1: Facile
        System.out.println("🎯 NIVEAU 1: Cryptarithm Simple");
        solveEasyCryptarithm();

        System.out.println("\n" + "=".repeat(70) + "\n");

        // Niveau 2: Intermédiaire
        System.out.println("🎯 NIVEAU 2: Cryptarithm Intermédiaire");
        solveMediumCryptarithm();

        System.out.println("\n" + "=".repeat(70) + "\n");

        // Niveau 3: Difficile
        System.out.println("🎯 NIVEAU 3: Cryptarithm Difficile");
        solveHardCryptarithm();

        System.out.println("\n" + "=".repeat(70) + "\n");

        // Comparaison finale avec l'exemple simple
        System.out.println("🏆 COMPARAISON FINALE - Cryptarithm AB + CD = EF");
        demonstrateSimpleComparison();
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
}