package com.example.planningweb;

import fr.univamu.solver.domain.Variable;
import fr.univamu.solver.engine.Solver;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service illustrant l'utilisation du solver GL côté backend.
 */
public class SolverService {

    private final PreferenceDataset dataset = PreferencesLoader.loadDefaultDataset();

    public Map<String, Object> solve(PlanningController.PlanningRequest request) {
        List<String> friends = (request.friends() == null || request.friends().isEmpty())
            ? dataset.friends().stream().map(PreferenceDataset.PreferenceEntry::name).toList()
            : request.friends();
        List<String> activities = (request.activities() == null || request.activities().isEmpty())
            ? dataset.activities()
            : request.activities();

        int[][] baseCosts = buildCostsMatrix(friends, activities);
        Optional<SolutionSummary> minCost = solveWithCosts(friends, activities, baseCosts, "cost");
        if (minCost.isEmpty()) {
            return Map.of("status", "no_solution");
        }

        int[][] balancedCosts = buildBalancedCosts(baseCosts);
        Optional<SolutionSummary> balanced = solveWithCosts(friends, activities, balancedCosts, "balanced_cost");

        Map<String, Object> response = new HashMap<>();
        response.put("status", "ok");
        response.put("friends", friends);
        response.put("activities", activities);
        response.put("minCost", minCost.get().toMap());
        response.put("balanced", balanced.map(SolutionSummary::toMap).orElse(null));
        response.put("group", Map.of("suggestions", buildGroupSuggestions(friends, activities, baseCosts)));
        return response;
    }

    private Optional<SolutionSummary> solveWithCosts(List<String> friends,
                                                     List<String> activities,
                                                     int[][] costs,
                                                     String costVarName) {
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
        var costVar = solver.newVar(costVarName, 0, 10_000);
        solver.addRelation(costExpression, "=", costVar);
        solver.minimize(costVar);

        long solutionCount = solver.solve();
        if (solutionCount == 0) {
            return Optional.empty();
        }

        var storedSolutions = solver.getSolutions().getStoredSolutions();
        var best = storedSolutions.get(0);

        Map<String, String> assignment = new LinkedHashMap<>();
        for (int f = 0; f < friendCount; f++) {
            final String friendName = friends.get(f);
            for (int a = 0; a < activityCount; a++) {
                final String activityName = activities.get(a);
                String varName = "F" + f + "A" + a;
                best.stream()
                    .filter(assignmentVar -> assignmentVar.variableName().equals(varName))
                    .findFirst()
                    .ifPresent(assignmentVar -> {
                        if (assignmentVar.value() == 1) {
                            assignment.put(friendName, activityName);
                        }
                    });
            }
        }

        int costValue = best.stream()
            .filter(a -> a.variableName().equals(costVarName))
            .findFirst()
            .map(a -> a.value())
            .orElse(0);

        return Optional.of(new SolutionSummary(costValue, solver.getNodesCounter(), assignment, solutionCount));
    }

    private int[][] buildCostsMatrix(List<String> friends, List<String> activities) {
        var datasetMap = dataset.friends().stream()
            .collect(Collectors.toMap(PreferenceDataset.PreferenceEntry::name, PreferenceDataset.PreferenceEntry::preferences));

        int[][] costs = new int[friends.size()][activities.size()];
        for (int f = 0; f < friends.size(); f++) {
            String friendName = friends.get(f);
            var preferences = datasetMap.get(friendName);

            for (int a = 0; a < activities.size(); a++) {
                String activity = activities.get(a);
                int defaultCost = (f + a) % 5 + 1;
                int cost = preferences != null ? preferences.getOrDefault(activity, defaultCost) : defaultCost;
                costs[f][a] = cost;
            }
        }
        return costs;
    }

    private int[][] buildBalancedCosts(int[][] baseCosts) {
        int friends = baseCosts.length;
        int activities = baseCosts[0].length;
        int[][] balanced = new int[friends][activities];

        double[] averages = new double[friends];
        for (int f = 0; f < friends; f++) {
            double sum = 0;
            for (int a = 0; a < activities; a++) {
                sum += baseCosts[f][a];
            }
            averages[f] = sum / activities;
        }

        for (int f = 0; f < friends; f++) {
            for (int a = 0; a < activities; a++) {
                int diff = (int)Math.abs(baseCosts[f][a] - averages[f]);
                balanced[f][a] = diff * 4 + baseCosts[f][a];
            }
        }
        return balanced;
    }

    private List<Map<String, Object>> buildGroupSuggestions(List<String> friends,
                                                            List<String> activities,
                                                            int[][] costs) {
        int friendCount = friends.size();
        int activityCount = activities.size();

        List<GroupScore> scores = new ArrayList<>();
        for (int a = 0; a < activityCount; a++) {
            int total = 0;
            int worst = 0;
            Map<String, Integer> details = new LinkedHashMap<>();
            for (int f = 0; f < friendCount; f++) {
                int value = costs[f][a];
                total += value;
                worst = Math.max(worst, value);
                details.put(friends.get(f), value);
            }
            double average = friendCount == 0 ? 0 : (double) total / friendCount;
            scores.add(new GroupScore(a, total, average, worst, details));
        }

        scores.sort(Comparator.comparingInt(GroupScore::total));
        List<Map<String, Object>> result = new ArrayList<>();
        int limit = Math.min(3, scores.size());
        for (int i = 0; i < limit; i++) {
            GroupScore score = scores.get(i);
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("activity", activities.get(score.index()));
            payload.put("total", score.total());
            payload.put("average", score.average());
            payload.put("worst", score.worst());
            payload.put("details", score.details());
            result.add(payload);
        }
        return result;
    }

    private record SolutionSummary(int cost, long nodes, Map<String, String> assignment, long solutions) {
        Map<String, Object> toMap() {
            return Map.of(
                "cost", cost,
                "nodes", nodes,
                "assignment", assignment,
                "solutions", solutions
            );
        }
    }

    private record GroupScore(int index, int total, double average, int worst, Map<String, Integer> details) {
    }
}
