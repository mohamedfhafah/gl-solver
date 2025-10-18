package com.example.planningweb;

import fr.univamu.solver.domain.Variable;
import fr.univamu.solver.engine.Solver;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Service d'exemple pour montrer comment encapsuler l'appel au solver.
 * Remplacer la logique de démonstration par un véritable problème d'affectation.
 */
public class SolverService {

    public Map<String, Object> solve(PlanningController.PlanningRequest request) {
        List<String> friends = request.friends();
        List<String> activities = request.activities();
        if (friends == null || activities == null || friends.isEmpty() || activities.isEmpty()) {
            return Map.of("status", "error", "message", "Friends and activities must be provided");
        }

        int friendCount = friends.size();
        int activityCount = activities.size();
        int[][] costs = buildDefaultCosts(friendCount, activityCount);

        var solver = new Solver();
        solver.alwaysReduceStrategy();

        Variable[][] matrix = new Variable[friendCount][activityCount];
        for (int f = 0; f < friendCount; f++) {
            for (int a = 0; a < activityCount; a++) {
                matrix[f][a] = solver.newVar("F" + f + "A" + a, 0, 1);
            }
        }

        var zero = solver.newConstant(0);

        for (int f = 0; f < friendCount; f++) {
            var sum = zero;
            for (int a = 0; a < activityCount; a++) {
                sum = solver.expression(sum, "+", matrix[f][a]);
            }
            solver.addRelation(sum, "=", 1);
        }

        for (int a = 0; a < activityCount; a++) {
            var sum = zero;
            for (int f = 0; f < friendCount; f++) {
                sum = solver.expression(sum, "+", matrix[f][a]);
            }
            solver.addRelation(sum, "<=", 1);
        }

        var costExpression = zero;
        for (int f = 0; f < friendCount; f++) {
            for (int a = 0; a < activityCount; a++) {
                costExpression = solver.expression(costExpression, "+", matrix[f][a], "*", costs[f][a]);
            }
        }
        var costVar = solver.newVar("cost", 0, 1000);
        solver.addRelation(costExpression, "=", costVar);
        solver.minimize(costVar);

        long solutionCount = solver.solve();
        if (solutionCount == 0) {
            return Map.of("status", "no_solution");
        }

        Map<String, String> assignment = new LinkedHashMap<>();
        var storedSolutions = solver.getSolutions().getStoredSolutions();
        if (!storedSolutions.isEmpty()) {
            var best = storedSolutions.get(0);
            for (int f = 0; f < friendCount; f++) {
                for (int a = 0; a < activityCount; a++) {
                    String varName = "F" + f + "A" + a;
                    best.stream()
                        .filter(assignmentVar -> assignmentVar.variableName().equals(varName))
                        .findFirst()
                        .ifPresent(assignmentVar -> {
                            if (assignmentVar.value() == 1) {
                                assignment.put(friends.get(f), activities.get(a));
                            }
                        });
                }
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("status", "ok");
        response.put("friends", friends);
        response.put("activities", activities);
        response.put("assignment", assignment);
        response.put("optimalCost", solver.getSolutions().getStoredSolutions().isEmpty() ? null
            : solver.getSolutions().getStoredSolutions().get(0).stream()
                .filter(a -> a.variableName().equals("cost"))
                .findFirst().map(a -> a.value()).orElse(null));

        return response;
    }

    private int[][] buildDefaultCosts(int friendCount, int activityCount) {
        int[][] costs = new int[friendCount][activityCount];
        for (int f = 0; f < friendCount; f++) {
            for (int a = 0; a < activityCount; a++) {
                costs[f][a] = (f + a) % 5 + 1;
            }
        }
        return costs;
    }
}
