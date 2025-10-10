package fr.univamu.solver;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import java.util.ArrayList;

/**
 * Tests unitaires pour la classe Reducer.
 */
class TestReducer {

    @Test
    void testReduceAddConstraint() {
        // Test de réduction d'une contrainte d'addition A + B = C
        System.out.println("\n=== TEST REDUCER - Addition ===");

        var a = new Variable("A");
        a.init(1, 5);
        var b = new Variable("B");
        b.init(2, 4);
        var c = new Variable("C");
        c.init(0, 10);

        var variables = List.of(a, b, c);
        var constraint = new Constraint(ConstraintType.ADD, c, a, b);
        var constraints = List.of(constraint);

        var reducer = new Reducer(constraints, variables);

        // Avant réduction: A=[1,5], B=[2,4], C=[0,10]
        assertEquals(1, a.getMin());
        assertEquals(5, a.getMax());
        assertEquals(2, b.getMin());
        assertEquals(4, b.getMax());
        assertEquals(0, c.getMin());
        assertEquals(10, c.getMax());

        reducer.reduce();

        // Après réduction: C devrait être réduit à [3,9] (1+2 à 5+4)
        // A et B ne peuvent pas être réduits plus
        assertEquals(3, c.getMin());
        assertEquals(9, c.getMax());

        System.out.println("✅ Réduction d'addition fonctionne");
    }

    @Test
    void testReduceMulConstraint() {
        // Test de réduction d'une contrainte de multiplication A * B = C
        System.out.println("\n=== TEST REDUCER - Multiplication ===");

        var a = new Variable("A");
        a.init(2, 3);
        var b = new Variable("B");
        b.init(1, 4);
        var c = new Variable("C");
        c.init(0, 12);

        var variables = List.of(a, b, c);
        var constraint = new Constraint(ConstraintType.MUL, c, a, b);
        var constraints = List.of(constraint);

        var reducer = new Reducer(constraints, variables);

        reducer.reduce();

        // Après réduction: C devrait être réduit à [2,12] (2*1 à 3*4)
        assertEquals(2, c.getMin());
        assertEquals(12, c.getMax());

        System.out.println("✅ Réduction de multiplication fonctionne");
    }

    @Test
    void testReduceContradiction() {
        // Test de réduction contradictoire: A + B = 1000 ∧ A + B = 2000
        System.out.println("\n=== TEST REDUCER - Contradiction ===");

        var a = new Variable("A");
        a.init(1, 100);
        var b = new Variable("B");
        b.init(1, 100);
        var sum1 = new Variable("SUM1");
        sum1.init(1000, 1000); // fixé à 1000
        var sum2 = new Variable("SUM2");
        sum2.init(2000, 2000); // fixé à 2000

        var variables = List.of(a, b, sum1, sum2);
        var constraints = List.of(
            new Constraint(ConstraintType.ADD, sum1, a, b),
            new Constraint(ConstraintType.ADD, sum2, a, b)
        );

        var reducer = new Reducer(constraints, variables);

        reducer.reduce();

        // Après réduction: les domaines devraient être vides (contradiction)
        assertTrue(a.isEmpty() || b.isEmpty() || sum1.isEmpty() || sum2.isEmpty(),
                  "Au moins un domaine devrait être vide suite à la contradiction");

        System.out.println("✅ Détection de contradiction fonctionne");
    }

    @Test
    void testReduceDiffConstraintCase1a() {
        // Test cas 1a: X=[10,20], Y=[20,20] → X=[10,19], Y=[20,20]
        System.out.println("\n=== TEST REDUCER - DIFF Cas 1a ===");

        var x = new Variable("X");
        x.init(10, 20);
        var y = new Variable("Y");
        y.init(20, 20); // Y fixé à 20

        var variables = List.of(x, y);
        var constraint = new Constraint(ConstraintType.DIFF, x, y, null);
        var constraints = List.of(constraint);

        var reducer = new Reducer(constraints, variables);

        reducer.reduce();

        // X devrait être réduit pour exclure 20
        assertEquals(10, x.getMin());
        assertEquals(19, x.getMax());
        assertEquals(20, y.getMin());
        assertEquals(20, y.getMax());

        System.out.println("✅ Cas DIFF 1a fonctionne");
    }

    @Test
    void testReduceDiffConstraintCase1b() {
        // Test cas 1b: X=[10,10], Y=[10,11] → X=[10,10], Y=[11,11]
        System.out.println("\n=== TEST REDUCER - DIFF Cas 1b ===");

        var x = new Variable("X");
        x.init(10, 10); // X fixé à 10
        var y = new Variable("Y");
        y.init(10, 11);

        var variables = List.of(x, y);
        var constraint = new Constraint(ConstraintType.DIFF, x, y, null);
        var constraints = List.of(constraint);

        var reducer = new Reducer(constraints, variables);

        reducer.reduce();

        // Y devrait être réduit pour exclure 10
        assertEquals(10, x.getMin());
        assertEquals(10, x.getMax());
        assertEquals(11, y.getMin());
        assertEquals(11, y.getMax());

        System.out.println("✅ Cas DIFF 1b fonctionne");
    }

    @Test
    void testReduceDiffConstraintCase3() {
        // Test cas 3: X=[15,15], Y=[15,15] → domaines vides
        System.out.println("\n=== TEST REDUCER - DIFF Cas 3 ===");

        var x = new Variable("X");
        x.init(15, 15); // X fixé à 15
        var y = new Variable("Y");
        y.init(15, 15); // Y fixé à 15

        var variables = List.of(x, y);
        var constraint = new Constraint(ConstraintType.DIFF, x, y, null);
        var constraints = List.of(constraint);

        var reducer = new Reducer(constraints, variables);

        reducer.reduce();

        // Les deux domaines devraient être vides (contradiction)
        assertTrue(x.isEmpty(), "X devrait avoir un domaine vide");
        assertTrue(y.isEmpty(), "Y devrait avoir un domaine vide");

        System.out.println("✅ Cas DIFF 3 fonctionne");
    }

    @Test
    void testReduceDiffConstraintCase2a() {
        // Test cas 2a: Y fixé, Y.value == X.min → X.min++
        System.out.println("\n=== TEST REDUCER - DIFF Cas 2a ===");

        var x = new Variable("X");
        x.init(5, 10);
        var y = new Variable("Y");
        y.init(5, 5); // Y fixé à 5

        var variables = List.of(x, y);
        var constraint = new Constraint(ConstraintType.DIFF, x, y, null);
        var constraints = List.of(constraint);

        var reducer = new Reducer(constraints, variables);

        reducer.reduce();

        // X devrait être réduit pour exclure 5
        assertEquals(6, x.getMin());
        assertEquals(10, x.getMax());
        assertEquals(5, y.getMin());
        assertEquals(5, y.getMax());

        System.out.println("✅ Cas DIFF 2a fonctionne");
    }

    @Test
    void testReduceDiffConstraintCase2b() {
        // Test cas 2b: Y fixé, Y.value == X.max → X.max--
        System.out.println("\n=== TEST REDUCER - DIFF Cas 2b ===");

        var x = new Variable("X");
        x.init(1, 8);
        var y = new Variable("Y");
        y.init(8, 8); // Y fixé à 8

        var variables = List.of(x, y);
        var constraint = new Constraint(ConstraintType.DIFF, x, y, null);
        var constraints = List.of(constraint);

        var reducer = new Reducer(constraints, variables);

        reducer.reduce();

        // X devrait être réduit pour exclure 8
        assertEquals(1, x.getMin());
        assertEquals(7, x.getMax());
        assertEquals(8, y.getMin());
        assertEquals(8, y.getMax());

        System.out.println("✅ Cas DIFF 2b fonctionne");
    }

    @Test
    void testNoReductionWhenNoConstraints() {
        // Test qu'aucune réduction n'est faite sans contraintes
        System.out.println("\n=== TEST REDUCER - Aucune contrainte ===");

        var a = new Variable("A");
        a.init(1, 10);
        var b = new Variable("B");
        b.init(5, 15);

        var variables = List.of(a, b);
        var constraints = new ArrayList<Constraint>(); // Liste vide

        var reducer = new Reducer(constraints, variables);

        // États avant/après devraient être identiques
        int aMinBefore = a.getMin();
        int aMaxBefore = a.getMax();
        int bMinBefore = b.getMin();
        int bMaxBefore = b.getMax();

        reducer.reduce();

        assertEquals(aMinBefore, a.getMin());
        assertEquals(aMaxBefore, a.getMax());
        assertEquals(bMinBefore, b.getMin());
        assertEquals(bMaxBefore, b.getMax());

        System.out.println("✅ Pas de réduction sans contraintes fonctionne");
    }
}
