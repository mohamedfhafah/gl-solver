package com.example.planning;

import fr.univamu.solver.domain.Variable;
import fr.univamu.solver.engine.Solver;

import java.util.ArrayList;
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

    private final PreferenceDataset dataset = PreferencesLoader.loadDefaultDataset();

    public PreferenceDataset getDataset() {
        return dataset;
    }

    public Optional<PlannerResult> solveDefault() {
        var friendNames = dataset.friends().stream().map(PreferenceDataset.PreferenceEntry::name).toList();
        int[][] costs = new int[friendNames.size()][dataset.activities().size()];
        for (int f = 0; f < friendNames.size(); f++) {
            var entry = dataset.friends().get(f);
            for (int a = 0; a < dataset.activities().size(); a++) {
                String activity = dataset.activities().get(a);
                costs[f][a] = entry.preferences().getOrDefault(activity, 5);
            }
        }
        return solve(friendNames, dataset.activities(), costs);
    }

    public Optional<PlannerResult> solve(List<String> friends,
                                         List<String> activities,
                                         int[][] costs) {
        int friendCount = friends.size();
        int activityCount = activities.size();

        var solver = new Solver();
        solver.alwaysReduceStrategy();
        solver.getSolutions().setDisplaySolutions(false);

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
        var costVar = solver.newVar("cout", 0, 1000);
        solver.addRelation(costExpression, "=", costVar);
        solver.minimize(costVar);

        long solutions = solver.solve();
        if (solutions == 0) {
            return Optional.empty();
        }

        var storedSolutions = solver.getSolutions().getStoredSolutions();
        List<PlannerResult.PlannerSolution> plannerSolutions = new ArrayList<>();
        int limit = Math.min(3, storedSolutions.size());

        long nodes = solver.getNodesCounter();
        for (int idx = 0; idx < limit; idx++) {
            var solution = storedSolutions.get(idx);
            int cost = solution.stream()
                .filter(assignment -> assignment.variableName().equals("cout"))
                .findFirst()
                .map(a -> a.value())
                .orElse(0);

            Map<String, String> assignment = new LinkedHashMap<>();
            for (int f = 0; f < friendCount; f++) {
                final String friendName = friends.get(f);
                for (int a = 0; a < activityCount; a++) {
                    final String activityName = activities.get(a);
                    String varName = "F" + f + "A" + a;
                    solution.stream()
                        .filter(assignmentVar -> assignmentVar.variableName().equals(varName))
                        .findFirst()
                        .ifPresent(assignmentVar -> {
                            if (assignmentVar.value() == 1) {
                                assignment.put(friendName, activityName);
                            }
                        });
                }
            }

            plannerSolutions.add(new PlannerResult.PlannerSolution(cost, nodes, assignment,
                "Planning individuel (coût total)") );
        }

        return Optional.of(new PlannerResult(plannerSolutions));
    }

    public Optional<PlannerResult> solveGroup(List<String> friends,
                                              List<String> activities,
                                              int[][] costs) {
        int activityCount = activities.size();
        int friendCount = friends.size();
        int[] aggregatedCosts = new int[activityCount];
        for (int a = 0; a < activityCount; a++) {
            int sum = 0;
            for (int f = 0; f < friendCount; f++) {
                sum += costs[f][a];
            }
            aggregatedCosts[a] = sum;
        }

        List<PlannerResult.PlannerSolution> solutions = new ArrayList<>();
        List<Integer> excluded = new ArrayList<>();

        for (int iteration = 0; iteration < Math.min(3, activityCount); iteration++) {
            var solver = new Solver();
            solver.alwaysReduceStrategy();
            solver.getSolutions().setDisplaySolutions(false);

            Variable[] chosen = new Variable[activityCount];
            for (int a = 0; a < activityCount; a++) {
                chosen[a] = solver.newVar("ACT" + a, 0, 1);
            }

            var sum = solver.newConstant(0);
            for (Variable variable : chosen) {
                sum = solver.expression(sum, "+", variable);
            }
            solver.addRelation(sum, "=", 1);

            for (int banned : excluded) {
                solver.addRelation(chosen[banned], "=", 0);
            }

            var costExpression = solver.newConstant(0);
            for (int a = 0; a < activityCount; a++) {
                costExpression = solver.expression(costExpression, "+", chosen[a], "*", aggregatedCosts[a]);
            }
            var costVar = solver.newVar("cout_group", 0, 10_000);
            solver.addRelation(costExpression, "=", costVar);
            solver.minimize(costVar);

            long solved = solver.solve();
            if (solved == 0) {
                break;
            }

            var solution = solver.getSolutions().getStoredSolutions().get(0);
            int chosenIndex = -1;
            for (int a = 0; a < activityCount; a++) {
                String varName = "ACT" + a;
                int value = solution.stream()
                    .filter(assignment -> assignment.variableName().equals(varName))
                    .findFirst()
                    .map(aVar -> aVar.value())
                    .orElse(0);
                if (value == 1) {
                    chosenIndex = a;
                    break;
                }
            }

            if (chosenIndex < 0) {
                break;
            }

            int cost = solution.stream()
                .filter(assignment -> assignment.variableName().equals("cout_group"))
                .findFirst()
                .map(a -> a.value())
                .orElse(0);

            String activityName = activities.get(chosenIndex);
            Map<String, String> assignment = new LinkedHashMap<>();
            for (String friend : friends) {
                assignment.put(friend, activityName);
            }

            solutions.add(new PlannerResult.PlannerSolution(cost, solver.getNodesCounter(), assignment,
                "Sortie commune : " + activityName));
            excluded.add(chosenIndex);
        }

        return solutions.isEmpty() ? Optional.empty() : Optional.of(new PlannerResult(solutions));
    }
}
