package fr.univamu.solver.domain;

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
    void testHasValidVariables() {
        var validVar = createVariable("A", 1, 9);
        var emptyVar = createVariable("B", 5, 3); // vide

        var problemValid = new Problem(List.of(validVar), List.of(), Problem.CHECK_INTERVALS_STRATEGY, 10L, false);
        assertTrue(problemValid.hasValidVariables());

        var problemInvalid = new Problem(List.of(emptyVar), List.of(), Problem.CHECK_INTERVALS_STRATEGY, 10L, false);
        assertFalse(problemInvalid.hasValidVariables());
    }

    @Test
    void testGetVariableByName() {
        var a = createVariable("A", 0, 1);
        var _1 = createVariable("B", 2, 3);
        var problem = new Problem(List.of(a, _1), List.of(), Problem.CHECK_INTERVALS_STRATEGY, 10L, false);

        assertSame(a, problem.getVariableByName("A"));
        assertSame(_1, problem.getVariableByName("B"));
        assertNull(problem.getVariableByName("UNKNOWN"));
        assertThrows(NullPointerException.class, () -> problem.getVariableByName(null));
    }

    @Test
    void testReturnedListsAreUnmodifiable() {
        var a = createVariable("A", 0, 1);
        var constraint = new Constraint(ConstraintType.ADD, createVariable("R", 0, 2), a, createVariable("B", 0, 1));
        var problem = new Problem(List.of(a), List.of(constraint), Problem.CHECK_INTERVALS_STRATEGY, 10L, false);

        assertThrows(UnsupportedOperationException.class, () -> problem.variables().add(createVariable("C", 0, 1)));
        assertThrows(UnsupportedOperationException.class, () -> problem.constraints().add(constraint));
    }

    // Méthode utilitaire pour créer des variables de test
    private Variable createVariable(String name, int min, int max) {
        var v = new Variable(name);
        v.init(min, max);
        return v;
    }
}
