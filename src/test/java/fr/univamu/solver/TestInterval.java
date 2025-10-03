package fr.univamu.solver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class TestInterval {
	
	@Test
	void testEmpty() {
		var i = new Interval(20, 10);
		assertTrue(i.isEmpty());
	}

	@Test
	void testFixed() {
		var i = new Interval(20, 20);
		assertTrue(i.isOneValue());
	}

	@Test
	void testAdd() {
		var a = new Interval(20, 30);
		var b = new Interval(-33, -5);
		assertEquals("[-13,25]", a.add(b).toString());
	}

	@Test
	void testSub() {
		// Test soustraction : [a,b] - [c,d] = [a-d, b-c]
		var a = new Interval(20, 30);
		var b = new Interval(-5, 15);
		// [20,30] - [-5,15] = [20-15, 30-(-5)] = [5, 35]
		assertEquals("[5,35]", a.sub(b).toString());

		// Test avec intervalles négatifs
		var c = new Interval(-10, -5);
		var d = new Interval(2, 8);
		// [-10,-5] - [2,8] = [-10-8, -5-2] = [-18, -7]
		assertEquals("[-18,-7]", c.sub(d).toString());
	}

	@Test
	void testMul() {
		// Test multiplication : [a,b] × [c,d] = [min(ac,ad,bc,bd), max(ac,ad,bc,bd)]
		var a = new Interval(2, 4);
		var b = new Interval(3, 5);
		// [2,4] × [3,5] = [2×3=6, 2×5=10, 4×3=12, 4×5=20] → [6,20]
		assertEquals("[6,20]", a.mul(b).toString());

		// Test avec nombres négatifs
		var c = new Interval(-3, 2);
		var d = new Interval(-4, 5);
		// Calcul : min(-3×-4=12, -3×5=-15, 2×-4=-8, 2×5=10) = -15
		//         max(-3×-4=12, -3×5=-15, 2×-4=-8, 2×5=10) = 12
		assertEquals("[-15,12]", c.mul(d).toString());
	}

	@Test
	void testDiv() {
		// Test division par intervalle contenant zéro (comportement spécial)
		var a = new Interval(10, 20);
		var b = new Interval(-2, 3);
		// L'algorithme teste les diviseurs {-2, -1, 1, 3}
		// Résultats : min(-20,-10,10,3)=-20, max(-5,-10,20,6)=20
		assertEquals("[-20,20]", a.div(b).toString());

		// Test division par intervalle positif
		var c = new Interval(8, 12);
		var d = new Interval(2, 4);
		// L'algorithme teste les diviseurs {2, -1, 1, 4}
		// 8/2=4, 8/4=2, 12/2=6, 12/4=3 → [2,6]
		assertEquals("[2,6]", c.div(d).toString());

		// Test division par intervalle négatif
		var e = new Interval(-12, -8);
		var f = new Interval(-4, -2);
		// L'algorithme teste les diviseurs {-4, -1, 1, -2}
		// -12/-4=3, -12/-2=6, -8/-4=2, -8/-2=4 → [2,6]
		assertEquals("[2,6]", e.div(f).toString());
	}

	@Test
	void testEmptyOperations() {
		// Test opérations avec intervalles vides
		var empty = new Interval(5, 3); // intervalle vide
		var normal = new Interval(1, 5);

		assertTrue(empty.isEmpty());

		// Toute opération avec un intervalle vide donne un intervalle vide
		assertEquals("[]", empty.add(normal).toString());
		assertEquals("[]", normal.add(empty).toString());
		assertEquals("[]", empty.sub(normal).toString());
		assertEquals("[]", empty.mul(normal).toString());
		assertEquals("[]", empty.div(normal).toString());
	}

	@Test
	void testReduce() {
		// Test de la méthode reduce
		var interval = new Interval(-100, 100);

		// Réduction vers l'intérieur
		assertTrue(interval.reduce(10, 50));
		assertEquals("[10,50]", interval.toString());

		// Réduction supplémentaire
		assertTrue(interval.reduce(20, 40));
		assertEquals("[20,40]", interval.toString());

		// Réduction qui ne change rien
		assertFalse(interval.reduce(15, 45)); // devrait retourner false
		assertEquals("[20,40]", interval.toString()); // inchangé
	}

	@Test
	void testIntervalProperties() {
		var empty = new Interval(10, 5);
		var fixed = new Interval(7, 7);
		var normal = new Interval(2, 8);

		// Test isEmpty
		assertTrue(empty.isEmpty());
		assertFalse(fixed.isEmpty());
		assertFalse(normal.isEmpty());

		// Test isOneValue
		assertFalse(empty.isOneValue());
		assertTrue(fixed.isOneValue());
		assertFalse(normal.isOneValue());

		// Test contains
		assertTrue(normal.contains(5));
		assertTrue(normal.contains(2));
		assertTrue(normal.contains(8));
		assertFalse(normal.contains(1));
		assertFalse(normal.contains(9));

		// Test size
		assertEquals(0, empty.getSize());
		assertEquals(1, fixed.getSize());
		assertEquals(7, normal.getSize()); // [2,8] = 7 valeurs
	}

}
