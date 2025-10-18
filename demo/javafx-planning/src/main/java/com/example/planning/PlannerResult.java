package com.example.planning;

import java.util.List;
import java.util.Map;

public record PlannerResult(List<PlannerSolution> solutions) {
    public record PlannerSolution(int cost,
                                  long exploredNodes,
                                  Map<String, String> assignment,
                                  String description) {
    }
}
