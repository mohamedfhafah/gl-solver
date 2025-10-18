package com.example.planningweb;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SolverServiceTest {

    private final SolverService solverService = new SolverService();

    @Test
    void shouldReturnSolutionForBasicRequest() {
        var request = new PlanningController.PlanningRequest(
            List.of("Alice", "Bruno"),
            List.of("Cinéma", "Escape")
        );

        var response = solverService.solve(request);
        assertEquals("ok", response.get("status"));
        @SuppressWarnings("unchecked")
        var minCost = (java.util.Map<String, Object>) response.get("minCost");
        assertNotNull(minCost);
        assertTrue(minCost.containsKey("assignment"));
    }
}
