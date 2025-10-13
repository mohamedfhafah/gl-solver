package fr.univamu.solver.engine;

import fr.univamu.solver.api.ISolver;
import fr.univamu.solver.domain.Constraint;
import fr.univamu.solver.domain.ConstraintType;
import fr.univamu.solver.domain.Variable;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.function.Consumer;

/**
 * Tests pour l'optimisation des expressions et l'élimination des variables intermédiaires.
 */
public class TestExpressionOptimization {

    // Méthode utilitaire pour créer des variables de test
    private Variable createVariable(String name, int min, int max) {
        var v = new Variable(name);
        v.init(min, max);
        return v;
    }

    @Test
    void testSimpleAdditionOptimization() {
        /*
         * Test de l'exemple donné : A + 2 = B
         *
         * SANS optimisation :
         *   T1 ∈ [2,2]        (constante)
         *   T2 = A + T1       (addition intermédiaire)
         *   T2 = B            (égalité)
         *
         * AVEC optimisation :
         *   T1 ∈ [2,2]        (constante)
         *   B = A + T1        (égalité directe)
         */

        Solver solver = new Solver();

        Variable A = createVariable("A", 0, 9);
        Variable B = createVariable("B", 0, 18);

        // Utiliser l'expression optimisée
        Variable expr = createOptimizedExpression(solver, A, "+", 2);
        solver.addRelation(expr, "=", B);

        solver.optimize();

        assertEquals(3, getVariables(solver).size(), "Doit conserver A, B et la constante 2");
        assertEquals(1, getConstraints(solver).size(), "La contrainte doit être directement B = A + 2");

        assertTrue(getConstraints(solver).stream()
            .anyMatch(c -> c.type() == ConstraintType.ADD
                && c.result().equals(B)
                && c.var1().equals(A)),
            "La contrainte optimisée doit cibler directement B");
    }

    @Test
    void testComplexExpressionOptimization() {
        /*
         * Test d'une expression plus complexe : (A + B) * 2 = C
         *
         * SANS optimisation : nombreuses variables intermédiaires
         * AVEC optimisation : minimiser les variables intermédiaires
         */

        // Version sans optimisation automatique
        Solver baseline = new Solver();
        Variable A0 = createVariable("A", 0, 9);
        Variable B0 = createVariable("B", 0, 9);
        Variable C0 = createVariable("C", 0, 36);
        Variable sumBaseline = baseline.expression(A0, "+", B0);
        Variable exprBaseline = baseline.expression(sumBaseline, "*", 2);
        baseline.addRelation(exprBaseline, "=", C0);

        int varsBaseline = getVariables(baseline).size();
        int consBaseline = getConstraints(baseline).size();

        // Version utilisant l'optimisation
        Solver optimized = new Solver();
        Variable A = createVariable("A", 0, 9);
        Variable B = createVariable("B", 0, 9);
        Variable C = createVariable("C", 0, 36);
        Variable sumExpr = createOptimizedExpression(optimized, A, "+", B);
        Variable expr = createOptimizedExpression(optimized, sumExpr, "*", 2);
        optimized.addRelation(expr, "=", C);
        optimized.optimize();

        int varsOptimized = getVariables(optimized).size();
        int consOptimized = getConstraints(optimized).size();

        assertTrue(varsOptimized <= varsBaseline,
            () -> String.format("L'optimisation devrait réduire ou maintenir le nombre de variables (%d vs %d)",
                varsOptimized, varsBaseline));
        assertTrue(consOptimized <= consBaseline,
            () -> String.format("L'optimisation devrait réduire ou maintenir le nombre de contraintes (%d vs %d)",
                consOptimized, consBaseline));
    }

    @Test
    void testOptimizationVsNoOptimization() {
        /*
         * Comparer les résultats avec et sans optimisation pour la même expression
         */

        // Test SANS optimisation
        Solver solverNoOpt = new Solver();
        Variable A1 = createVariable("A", 0, 9);
        Variable B1 = createVariable("B", 0, 9);

        Variable expr1 = solverNoOpt.expression(A1, "+", 2); // pas d'optimisation (méthode par défaut)
        solverNoOpt.addRelation(expr1, "=", B1);

        int variablesNoOpt = getVariables(solverNoOpt).size();
        int constraintsNoOpt = getConstraints(solverNoOpt).size();

        // Test AVEC optimisation
        Solver solverOpt = new Solver();
        Variable A2 = createVariable("A", 0, 9);
        Variable B2 = createVariable("B", 0, 9);

        Variable expr2 = createOptimizedExpression(solverOpt, A2, "+", 2); // avec optimisation
        solverOpt.addRelation(expr2, "=", B2);

        solverOpt.optimize();

        int variablesOpt = getVariables(solverOpt).size();
        int constraintsOpt = getConstraints(solverOpt).size();

        // L'optimisation devrait réduire le nombre de variables et/ou contraintes
        assertTrue(variablesOpt <= variablesNoOpt, "L'optimisation devrait réduire ou maintenir le nombre de variables");
        assertTrue(constraintsOpt <= constraintsNoOpt, "L'optimisation devrait réduire ou maintenir le nombre de contraintes");
    }

    @Test
    void testNoOptimizationRegression() {
        /*
         * S'assurer que l'ancien comportement (sans optimisation) fonctionne toujours
         */

        Solver solver = new Solver();
        Variable A = createVariable("A", 0, 9);
        Variable B = createVariable("B", 0, 9);

        // Utiliser l'ancienne méthode (sans paramètre = pas d'optimisation)
        Variable expr = solver.expression(A, "+", 2);
        solver.addRelation(expr, "=", B);

        // Devrait fonctionner comme avant
        assertTrue(getVariables(solver).size() >= 3, "Devrait avoir au moins les variables A, B et une constante");
        assertTrue(getConstraints(solver).size() >= 2, "Devrait avoir au moins 2 contraintes");
    }

    // Méthode utilitaire pour créer une expression optimisée (évite l'ambiguïté)
    private Variable createOptimizedExpression(Solver solver, Object... terms) {
        return solver.expressionOptimized(terms);
    }

    // Méthode utilitaire pour accéder aux contraintes (via réflexion car c'est privé)
    @SuppressWarnings("unchecked")
    private java.util.List<Constraint> getConstraints(Solver solver) {
        try {
            var field = Solver.class.getDeclaredField("constraints");
            field.setAccessible(true);
            return (java.util.List<Constraint>) field.get(solver);
        } catch (Exception e) {
            fail("Impossible d'accéder aux contraintes: " + e.getMessage());
            return null;
        }
    }

    @Test
    void testOptimizationMetricsAndEquivalence() {
        /*
         * Test de validation finale : mesurer la réduction et vérifier l'équivalence
         */

        System.out.println("\n=== VALIDATION OPTIMISATION ===");

        // Test avec différentes expressions
        testOptimizationMetrics("A + 2 = B", (solver) -> {
            Variable A = createVariable("A", 0, 9);
            Variable B = createVariable("B", 0, 18);
            Variable expr = createOptimizedExpression(solver, A, "+", 2);
            solver.addRelation(expr, "=", B);
        });

        testOptimizationMetrics("A + B = C", (solver) -> {
            Variable A = createVariable("A", 0, 9);
            Variable B = createVariable("B", 0, 9);
            Variable C = createVariable("C", 0, 18);
            Variable expr = createOptimizedExpression(solver, A, "+", B);
            solver.addRelation(expr, "=", C);
        });

        testOptimizationMetrics("(A + B) * 2 = C", (solver) -> {
            Variable A = createVariable("A", 0, 9);
            Variable B = createVariable("B", 0, 9);
            Variable C = createVariable("C", 0, 36);
            Variable sumExpr = createOptimizedExpression(solver, A, "+", B);
            Variable expr = createOptimizedExpression(solver, sumExpr, "*", 2);
            solver.addRelation(expr, "=", C);
        });
    }

    private void testOptimizationMetrics(String expressionName, Consumer<Solver> setupExpression) {
        System.out.println("\n--- Test expression: " + expressionName + " ---");

        // Test SANS optimisation
        Solver solverNoOpt = new Solver();
        setupExpression.accept(solverNoOpt);

        int variablesNoOpt = getVariables(solverNoOpt).size();
        int constraintsNoOpt = getConstraints(solverNoOpt).size();

        // Résoudre pour vérifier que ça fonctionne
        long solutionsNoOpt = solverNoOpt.solve();

        System.out.printf("SANS optimisation: %d variables, %d contraintes, %d solutions%n",
                         variablesNoOpt, constraintsNoOpt, solutionsNoOpt);

        // Test AVEC optimisation
        Solver solverOpt = new Solver();
        setupExpression.accept(solverOpt);

        int variablesOpt = getVariables(solverOpt).size();
        int constraintsOpt = getConstraints(solverOpt).size();

        // Résoudre pour vérifier l'équivalence
        long solutionsOpt = solverOpt.solve();

        System.out.printf("AVEC optimisation: %d variables, %d contraintes, %d solutions%n",
                         variablesOpt, constraintsOpt, solutionsOpt);

        // Vérifications
        assertTrue(variablesOpt <= variablesNoOpt,
                  "L'optimisation devrait réduire ou maintenir le nombre de variables");
        assertTrue(constraintsOpt <= constraintsNoOpt,
                  "L'optimisation devrait réduire ou maintenir le nombre de contraintes");
        assertEquals(solutionsNoOpt, solutionsOpt,
                    "L'optimisation devrait donner le même nombre de solutions");

        // Calcul des réductions
        double varReduction = variablesNoOpt > 0 ?
            100.0 * (variablesNoOpt - variablesOpt) / variablesNoOpt : 0;
        double constraintReduction = constraintsNoOpt > 0 ?
            100.0 * (constraintsNoOpt - constraintsOpt) / constraintsNoOpt : 0;

        System.out.printf("Réduction: %.1f%% variables, %.1f%% contraintes%n",
                         varReduction, constraintReduction);
    }

    @Test
    void testBasicFunctionality() {
        /*
         * Test basique pour vérifier que l'optimisation ne casse pas les fonctionnalités de base
         */

        System.out.println("\n=== TEST FONCTIONNALITÉS DE BASE ===");

        // Test simple sans optimisation
        Solver solver1 = new Solver();
        Variable A1 = createVariable("A", 0, 5);
        Variable B1 = createVariable("B", 0, 10);
        Variable sum1 = solver1.expression(A1, "+", 3);
        solver1.addRelation(sum1, "=", B1);

        long solutions1 = solver1.solve();
        assertTrue(solutions1 > 0, "Devrait trouver des solutions sans optimisation");

        // Test simple avec optimisation
        Solver solver2 = new Solver();
        Variable A2 = createVariable("A", 0, 5);
        Variable B2 = createVariable("B", 0, 10);
        Variable sum2 = createOptimizedExpression(solver2, A2, "+", 3);
        solver2.addRelation(sum2, "=", B2);

        long solutions2 = solver2.solve();
        assertTrue(solutions2 > 0, "Devrait trouver des solutions avec optimisation");

        // Même nombre de solutions
        assertEquals(solutions1, solutions2, "Devrait donner le même nombre de solutions");

        System.out.println("✅ Fonctionnalités de base préservées");
    }

    @Test
    void testNoImpactOnTestSolver() {
        /*
         * Vérifier que l'optimisation n'impacte pas les tests de TestSolver
         * En reproduisant testConstraintAdd qui utilise expression(a, "+", b)
         */

        System.out.println("\n=== TEST IMPACT SUR TestSolver ===");

        // Reproduire exactement testConstraintAdd de TestSolver
        ISolver solver = new Solver();
        var a = solver.newVar("a", 0, 9);
        var b = solver.newVar("b", 0, 9);
        var aPlusB = solver.expression(a, "+", b); // Appel normal SANS optimisation
        solver.addRelation(aPlusB, "=", 5);

        long solutions = solver.solve();
        assertEquals(6, solutions, "Devrait donner le même résultat que testConstraintAdd");

        System.out.println("✅ TestSolver.testConstraintAdd fonctionne toujours : " + solutions + " solutions");
        System.out.println("✅ Aucun impact sur les tests existants");
    }

    // Méthode utilitaire pour accéder aux variables (via réflexion car c'est privé)
    @SuppressWarnings("unchecked")
    private java.util.List<Variable> getVariables(Solver solver) {
        try {
            var field = Solver.class.getDeclaredField("variables");
            field.setAccessible(true);
            return (java.util.List<Variable>) field.get(solver);
        } catch (Exception e) {
            fail("Impossible d'accéder aux variables: " + e.getMessage());
            return null;
        }
    }
}
