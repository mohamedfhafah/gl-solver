package fr.univamu.solver.engine;

import fr.univamu.solver.domain.Assignment;
import fr.univamu.solver.domain.Variable;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestOptimizationStrategy {

    @Test
    void testAssignmentProblemMinimization() {
        int[][] costs = {
            {8, 5, 9, 9},
            {4, 2, 6, 4},
            {7, 3, 7, 8}
        };

        Solver solver = buildAssignmentProblem(costs);
        solver.getSolutions().setDisplaySolutions(false);

        long optimalSolutions = solver.solve();
        assertEquals(1, optimalSolutions, "Il doit exister une unique affectation optimale.");

        List<List<Assignment>> storedSolutions = solver.getSolutions().getStoredSolutions();
        assertEquals(1, storedSolutions.size(), "Seule la solution optimale doit être conservée.");

        int costValue = storedSolutions.get(0).stream()
            .filter(a -> a.variableName().equals("cost"))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Variable cost absente de la solution"))
            .value();

        assertEquals(15, costValue, "Le coût minimal attendu est 15.");
        assertTrue(solver.getNodesCounter() > 0, "Le solveur doit explorer au moins un nœud.");
    }

    private Solver buildAssignmentProblem(int[][] costs) {
        Solver solver = new Solver();
        int nbTasks = costs.length;
        int nbAgents = costs[0].length;
        Variable[][] matrix = new Variable[nbTasks][nbAgents];

        // create nbTasks * nbAgents variables 0..1
        for (int t = 0; t < nbTasks; t++) {
            for (int a = 0; a < nbAgents; a++) {
                matrix[t][a] = solver.newVar("T" + t + "A" + a, 0, 1);
            }
        }

        // each task have one agent
        var zero = solver.newConstant(0);
        for (int t = 0; t < nbTasks; t++) {
            var sum = zero;
            for (int a = 0; a < nbAgents; a++) {
                sum = solver.expression(sum, "+", matrix[t][a]);
            }
            solver.addRelation(sum, "=", 1);
        }

        // each agent have at most one task
        for (int a = 0; a < nbAgents; a++) {
            var sum = zero;
            for (int t = 0; t < nbTasks; t++) {
                sum = solver.expression(sum, "+", matrix[t][a]);
            }
            solver.addRelation(sum, "<=", 1);
        }

        // compute cost
        var costExpression = zero;
        for (int t = 0; t < nbTasks; t++) {
            for (int a = 0; a < nbAgents; a++) {
                costExpression = solver.expression(costExpression, "+", matrix[t][a], "*", costs[t][a]);
            }
        }
        var costVar = solver.newVar("cost", 0, 1000);
        solver.addRelation(costExpression, "=", costVar);
        solver.setVerbose(false);
        solver.alwaysReduceStrategy();
        solver.minimize(costVar);
        return solver;
    }
}
