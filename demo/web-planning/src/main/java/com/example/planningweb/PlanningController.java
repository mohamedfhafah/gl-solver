package com.example.planningweb;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Contrôleur REST minimal. À compléter avec une vraie modélisation.
 */
@RestController
public class PlanningController {

    private final SolverService solverService = new SolverService();

    @PostMapping("/planifier")
    public ResponseEntity<Map<String, Object>> planifier(@RequestBody PlanningRequest request) {
        var result = solverService.solve(request);
        return ResponseEntity.ok(result);
    }

    public record PlanningRequest(List<String> friends,
                                  List<String> activities,
                                  Map<String, Map<String, Integer>> preferences) {
    }
}
