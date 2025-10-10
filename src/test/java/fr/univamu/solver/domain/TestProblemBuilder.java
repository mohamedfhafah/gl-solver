package fr.univamu.solver.domain;

import fr.univamu.solver.api.ProblemBuilder;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

/**
 * Tests unitaires pour le ProblemBuilder.
 */
public class TestProblemBuilder {

    // Méthode utilitaire pour créer des variables de test
    private Variable createVariable(String name, int min, int max) {
        var v = new Variable(name);
        v.init(min, max);
        return v;
    }

    @Test
    void testBuilderConstructionBasique() {
        // Test de construction basique d'un problème simple
        var builder = new ProblemBuilder();

        var varA = createVariable("A", 1, 9);
        var varB = createVariable("B", 0, 9);
        var varResult = createVariable("RESULT", 0, 18);

        var problem = builder
            .addVariable(varA)
            .addVariable(varB)
            .addVariable(varResult)
            .addConstraint(new Constraint(ConstraintType.ADD, varResult, varA, varB))
            .build();

        assertEquals(3, problem.getVariableCount());
        assertEquals(1, problem.getConstraintCount());
        assertEquals(Problem.CHECK_INTERVALS_STRATEGY, problem.strategy());
        assertEquals(1000000000L, problem.maxNodes());
        assertTrue(problem.verbose());
    }

    @Test
    void testBuilderValidationNull() {
        var builder = new ProblemBuilder();

        // Test ajout de variable null
        assertThrows(IllegalArgumentException.class, () -> {
            builder.addVariable(null);
        });

        // Test ajout de contrainte null
        assertThrows(IllegalArgumentException.class, () -> {
            builder.addConstraint(null);
        });
    }

    @Test
    void testBuilderValidationStrategy() {
        var builder = new ProblemBuilder();
        var varA = createVariable("A", 1, 9);

        // Stratégie invalide
        assertThrows(IllegalArgumentException.class, () -> {
            builder.setStrategy(999);
        });

        // Stratégies valides
        assertDoesNotThrow(() -> {
            builder.addVariable(varA)
                  .setStrategy(Problem.CHECK_INTERVALS_STRATEGY)
                  .setStrategy(Problem.REDUCE_AND_CHECK_INTERVALS_STRATEGY);
        });
    }

    @Test
    void testBuilderValidationMaxNodes() {
        var builder = new ProblemBuilder();
        var varA = createVariable("A", 1, 9);

        // maxNodes négatif
        assertThrows(IllegalArgumentException.class, () -> {
            builder.setMaxNodes(-1);
        });

        // maxNodes zéro
        assertThrows(IllegalArgumentException.class, () -> {
            builder.setMaxNodes(0);
        });

        // maxNodes positif
        assertDoesNotThrow(() -> {
            builder.addVariable(varA).setMaxNodes(1000);
        });
    }

    @Test
    void testBuilderBuildValidation() {
        var builder = new ProblemBuilder();

        // Tentative de build sans variables
        assertThrows(IllegalStateException.class, () -> {
            builder.build();
        });

        // Build réussi avec au moins une variable
        assertDoesNotThrow(() -> {
            builder.addVariable(createVariable("A", 1, 9)).build();
        });
    }

    @Test
    void testBuilderChaining() {
        // Test du chaînage fluide complet
        var builder = new ProblemBuilder();

        var varA = createVariable("A", 1, 9);
        var varB = createVariable("B", 0, 9);
        var varResult = createVariable("RESULT", 0, 18);

        // Construction en une seule chaîne
        var problem = builder
            .addVariable(varA)
            .addVariable(varB)
            .addVariable(varResult)
            .addConstraint(new Constraint(ConstraintType.ADD, varResult, varA, varB))
            .addConstraint(new Constraint(ConstraintType.MUL, varResult, varA, varB))
            .setStrategy(Problem.REDUCE_AND_CHECK_INTERVALS_STRATEGY)
            .setMaxNodes(5000)
            .setVerbose(false)
            .build();

        // Vérifications
        assertEquals(3, problem.getVariableCount());
        assertEquals(2, problem.getConstraintCount());
        assertEquals(Problem.REDUCE_AND_CHECK_INTERVALS_STRATEGY, problem.strategy());
        assertEquals(5000L, problem.maxNodes());
        assertFalse(problem.verbose());
    }

    @Test
    void testBuilderGetters() {
        var builder = new ProblemBuilder();

        // Vérification des compteurs initiaux
        assertEquals(0, builder.getVariableCount());
        assertEquals(0, builder.getConstraintCount());

        // Ajout et vérification
        builder.addVariable(createVariable("A", 1, 9));
        builder.addConstraint(new Constraint(ConstraintType.ADD, createVariable("R", 0, 10),
                          createVariable("A", 1, 9), createVariable("B", 0, 9)));

        assertEquals(1, builder.getVariableCount());
        assertEquals(1, builder.getConstraintCount());
    }

    @Test
    void testBuilderReutilisation() {
        var builder = new ProblemBuilder();

        // Premier build
        var problem1 = builder
            .addVariable(createVariable("A", 1, 9))
            .build();

        assertEquals(1, problem1.getVariableCount());

        // Le builder devrait être réinitialisé pour le prochain usage
        // (en fait, il continue d'accumuler, donc on teste cela)
        var problem2 = builder
            .addVariable(createVariable("B", 0, 9))
            .build();

        assertEquals(2, problem2.getVariableCount()); // Variables accumulées
    }

    @Test
    void testComplexProblemConstruction() {
        // Test de construction d'un problème plus complexe (comme un cryptarithme)
        var builder = new ProblemBuilder();

        // Variables pour AB + CD = EF (A,B,C,D,E,F dans 0-9, A,C,E ≠ 0)
        var A = createVariable("A", 1, 9);
        var B = createVariable("B", 0, 9);
        var C = createVariable("C", 1, 9);
        var D = createVariable("D", 0, 9);
        var E = createVariable("E", 1, 9);
        var F = createVariable("F", 0, 9);

        // Variables intermédiaires pour AB, CD, EF
        var AB = createVariable("AB", 10, 99);    // AB entre 10-99
        var CD = createVariable("CD", 10, 99);    // CD entre 10-99
        var EF = createVariable("EF", 10, 99);    // EF entre 10-99

        var problem = builder
            // Ajout de toutes les variables
            .addVariable(A).addVariable(B).addVariable(C).addVariable(D)
            .addVariable(E).addVariable(F).addVariable(AB).addVariable(CD).addVariable(EF)
            // Contraintes arithmétiques
            .addConstraint(new Constraint(ConstraintType.ADD, AB, A, B))  // AB = A*10 + B
            .addConstraint(new Constraint(ConstraintType.ADD, CD, C, D))  // CD = C*10 + D
            .addConstraint(new Constraint(ConstraintType.ADD, EF, E, F))  // EF = E*10 + F
            .addConstraint(new Constraint(ConstraintType.ADD, EF, AB, CD)) // EF = AB + CD
            // Contrainte all-different (simplifiée pour le test)
            .addConstraint(new Constraint(ConstraintType.DIFF, A, B, null)) // A ≠ B
            // Configuration
            .setStrategy(Problem.REDUCE_AND_CHECK_INTERVALS_STRATEGY)
            .setMaxNodes(1000000)
            .setVerbose(false)
            .build();

        // Vérifications
        assertEquals(9, problem.getVariableCount());
        assertEquals(5, problem.getConstraintCount());
        assertEquals(Problem.REDUCE_AND_CHECK_INTERVALS_STRATEGY, problem.strategy());
        assertEquals(1000000L, problem.maxNodes());
        assertFalse(problem.verbose());
    }
}
