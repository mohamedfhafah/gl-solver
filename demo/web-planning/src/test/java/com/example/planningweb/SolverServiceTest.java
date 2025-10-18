package com.example.planningweb;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class SolverServiceTest {

    private final SolverService solverService = new SolverService();

    @Test
    void shouldReturnSolutionForBasicRequest() {
        var request = new PlanningController.PlanningRequest(
            List.of("Alice", "Bruno"),
            List.of("Cinéma", "Escape"),
            Map.of(
                "Alice", Map.of("Cinéma", 1, "Escape", 5),
                "Bruno", Map.of("Cinéma", 4, "Escape", 1)
            )
        );

        var response = solverService.solve(request);
        assertEquals("ok", response.get("status"));
        @SuppressWarnings("unchecked")
        var minCost = (java.util.Map<String, Object>) response.get("minCost");
        assertNotNull(minCost);
        assertTrue(minCost.containsKey("assignment"));

        @SuppressWarnings("unchecked")
        var group = (java.util.Map<String, Object>) response.get("group");
        assertNotNull(group);
        assertTrue(group.containsKey("suggestions"));
    }
}
