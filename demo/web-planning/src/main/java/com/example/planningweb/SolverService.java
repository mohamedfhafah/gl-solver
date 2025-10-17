package com.example.planningweb;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service d'exemple pour montrer comment encapsuler l'appel au solver.
 * Remplacer la logique de démonstration par un véritable problème d'affectation.
 */
public class SolverService {

    public Map<String, Object> solve(PlanningController.PlanningRequest request) {
        // TODO: instancier fr.univamu.solver.engine.Solver, modéliser le problème et retourner la meilleure solution.
        Map<String, Object> response = new HashMap<>();
        response.put("friends", request.friends());
        response.put("activities", request.activities());
        response.put("assignment", List.of(
            Map.of("friend", "Alice", "activity", "Cinéma"),
            Map.of("friend", "Bruno", "activity", "Escape Game")));
        response.put("status", "demo");
        return response;
    }
}
