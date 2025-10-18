package com.example.planning;

import fr.univamu.solver.engine.Solver;
import fr.univamu.solver.domain.Variable;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Classe d'exemple montrant comment connecter l'application JavaFX au solver GL.
 * Placez le jar compilé du solver dans {@code demo/javafx-planning/libs/gl-solver.jar}
 * avant de lancer le programme.
 */
public class SolverBridge {

    public Optional<String> solveDemoProblem() {
        List<String> friends = List.of("Alice", "Bruno", "Chloé", "David");
        List<String> activities = List.of("Cinéma", "Escape Game", "Restaurant", "Karaoké");

        int[][] costs = {
            {1, 3, 4, 6},  // Alice
            {4, 1, 5, 3},  // Bruno
            {6, 2, 1, 4},  // Chloé
            {3, 4, 2, 1}   // David
        };

        var solver = new Solver();
        solver.alwaysReduceStrategy();

        int friendCount = friends.size();
        int activityCount = activities.size();
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
        var costVar = solver.newVar("cout", 0, 100);
        solver.addRelation(costExpression, "=", costVar);

        solver.minimize(costVar);
        long solutions = solver.solve();

        if (solutions == 0) {
            return Optional.empty();
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

        StringBuilder builder = new StringBuilder();
        assignment.forEach((friend, activity) ->
            builder.append(friend).append(" -> ").append(activity).append("\n"));
        return Optional.of(builder.toString().trim());
    }
}
