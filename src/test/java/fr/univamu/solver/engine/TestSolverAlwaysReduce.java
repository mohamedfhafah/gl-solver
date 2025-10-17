package fr.univamu.solver.engine;

import fr.univamu.solver.domain.Variable;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests dédiés à la stratégie alwaysReduce.
 */
public class TestSolverAlwaysReduce {

    @Test
    void testAlwaysReduceStrategyFindsAllEightQueensSolutions() {
        Solver baseline = buildQueensSolver(8);
        baseline.reduceAndCheckIntervalsStrategy();
        baseline.setVerbose(false);
        baseline.getSolutions().setDisplaySolutions(false);
        long baselineSolutions = baseline.solve();
        long baselineNodes = baseline.getNodesCounter();

        Solver alwaysReduceSolver = buildQueensSolver(8);
        alwaysReduceSolver.alwaysReduceStrategy();
        alwaysReduceSolver.setVerbose(false);
        alwaysReduceSolver.getSolutions().setDisplaySolutions(false);
        long optimizedSolutions = alwaysReduceSolver.solve();
        long optimizedNodes = alwaysReduceSolver.getNodesCounter();

        assertEquals(92, optimizedSolutions, "Le problème des 8 reines doit avoir 92 solutions.");
        assertEquals(baselineSolutions, optimizedSolutions, "La nouvelle stratégie doit préserver le nombre de solutions.");
        assertTrue(optimizedNodes < baselineNodes,
            () -> String.format("alwaysReduce doit explorer moins de nœuds (baseline=%d, alwaysReduce=%d)",
                baselineNodes, optimizedNodes));
    }

    private Solver buildQueensSolver(int n) {
        Solver solver = new Solver();
        var queens = new Variable[n];
        var diagonals1 = new Variable[n];
        var diagonals2 = new Variable[n];

        for (int i = 0; i < n; i++) {
            queens[i] = solver.newVar("R" + i, 1, n);
            diagonals1[i] = solver.expression(queens[i], "+", i);
            diagonals2[i] = solver.expression(queens[i], "-", i);
        }

        solver.addAllDiffRelation(queens);
        solver.addAllDiffRelation(diagonals1);
        solver.addAllDiffRelation(diagonals2);
        solver.setVerbose(false);
        solver.getSolutions().setDisplaySolutions(false);
        return solver;
    }
}
