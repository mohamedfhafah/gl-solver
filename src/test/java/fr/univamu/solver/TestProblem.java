package fr.univamu.solver;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.ArrayList;

/**
 * Tests unitaires pour le record Problem.
 */
public class TestProblem {

    @Test
    void testProblemCreationValid() {
        // Création d'un problème valide
        var variables = List.of(
            createVariable("A", 1, 9),
            createVariable("B", 0, 9)
        );

        var constraints = List.of(
            new Constraint(ConstraintType.ADD, createVariable("RESULT", 0, 18), variables.get(0), variables.get(1))
        );

        var problem = new Problem(
            variables,
            constraints,
            Problem.CHECK_INTERVALS_STRATEGY,
            1000L,
            true
        );

        assertEquals(2, problem.getVariableCount());
        assertEquals(1, problem.getConstraintCount());
        assertEquals(Problem.CHECK_INTERVALS_STRATEGY, problem.strategy());
        assertEquals(1000L, problem.maxNodes());
        assertTrue(problem.verbose());
    }

    @Test
    void testProblemValidationNullLists() {
        var variables = List.of(createVariable("A", 1, 9));

        // Test variables null
        assertThrows(NullPointerException.class, () -> {
            new Problem(null, List.of(), Problem.CHECK_INTERVALS_STRATEGY, 1000L, true);
        });

        // Test constraints null
        assertThrows(NullPointerException.class, () -> {
            new Problem(variables, null, Problem.CHECK_INTERVALS_STRATEGY, 1000L, true);
        });
    }

    @Test
    void testProblemValidationStrategy() {
        var variables = List.of(createVariable("A", 1, 9));

        // Stratégie invalide
        assertThrows(IllegalArgumentException.class, () -> {
            new Problem(variables, List.of(), 999, 1000L, true);
        });

        // Stratégies valides
        assertDoesNotThrow(() -> {
            new Problem(variables, List.of(), Problem.CHECK_INTERVALS_STRATEGY, 1000L, true);
        });

        assertDoesNotThrow(() -> {
            new Problem(variables, List.of(), Problem.REDUCE_AND_CHECK_INTERVALS_STRATEGY, 1000L, true);
        });
    }

    @Test
    void testProblemValidationMaxNodes() {
        var variables = List.of(createVariable("A", 1, 9));

        // maxNodes négatif
        assertThrows(IllegalArgumentException.class, () -> {
            new Problem(variables, List.of(), Problem.CHECK_INTERVALS_STRATEGY, -1L, true);
        });

        // maxNodes zéro
        assertThrows(IllegalArgumentException.class, () -> {
            new Problem(variables, List.of(), Problem.CHECK_INTERVALS_STRATEGY, 0L, true);
        });

        // maxNodes positif
        assertDoesNotThrow(() -> {
            new Problem(variables, List.of(), Problem.CHECK_INTERVALS_STRATEGY, 1L, true);
        });
    }

    @Test
    void testProblemImmutability() {
        // Test des copies défensives
        var originalVariables = new ArrayList<Variable>();
        originalVariables.add(createVariable("A", 1, 9));

        var originalConstraints = new ArrayList<Constraint>();

        var problem = new Problem(
            originalVariables,
            originalConstraints,
            Problem.CHECK_INTERVALS_STRATEGY,
            1000L,
            true
        );

        // Modification des listes originales ne doit pas affecter le problème
        originalVariables.add(createVariable("B", 0, 5));
        originalConstraints.add(new Constraint(ConstraintType.ADD, createVariable("R", 0, 10),
            originalVariables.get(0), originalVariables.get(1)));

        // Le problème doit avoir conservé ses valeurs originales
        assertEquals(1, problem.getVariableCount());
        assertEquals(0, problem.getConstraintCount());
        assertEquals("A", problem.variables().get(0).getName());
    }

    @Test
    void testProblemEquality() {
        var variables = List.of(createVariable("A", 1, 9));
        var constraints = List.<Constraint>of();

        var problem1 = new Problem(variables, constraints, Problem.CHECK_INTERVALS_STRATEGY, 1000L, true);
        var problem2 = new Problem(variables, constraints, Problem.CHECK_INTERVALS_STRATEGY, 1000L, true);
        var problem3 = new Problem(variables, constraints, Problem.REDUCE_AND_CHECK_INTERVALS_STRATEGY, 1000L, true);

        // Problèmes identiques doivent être égaux
        assertEquals(problem1, problem2);
        assertEquals(problem1.hashCode(), problem2.hashCode());

        // Problèmes avec stratégie différente ne doivent pas être égaux
        assertNotEquals(problem1, problem3);
    }

    @Test
    void testProblemToString() {
        var variables = List.of(createVariable("A", 1, 9));
        var constraints = List.of(new Constraint(ConstraintType.ADD, createVariable("R", 0, 10), variables.get(0), createVariable("B", 0, 9)));

        var problem = new Problem(
            variables,
            constraints,
            Problem.REDUCE_AND_CHECK_INTERVALS_STRATEGY,
            5000L,
            false
        );

        String toString = problem.toString();
        assertTrue(toString.contains("variables=1"));
        assertTrue(toString.contains("constraints=1"));
        assertTrue(toString.contains("strategy=2"));
        assertTrue(toString.contains("maxNodes=5000"));
        assertTrue(toString.contains("verbose=false"));
    }

    @Test
    void testGetVariableByName() {
        var varA = createVariable("A", 1, 9);
        var varB = createVariable("B", 0, 9);
        var variables = List.of(varA, varB);

        var problem = new Problem(variables, List.of(), Problem.CHECK_INTERVALS_STRATEGY, 1000L, true);

        // Variable existante
        assertEquals(varA, problem.getVariableByName("A"));
        assertEquals(varB, problem.getVariableByName("B"));

        // Variable inexistante
        assertNull(problem.getVariableByName("C"));

        // Test null (devrait lever NullPointerException selon la doc)
        assertThrows(NullPointerException.class, () -> {
            problem.getVariableByName(null);
        });
    }

    @Test
    void testHasValidVariables() {
        // Variables valides
        var validVars = List.of(
            createVariable("A", 1, 9),
            createVariable("B", 0, 5)
        );

        var problem1 = new Problem(validVars, List.of(), Problem.CHECK_INTERVALS_STRATEGY, 1000L, true);
        assertTrue(problem1.hasValidVariables());

        // Variable avec domaine vide
        var emptyVar = createVariable("EMPTY", 5, 3); // min > max = domaine vide
        var invalidVars = List.of(createVariable("A", 1, 9), emptyVar);

        var problem2 = new Problem(invalidVars, List.of(), Problem.CHECK_INTERVALS_STRATEGY, 1000L, true);
        assertFalse(problem2.hasValidVariables());
    }

    // Méthode utilitaire pour créer des variables de test
    private Variable createVariable(String name, int min, int max) {
        var v = new Variable(name);
        v.init(min, max);
        return v;
    }
}
