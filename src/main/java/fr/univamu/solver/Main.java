package fr.univamu.solver;

import fr.univamu.solver.api.ProblemBuilder;
import fr.univamu.solver.domain.Assignment;
import fr.univamu.solver.domain.Constraint;
import fr.univamu.solver.domain.ConstraintType;
import fr.univamu.solver.domain.Interval;
import fr.univamu.solver.domain.Problem;
import fr.univamu.solver.domain.Variable;
import fr.univamu.solver.engine.Solutions;
import fr.univamu.solver.engine.Solver;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Point d'entrée de démonstration du projet GL-Solver.
 * Chaque section illustre une fonctionnalité clé du solveur.
 */
public final class Main {

    private static final String SECTION = "=".repeat(90);
    private static final String SUB_SECTION = "-".repeat(90);

    private record Demo(String title, Runnable action) {}

    private record RunStats(long solutions, long nodes, long timeMs, int variables, int constraints) {}

    private record RunOutcome(RunStats stats, List<List<Assignment>> solutions) {}

    private record ExpressionScenario(String title, Consumer<Solver> builder) {}

    private record CryptarithmScenario(String title, String description,
                                       Consumer<Solver> builder,
                                       boolean useReducer,
                                       long maxNodes,
                                       int solutionLimit,
                                       int sampleCount) {}

    private record ProblemScenario(String title, String description,
                                   Consumer<ProblemBuilder> builderConfigurator) {}

    private static class StopSearchException extends RuntimeException {
        private StopSearchException() {
            super(null, null, false, false);
        }
    }

    private Main() {
        // Classe utilitaire
    }

    public static void main(String[] args) {
        List<Demo> demos = List.of(
            new Demo("Cryptarithmes multi-niveaux", Main::demoCryptarithms),
            new Demo("Optimisation d'expressions arithmétiques", Main::demoExpressionOptimisation),
            new Demo("Construction de problèmes immuables (ProblemBuilder)", Main::demoProblemBuilder),
            new Demo("Boîte à outils Interval : opérations & vérifications", Main::demoIntervals),
            new Demo("Constraint record & réduction avancée de DIFF", Main::demoConstraintAndDiffReduction)
        );

        printHeader();
        demos.forEach(Main::runDemo);
        printFooter();
    }

    private static void runDemo(Demo demo) {
        System.out.println();
        System.out.println(SECTION);
        System.out.println("▶ " + demo.title());
        System.out.println(SECTION);
        long start = System.currentTimeMillis();
        try {
            demo.action().run();
        } catch (Exception exception) {
            System.out.println("⚠️  Erreur pendant la démonstration : " + exception.getMessage());
            exception.printStackTrace(System.out);
        } finally {
            long duration = System.currentTimeMillis() - start;
            System.out.printf("%n⌛ Durée de la démonstration : %d ms%n", duration);
        }
    }

    // ---------------------------------------------------------------------
    // Demo 1 – Cryptarithmes
    // ---------------------------------------------------------------------

    private static void demoCryptarithms() {
        List<CryptarithmScenario> scenarios = List.of(
            new CryptarithmScenario(
                "Niveau 1 : AB + CD = EF",
                "Addition sur deux chiffres (476 solutions, utile pour illustrer la combinaison & la diversité).",
                solver -> {
                    var a = solver.newVar("A", 1, 9);
                    var b = solver.newVar("B", 0, 9);
                    var c = solver.newVar("C", 1, 9);
                    var d = solver.newVar("D", 0, 9);
                    var e = solver.newVar("E", 1, 9);
                    var f = solver.newVar("F", 0, 9);
                    solver.addAllDiffRelation(a, b, c, d, e, f);
                    var ab = solver.expression(a, "*", 10, "+", b);
                    var cd = solver.expression(c, "*", 10, "+", d);
                    var ef = solver.expression(e, "*", 10, "+", f);
                    solver.addRelation(solver.expression(ab, "+", cd), "=", ef);
                },
                true,
                150_000_000L,
                0,
                4
            ),
            new CryptarithmScenario(
                "Niveau 2 : AB + BA = CBC",
                "Cryptarithme avec retenue et solution unique (puzzle classique issu des tests).",
                solver -> {
                    var a = solver.newVar("A", 1, 9);
                    var b = solver.newVar("B", 0, 9);
                    var c = solver.newVar("C", 1, 9);
                    solver.addAllDiffRelation(a, b, c);
                    var ab = solver.expression(a, "*", 10, "+", b);
                    var ba = solver.expression(b, "*", 10, "+", a);
                    var cbc = solver.expression(c, "*", 100, "+", b, "*", 10, "+", c);
                    solver.addRelation(solver.expression(ab, "+", ba), "=", cbc);
                },
                true,
                120_000_000L,
                0,
                2
            ),
            new CryptarithmScenario(
                "Niveau 3 : FUN + FUN = GAME",
                "Cryptarithme à huit lettres (hérité de l'ancien Main), avec une poignée de solutions.",
                solver -> {
                    var f = solver.newVar("F", 1, 9);
                    var u = solver.newVar("U", 0, 9);
                    var n = solver.newVar("N", 0, 9);
                    var g = solver.newVar("G", 1, 9);
                    var a = solver.newVar("A", 0, 9);
                    var m = solver.newVar("M", 0, 9);
                    var e = solver.newVar("E", 0, 9);
                    solver.addAllDiffRelation(f, u, n, g, a, m, e);
                    var fun = solver.expression(f, "*", 100, "+", u, "*", 10, "+", n);
                    var game = solver.expression(g, "*", 1000, "+", a, "*", 100, "+", m, "*", 10, "+", e);
                    solver.addRelation(solver.expression(fun, "*", 2), "=", game);
                },
                true,
                200_000_000L,
                0,
                2
            ),
            new CryptarithmScenario(
                "Niveau 4 : SEND + MORE = MONEY",
                "Cryptarithme classique à huit lettres avec retenues multiples (solution unique).",
                solver -> {
                    var s = solver.newVar("S", 1, 9);
                    var e = solver.newVar("E", 0, 9);
                    var n = solver.newVar("N", 0, 9);
                    var d = solver.newVar("D", 0, 9);
                    var m = solver.newVar("M", 1, 9);
                    var o = solver.newVar("O", 0, 9);
                    var r = solver.newVar("R", 0, 9);
                    var y = solver.newVar("Y", 0, 9);

                    solver.addAllDiffRelation(s, e, n, d, m, o, r, y);

                    var send = solver.expression(s, "*", 1000, "+", e, "*", 100, "+", n, "*", 10, "+", d);
                    var more = solver.expression(m, "*", 1000, "+", o, "*", 100, "+", r, "*", 10, "+", e);
                    var money = solver.expression(m, "*", 10000, "+", o, "*", 1000, "+", n, "*", 100, "+", e, "*", 10, "+", y);
                    solver.addRelation(solver.expression(send, "+", more), "=", money);
                },
                true,
                800_000_000L,
                1,
                1
            )
        );

        scenarios.forEach(Main::runCryptarithmScenario);
    }

    private static void runCryptarithmScenario(CryptarithmScenario scenario) {
        System.out.println(SUB_SECTION);
        System.out.println("🧩 " + scenario.title());
        System.out.println(SUB_SECTION);
        System.out.println("   " + scenario.description());

        Solver solver = newConfiguredSolver(scenario.useReducer());
        solver.setMaxNodes(scenario.maxNodes());
        scenario.builder().accept(solver);

        RunOutcome outcome = solveAndCollect(solver, scenario.solutionLimit());
        printRunStats(outcome.stats());
        printSampleSolutions(outcome.solutions(), scenario.sampleCount());
    }

    // ---------------------------------------------------------------------
    // Demo 2 – Expression optimisation
    // ---------------------------------------------------------------------

    private static void demoExpressionOptimisation() {
        List<ExpressionScenario> scenarios = List.of(
            new ExpressionScenario("A + 2 = B",
                solver -> {
                    var a = solver.newVar("A", 0, 9);
                    var b = solver.newVar("B", 0, 9);
                    var expr = solver.expression(a, "+", 2);
                    solver.addRelation(expr, "=", b);
                }),
            new ExpressionScenario("(A + B) * 2 = C",
                solver -> {
                    var a = solver.newVar("A", 0, 9);
                    var b = solver.newVar("B", 0, 9);
                    var c = solver.newVar("C", 0, 36);
                    var sum = solver.expression(a, "+", b);
                    var expr = solver.expression(sum, "*", 2);
                    solver.addRelation(expr, "=", c);
                }),
            new ExpressionScenario("A + B + C = D",
                solver -> {
                    var a = solver.newVar("A", 0, 5);
                    var b = solver.newVar("B", 0, 5);
                    var c = solver.newVar("C", 0, 5);
                    var d = solver.newVar("D", 0, 15);
                    var sumAB = solver.expression(a, "+", b);
                    var expr = solver.expression(sumAB, "+", c);
                    solver.addRelation(expr, "=", d);
                }),
            new ExpressionScenario("(A + 5) * (B + 3) = C",
                solver -> {
                    var a = solver.newVar("A", 0, 5);
                    var b = solver.newVar("B", 0, 5);
                    var c = solver.newVar("C", 0, 80);
                    var offsetA = solver.expression(a, "+", 5);
                    var offsetB = solver.expression(b, "+", 3);
                    var expr = solver.expression(offsetA, "*", offsetB);
                    solver.addRelation(expr, "=", c);
                }),
            new ExpressionScenario("(A * B) + (C * D) = E",
                solver -> {
                    var a = solver.newVar("A", 0, 5);
                    var b = solver.newVar("B", 0, 5);
                    var c = solver.newVar("C", 0, 5);
                    var d = solver.newVar("D", 0, 5);
                    var e = solver.newVar("E", 0, 60);
                    var productAB = solver.expression(a, "*", b);
                    var productCD = solver.expression(c, "*", d);
                    var expr = solver.expression(productAB, "+", productCD);
                    solver.addRelation(expr, "=", e);
                }),
            new ExpressionScenario("(3 * A) / (B + 1) = C",
                solver -> {
                    var a = solver.newVar("A", 1, 9);
                    var b = solver.newVar("B", 0, 8);
                    var c = solver.newVar("C", 0, 20);
                    var triple = solver.expression(a, "*", 3);
                    var denom = solver.expression(b, "+", 1);
                    var expr = solver.expression(triple, "/", denom);
                    solver.addRelation(expr, "=", c);
                }),
            new ExpressionScenario("X > 10 (réduction directe du domaine)",
                solver -> {
                    var x = solver.newVar("X", 0, 20);
                    solver.addRelation(x, ">", 10);
                })
        );

        scenarios.forEach(Main::runExpressionScenario);
    }

    private static void runExpressionScenario(ExpressionScenario scenario) {
        System.out.println(SUB_SECTION);
        System.out.println("🧪 " + scenario.title());
        System.out.println(SUB_SECTION);

        Solver normal = newConfiguredSolver(false);
        scenario.builder().accept(normal);
        RunOutcome normalOutcome = solveAndCollect(normal);

        Solver optimized = newConfiguredSolver(false);
        scenario.builder().accept(optimized);
        optimized.optimize();
        RunOutcome optimizedOutcome = solveAndCollect(optimized);

        printComparison(normalOutcome.stats(), optimizedOutcome.stats());
        if (normalOutcome.stats().solutions() != optimizedOutcome.stats().solutions()) {
            System.out.println("   ⚠️  Les deux stratégies donnent un nombre de solutions différent !");
        }
    }

    // ---------------------------------------------------------------------
    // Demo 3 – ProblemBuilder
    // ---------------------------------------------------------------------

    private static void demoProblemBuilder() {
        List<ProblemScenario> scenarios = List.of(
            new ProblemScenario(
                "Budget équilibré",
                "TOTAL = PRICE + TAX avec domaines bornés.",
                builder -> {
                    var price = new Variable("PRICE");
                    price.init(10, 15);
                    var tax = new Variable("TAX");
                    tax.init(1, 4);
                    var total = new Variable("TOTAL");
                    total.init(10, 25);
                    builder.addVariable(price)
                           .addVariable(tax)
                           .addVariable(total)
                           .addConstraint(new Constraint(ConstraintType.ADD, total, price, tax))
                           .setStrategy(Problem.REDUCE_AND_CHECK_INTERVALS_STRATEGY)
                           .setVerbose(false)
                           .setMaxNodes(200_000);
                }),
            new ProblemScenario(
                "Coûts de production",
                "Calcul du coût total : COST_A = PROD_A * UNIT_COST_A, etc.",
                builder -> {
                    var prodA = new Variable("PROD_A");
                    prodA.init(0, 12);
                    var prodB = new Variable("PROD_B");
                    prodB.init(0, 9);
                    var unitCostA = new Variable("UNIT_COST_A");
                    unitCostA.init(5, 5);
                    var unitCostB = new Variable("UNIT_COST_B");
                    unitCostB.init(7, 7);
                    var costA = new Variable("COST_A");
                    costA.init(0, 84);
                    var costB = new Variable("COST_B");
                    costB.init(0, 63);
                    var totalCost = new Variable("TOTAL_COST");
                    totalCost.init(0, 160);

                    builder.addVariable(prodA)
                           .addVariable(prodB)
                           .addVariable(unitCostA)
                           .addVariable(unitCostB)
                           .addVariable(costA)
                           .addVariable(costB)
                           .addVariable(totalCost)
                           .addConstraint(new Constraint(ConstraintType.MUL, costA, prodA, unitCostA))
                           .addConstraint(new Constraint(ConstraintType.MUL, costB, prodB, unitCostB))
                           .addConstraint(new Constraint(ConstraintType.ADD, totalCost, costA, costB))
                           .setStrategy(Problem.CHECK_INTERVALS_STRATEGY)
                           .setVerbose(false)
                           .setMaxNodes(400_000);
                }),
            new ProblemScenario(
                "Score moyen",
                "AVERAGE = TOTAL_SCORE / PARTICIPANTS (division entière).",
                builder -> {
                    var totalScore = new Variable("TOTAL_SCORE");
                    totalScore.init(0, 120);
                    var participants = new Variable("PARTICIPANTS");
                    participants.init(1, 6);
                    var average = new Variable("AVERAGE");
                    average.init(0, 120);
                    builder.addVariable(totalScore)
                           .addVariable(participants)
                           .addVariable(average)
                           .addConstraint(new Constraint(ConstraintType.DIV, average, totalScore, participants))
                           .setStrategy(Problem.CHECK_INTERVALS_STRATEGY)
                           .setVerbose(false)
                           .setMaxNodes(200_000);
                })
        );

        scenarios.forEach(Main::runProblemScenario);
    }

    private static void runProblemScenario(ProblemScenario scenario) {
        System.out.println(SUB_SECTION);
        System.out.println("🏗️  " + scenario.title());
        System.out.println(SUB_SECTION);
        System.out.println("   " + scenario.description());

        ProblemBuilder builder = new ProblemBuilder();
        scenario.builderConfigurator().accept(builder);
        Problem problem = builder.build();

        System.out.println("   Caractéristiques du Problem immuable :");
        System.out.printf("      • Variables : %d%n", problem.getVariableCount());
        System.out.printf("      • Contraintes : %d%n", problem.getConstraintCount());
        System.out.printf("      • Stratégie : %d%n", problem.strategy());
        System.out.printf("      • maxNodes : %d%n", problem.maxNodes());
        System.out.printf("      • Variables listées : %s%n",
            problem.variables().stream()
                .map(Variable::toString)
                .collect(Collectors.joining(", ")));

        RunOutcome outcome = replayProblem(problem, 50);
        printRunStats(outcome.stats());
        printSampleSolutions(outcome.solutions(), 10);
    }

    private static RunOutcome replayProblem(Problem problem, int solutionLimit) {
        Solver solver = newConfiguredSolver(true);
        solver.setMaxNodes(Math.max(problem.maxNodes(), 10_000_000L));

        Map<Variable, Variable> mapping = new HashMap<>();
        int anonymousCounter = 0;
        for (Variable original : problem.variables()) {
            String name = original.isNamed() ? original.getName() : "_VAR" + (++anonymousCounter);
            mapping.put(original, solver.newVar(name, original.getMin(), original.getMax()));
        }

        for (Constraint constraint : problem.constraints()) {
            Variable result = mapping.get(constraint.result());
            Variable var1 = mapping.get(constraint.var1());
            Variable var2 = constraint.var2() != null ? mapping.get(constraint.var2()) : null;
            switch (constraint.type()) {
                case ADD -> solver.addRelation(result, "=", solver.expression(var1, "+", var2));
                case MUL -> solver.addRelation(result, "=", solver.expression(var1, "*", var2));
                case DIV -> solver.addRelation(result, "=", solver.expression(var1, "/", var2));
                case DIFF -> solver.addRelation(result, "<>", var1);
            }
        }

        solver.optimize();
        return solveAndCollect(solver, solutionLimit);
    }

    // ---------------------------------------------------------------------
    // Demo 4 – Intervalles
    // ---------------------------------------------------------------------

    private static void demoIntervals() {
        System.out.println(SUB_SECTION);
        System.out.println("📈 Exemple financier :");
        System.out.println(SUB_SECTION);
        Interval revenue = new Interval(120, 180);
        Interval cost = new Interval(35, 60);
        Interval profit = revenue.sub(cost);
        System.out.printf("   • Revenus : %s%n", revenue);
        System.out.printf("   • Coûts   : %s%n", cost);
        System.out.printf("   • Profit  : %s%n", profit);

        System.out.println();
        System.out.println(SUB_SECTION);
        System.out.println("🧮 Opérations d'arithmétique intervalle :");
        System.out.println(SUB_SECTION);
        Interval a = new Interval(3, 10);
        Interval b = new Interval(-5, 0);
        System.out.printf("   • Addition : %s + %s = %s%n", a, b, a.add(b));

        Interval m1 = new Interval(-2, 4);
        Interval m2 = new Interval(3, 5);
        System.out.printf("   • Multiplication : %s × %s = %s%n", m1, m2, m1.mul(m2));

        Interval d1 = new Interval(-12, 12);
        Interval d2 = new Interval(-3, 4);
        System.out.printf("   • Division : %s ÷ %s = %s%n", d1, d2, d1.div(d2));

        Interval inter1 = new Interval(-5, 8);
        Interval inter2 = new Interval(0, 12);
        System.out.printf("   • Intersection : %s ∩ %s = %s%n", inter1, inter2, inter1.inter(inter2));

        Interval bruteA = new Interval(-4, 2);
        Interval bruteB = new Interval(1, 3);
        Interval computed = bruteA.add(bruteB);
        Interval explored = bruteForceIntervalOperation(bruteA, bruteB, Integer::sum);
        System.out.printf("   • Vérification exhaustive (addition) : %s (arithmétique) vs %s (exploration)%n",
            computed, explored);

        Interval generator = new Interval();
        List<Interval> generated = generator.buildAllNotEmptyIntervals(0, 3);
        System.out.printf("%n🔁 Génération de tous les intervalles inclus dans [0,3] : %d combinaisons%n",
            generated.size());
        System.out.println("   • Exemples : " + generated.stream()
            .limit(6)
            .map(Interval::toString)
            .collect(Collectors.joining(", ")) + " ...");
    }

    private static Interval bruteForceIntervalOperation(Interval a, Interval b, BiFunction<Integer, Integer, Integer> op) {
        if (a.isEmpty() || b.isEmpty()) {
            return Interval.empty();
        }
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        for (int va = a.getMin(); va <= a.getMax(); va++) {
            for (int vb = b.getMin(); vb <= b.getMax(); vb++) {
                try {
                    int value = op.apply(va, vb);
                    min = Math.min(min, value);
                    max = Math.max(max, value);
                } catch (ArithmeticException ignored) {
                    // Division par zéro ou autre exception : ignorée.
                }
            }
        }
        if (min == Integer.MAX_VALUE) {
            return Interval.empty();
        }
        return new Interval(min, max);
    }

    // ---------------------------------------------------------------------
    // Demo 5 – Constraint record & réduction de DIFF
    // ---------------------------------------------------------------------

    private static void demoConstraintAndDiffReduction() {
        System.out.println(SUB_SECTION);
        System.out.println("🧾 Constraint record");
        System.out.println(SUB_SECTION);
        var constraint = new Constraint(
            ConstraintType.ADD,
            new Variable("SUM"),
            new Variable("A"),
            new Variable("B")
        );
        System.out.println("   • Exemple : " + constraint);

        System.out.println();
        System.out.println(SUB_SECTION);
        System.out.println("🔍 Réduction de la contrainte DIFF (différents cas)");
        System.out.println(SUB_SECTION);
        runDiffScenario("X∈[10,20], Y∈[20,20]", 10, 20, 20, 20);
        runDiffScenario("X∈[10,10], Y∈[10,11]", 10, 10, 10, 11);
        runDiffScenario("X∈[5,10], Y∈[5,5]", 5, 10, 5, 5);
        runDiffScenario("X∈[1,8], Y∈[8,8]", 1, 8, 8, 8);
        runDiffScenario("X∈[15,15], Y∈[15,15]", 15, 15, 15, 15);
    }

    private static void runDiffScenario(String label, int xMin, int xMax, int yMin, int yMax) {
        Solver solver = newConfiguredSolver(true);
        var x = solver.newVar("X", xMin, xMax);
        var y = solver.newVar("Y", yMin, yMax);
        solver.addRelation(x, "<>", y);
        solver.solve();
        System.out.printf("   • %s → %s, %s%n", label, x, y);
    }

    // ---------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------

    private static Solver newConfiguredSolver(boolean useReducer) {
        Solver solver = new Solver();
        solver.setVerbose(false);
        solver.getSolutions().setDisplaySolutions(false);
        if (useReducer) {
            solver.reduceAndCheckIntervalsStrategy();
        }
        return solver;
    }

    private static RunOutcome solveAndCollect(Solver solver) {
        return solveAndCollect(solver, 0);
    }

    private static RunOutcome solveAndCollect(Solver solver, int solutionLimit) {
        int variables = solver.getVariables().size();
        int constraints = solver.getConstraints().size();
        long start = System.currentTimeMillis();
        boolean nodeLimitReached = false;
        boolean solutionLimitReached = false;
        Solutions solutionsRef = solver.getSolutions();
        if (solutionLimit > 0) {
            solutionsRef.setOnSolutionFound(solution -> {
                if (solutionsRef.getCount() >= solutionLimit) {
                    throw new StopSearchException();
                }
            });
        }
        try {
            solver.solve();
        } catch (StopSearchException ex) {
            solutionLimitReached = true;
        } catch (IllegalStateException ex) {
            nodeLimitReached = true;
        }
        if (solutionLimit > 0) {
            solutionsRef.setOnSolutionFound(null);
        }
        long time = System.currentTimeMillis() - start;
        long nodes = solver.getNodesCounter();
        long solutions = solutionsRef.getCount();
        RunStats stats = new RunStats(solutions, nodes, time, variables, constraints);
        if (solutionLimitReached) {
            System.out.printf("   ⚠️  Recherche arrêtée après %d solutions (limite de démonstration).%n", solutions);
        }
        if (nodeLimitReached) {
            System.out.println("   ⚠️  Exploration interrompue : limite de nœuds atteinte.");
        }
        List<List<Assignment>> solutionsSnapshot = solutionsRef.getStoredSolutions();
        return new RunOutcome(stats, solutionsSnapshot);
    }

    private static void printRunStats(RunStats stats) {
        System.out.printf("   • Solutions trouvées : %d%n", stats.solutions());
        System.out.printf("   • Nœuds explorés     : %d%n", stats.nodes());
        System.out.printf("   • Temps de résolution : %d ms%n", stats.timeMs());
        System.out.printf("   • Variables           : %d%n", stats.variables());
        System.out.printf("   • Contraintes         : %d%n", stats.constraints());
    }

    private static void printComparison(RunStats normal, RunStats optimized) {
        System.out.println("   Structure :");
        System.out.printf("      - Normal    : %d variables, %d contraintes%n",
            normal.variables(), normal.constraints());
        System.out.printf("      - Optimisée : %d variables, %d contraintes%n",
            optimized.variables(), optimized.constraints());

        System.out.println("   Performance :");
        System.out.printf("      - Normal    : %d solutions, %d nœuds, %d ms%n",
            normal.solutions(), normal.nodes(), normal.timeMs());
        System.out.printf("      - Optimisée : %d solutions, %d nœuds, %d ms%n",
            optimized.solutions(), optimized.nodes(), optimized.timeMs());

        if (optimized.nodes() < normal.nodes()) {
            double gain = normal.nodes() == 0 ? 0 :
                100.0 * (normal.nodes() - optimized.nodes()) / normal.nodes();
            System.out.printf("      ➜ Réduction des nœuds : %.1f%%%n", gain);
        }
    }

    private static void printSampleSolutions(List<List<Assignment>> solutions, int maxSamples) {
        if (solutions.isEmpty()) {
            System.out.println("   • Aucune solution trouvée.");
            return;
        }
        int displayed = Math.min(maxSamples, solutions.size());
        System.out.println("   • Exemples de solutions :");
        for (int i = 0; i < displayed; i++) {
            String formatted = solutions.get(i).stream()
                .map(a -> a.variableName() + "=" + a.value())
                .collect(Collectors.joining(", "));
            System.out.println("      • " + formatted);
        }
        if (solutions.size() > displayed) {
            System.out.printf("      • ... (%d solutions supplémentaires)%n",
                solutions.size() - displayed);
        }
    }

    private static void printHeader() {
        System.out.println(SECTION);
        System.out.println("GL-Solver – Démonstration approfondie des fonctionnalités");
        System.out.println(SECTION);
    }

    private static void printFooter() {
        System.out.println();
        System.out.println(SECTION);
        System.out.println("✅ Démonstration terminée – consultez les tests automatisés pour valider les comportements détaillés.");
        System.out.println(SECTION);
    }
}
