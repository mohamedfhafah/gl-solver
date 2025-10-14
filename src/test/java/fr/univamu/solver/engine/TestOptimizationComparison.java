package fr.univamu.solver.engine;

import fr.univamu.solver.domain.Variable;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests de comparaison entre diverses stratégies d'optimisation.
 * L'objectif est de vérifier que les optimisations conservent les solutions
 * tout en réduisant (ou en ne dégradant pas) la complexité structurelle.
 */
public class TestOptimizationComparison {

    private record OptimizationResult(long solutions, long nodes, int variables, int constraints) {}

    private OptimizationResult measurePerformance(Solver solver) {
        long startNodes = solver.getNodesCounter();
        long solutions = solver.solve();
        return new OptimizationResult(
            solutions,
            solver.getNodesCounter() - startNodes,
            solver.getVariables().size(),
            solver.getConstraints().size()
        );
    }

    @Test
    void testLinearExpressionKeepsSolutionsAcrossOptimizations() {
        var scenario = (ConsumerWithSolver) solver -> {
            var a = solver.newVar("A", 0, 9);
            var b = solver.newVar("B", 0, 9);
            var expr = solver.expression(a, "+", 2);
            solver.addRelation(expr, "=", b);
        };

        Solver normal = new Solver();
        scenario.accept(normal);
        OptimizationResult normalResult = measurePerformance(normal);

        Solver optimized = new Solver();
        scenario.accept(optimized);
        optimized.optimize();
        OptimizationResult optimizedResult = measurePerformance(optimized);

        assertEquals(normalResult.solutions(), optimizedResult.solutions(), "Les optimisations doivent préserver les solutions");
        assertTrue(optimizedResult.nodes() <= normalResult.nodes(), "Les optimisations ne doivent pas augmenter le nombre de nœuds");
    }

    @Test
    void testOptimizationReducesIntermediatesWhenPossible() {
        Solver solver = new Solver();
        var a = solver.newVar("A", 0, 5);
        var b = solver.newVar("B", 0, 10);
        var expr = solver.expression(a, "+", 3);
        solver.addRelation(expr, "=", b);

        OptimizationResult normalResult = measurePerformance(solver);

        Solver optimized = new Solver();
        var a2 = optimized.newVar("A", 0, 5);
        var b2 = optimized.newVar("B", 0, 10);
        var expr2 = optimized.expressionOptimized(a2, "+", 3);
        optimized.addRelation(expr2, "=", b2);
        optimized.optimize();

        OptimizationResult optimizedResult = measurePerformance(optimized);

        assertTrue(optimizedResult.variables() <= normalResult.variables(), "L'optimisation doit supprimer les variables intermédiaires inutiles");
        assertEquals(normalResult.solutions(), optimizedResult.solutions());
    }

    @Test
    void testConstantDomainReductionIsPreserved() {
        Solver solver = new Solver();
        var x = solver.newVar("X", 0, 20);
        solver.addRelation(x, ">", 10);
        OptimizationResult result = measurePerformance(solver);

        assertEquals(10, result.solutions(), "X > 10 devrait laisser 10 valeurs possibles (11..20)");
        assertEquals(11, x.getMin());
        assertEquals(0, result.constraints());
    }

    @FunctionalInterface
    private interface ConsumerWithSolver {
        void accept(Solver solver);
    }
}

