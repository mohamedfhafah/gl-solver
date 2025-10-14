package fr.univamu.solver.engine;

import fr.univamu.solver.domain.Constraint;
import fr.univamu.solver.domain.ConstraintType;
import fr.univamu.solver.domain.Variable;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;

/**
 * Tests unitaires pour la classe Checker.
 */
class TestChecker {

    @Test
    void testCheckAllPositiveCase() {
        // Cas positif: contraintes satisfiables
        System.out.println("\n=== TEST CHECKER - Cas positif ===");

        // Créer des variables et contraintes satisfiables
        var a = new Variable("A");
        a.init(1, 3);
        var b = new Variable("B");
        b.init(2, 4);
        var sum = new Variable("SUM");
        sum.init(3, 7);

        var variables = List.of(a, b, sum);
        var constraints = List.of(new Constraint(ConstraintType.ADD, sum, a, b));

        var checker = new Checker(constraints, variables);

        // Les contraintes devraient être satisfaites
        assertTrue(checker.checkAll(), "Les contraintes d'addition devraient être satisfaites");

        System.out.println("✅ Cas positif passé: contraintes satisfiables détectées");
    }

    @Test
    void testCheckAllNegativeCase() {
        // Cas négatif: contraintes insatisfiables
        // Créer des variables et contraintes insatisfiables
        var a = new Variable("A");
        a.init(1, 3);
        var b = new Variable("B");
        b.init(5, 7);  // b > 3, donc A + B ne peut pas être dans [1,1]
        var sum = new Variable("SUM");
        sum.init(1, 1);  // fixé à 1, mais A + B ≥ 1+5 = 6

        var variables = List.of(a, b, sum);
        var constraints = List.of(new Constraint(ConstraintType.ADD, sum, a, b));

        var checker = new Checker(constraints, variables);

        // Les contraintes devraient être insatisfaites
        assertFalse(checker.checkAll(), "Les contraintes d'addition devraient être insatisfaites");
    }

    @Test
    void testCheckCounter() {
        var a = new Variable("A");
        a.init(1, 3);
        var b = new Variable("B");
        b.init(2, 4);
        var sum = new Variable("SUM");
        sum.init(3, 7);

        var variables = List.of(a, b, sum);
        var constraints = List.of(new Constraint(ConstraintType.ADD, sum, a, b));

        var checker = new Checker(constraints, variables);

        // Compteur initial à 0
        assertEquals(0, checker.getCheckCounter());

        // Première vérification
        checker.checkAll();
        assertEquals(1, checker.getCheckCounter());

        // Seconde vérification
        checker.checkAll();
        assertEquals(2, checker.getCheckCounter());

        // Reset du compteur
        checker.resetCheckCounter();
        assertEquals(0, checker.getCheckCounter());

    }

    @Test
    void testCheckConstraintDirectly() {
        var a = new Variable("A");
        a.init(1, 3);
        var b = new Variable("B");
        b.init(2, 4);
        var sum = new Variable("SUM");
        sum.init(3, 7);

        var variables = List.of(a, b, sum);
        var constraints = List.of(new Constraint(ConstraintType.ADD, sum, a, b));
        var constraint = constraints.get(0);

        var checker = new Checker(constraints, variables);

        // Vérifier la contrainte directement
        assertTrue(checker.checkConstraint(constraint, Checker.CHECK_INTERVALS_STRATEGY),
                  "La contrainte devrait être satisfaite");

        assertEquals(1, checker.getCheckCounter());

    }

    @Test
    void testDiffConstraint() {
        // Cas où la différence est possible
        var x = new Variable("X");
        x.init(1, 5);
        var y = new Variable("Y");
        y.init(1, 5);

        var variables = List.of(x, y);
        var diffConstraint = new Constraint(ConstraintType.DIFF, x, y, null);

        var checker = new Checker(List.of(diffConstraint), variables);
        assertTrue(checker.checkAll(), "X ≠ Y devrait être satisfiable");

        // Cas où la différence est impossible (X et Y sont la même variable fixée)
        x.init(3, 3); // X = 3
        y.init(3, 3); // Y = 3, mais c'est la même variable !

        var sameVarConstraint = new Constraint(ConstraintType.DIFF, x, x, null);
        checker = new Checker(List.of(sameVarConstraint), variables);
        assertFalse(checker.checkAll(), "X ≠ X devrait être insatisfiable");

    }

    @Test
    void testMulConstraint() {
        var a = new Variable("A");
        a.init(2, 3);
        var b = new Variable("B");
        b.init(4, 5);
        var prod = new Variable("PROD");
        prod.init(8, 15);

        var checker = new Checker(List.of(new Constraint(ConstraintType.MUL, prod, a, b)), List.of(a, b, prod));
        assertTrue(checker.checkAll(), "Multiplication cohérente devrait être valide");

        prod.init(20, 30);
        assertFalse(checker.checkAll(), "Intervalle résultat incompatible devrait invalider la contrainte");
    }

    @Test
    void testDivConstraint() {
        var dividend = new Variable("DIVIDEND");
        dividend.init(10, 20);
        var divisor = new Variable("DIVISOR");
        divisor.init(2, 4);
        var quotient = new Variable("QUOTIENT");
        quotient.init(2, 10);

        var checker = new Checker(List.of(new Constraint(ConstraintType.DIV, quotient, dividend, divisor)),
                                  List.of(dividend, divisor, quotient));
        assertTrue(checker.checkAll(), "Division cohérente devrait être valide");

        quotient.init(20, 30);
        assertFalse(checker.checkAll(), "Quotient hors des bornes devrait invalider la contrainte");
    }
}
