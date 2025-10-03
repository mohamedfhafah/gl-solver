package fr.univamu.solver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.function.BiFunction;
import java.util.List;

import org.junit.jupiter.api.Test;

public class TestInterval {

	/**
	 * Méthode exhaustive qui explore toutes les combinaisons possibles
	 * de valeurs des intervalles a et b pour déterminer l'intervalle résultat exact.
	 */
	private Interval exploreOperation(BiFunction<Integer, Integer, Integer> operation, Interval a, Interval b) {
		if (a.isEmpty() || b.isEmpty()) {
			return Interval.empty();
		}

		int minResult = Integer.MAX_VALUE;
		int maxResult = Integer.MIN_VALUE;

		// Explorer toutes les combinaisons possibles
		for (int valA = a.getMin(); valA <= a.getMax(); valA++) {
			for (int valB = b.getMin(); valB <= b.getMax(); valB++) {
				try {
					int result = operation.apply(valA, valB);
					minResult = Math.min(minResult, result);
					maxResult = Math.max(maxResult, result);
				} catch (ArithmeticException e) {
					// Masquer les exceptions (comme division par zéro)
					// et continuer l'exploration
				}
			}
		}

		if (minResult == Integer.MAX_VALUE) {
			// Aucune opération valide trouvée
			return Interval.empty();
		}

		return new Interval(minResult, maxResult);
	}

	@Test
	void testExploreOperationAddition() {
		// Test avec l'addition de [3,10] et [-5,0]
		var a = new Interval(3, 10);
		var b = new Interval(-5, 0);

		// Calcul exhaustif : min=3+(-5)=-2, max=10+0=10
		var expected = exploreOperation(Integer::sum, a, b);
		assertEquals("[-2,10]", expected.toString());

		// Vérification que cela correspond à la méthode add() existante
		var actual = a.add(b);
		assertEquals(expected.toString(), actual.toString());
	}

	@Test
	void testExploreOperationDivision() {
		// Test avec la division sur [-10,10] et [-2,2]
		var a = new Interval(-10, 10);
		var b = new Interval(-2, 2);

		// Calcul exhaustif en gérant la division par zéro
		var expected = exploreOperation((x, y) -> {
			if (y == 0) throw new ArithmeticException("Division by zero");
			return x / y;
		}, a, b);

		// Cette méthode donne le résultat exact par exploration complète
		System.out.println("Résultat exact pour [-10,10] ÷ [-2,2] : " + expected);

		// Comparaison avec la méthode div() existante (approximative)
		var actual = a.div(b);
		System.out.println("Résultat approximatif : " + actual);
	}

	@Test
	void testExploreOperationSubtraction() {
		// Test soustraction : [5,8] - [2,6]
		var a = new Interval(5, 8);
		var b = new Interval(2, 6);

		// Calcul exhaustif : min=5-6=-1, max=8-2=6
		var expected = exploreOperation((x, y) -> x - y, a, b);
		assertEquals("[-1,6]", expected.toString());

		// Vérification cohérence avec sub()
		var actual = a.sub(b);
		assertEquals(expected.toString(), actual.toString());
	}

	@Test
	void testExploreOperationMultiplication() {
		// Test multiplication : [2,4] × [-1,3]
		var a = new Interval(2, 4);
		var b = new Interval(-1, 3);

		// Calcul exhaustif : 2×(-1)=-2, 2×3=6, 4×(-1)=-4, 4×3=12
		// min=-4, max=12
		var expected = exploreOperation((x, y) -> x * y, a, b);
		assertEquals("[-4,12]", expected.toString());

		// Vérification cohérence avec mul()
		var actual = a.mul(b);
		assertEquals(expected.toString(), actual.toString());
	}

	@Test
	void testExploreOperationEmptyIntervals() {
		// Test avec intervalles vides
		var empty = Interval.empty();
		var normal = new Interval(1, 5);

		// Toute opération avec intervalle vide donne intervalle vide
		var result1 = exploreOperation(Integer::sum, empty, normal);
		assertTrue(result1.isEmpty());

		var result2 = exploreOperation(Integer::sum, normal, empty);
		assertTrue(result2.isEmpty());

		var result3 = exploreOperation((x, y) -> x - y, empty, empty);
		assertTrue(result3.isEmpty());
	}

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

	@Test
	void testBuildAllNotEmptyIntervals() {
		// Test de la méthode buildAllNotEmptyIntervals sur [0,9]
		// Cette méthode est privée, nous testons donc indirectement
		var testInterval = new Interval(0, 0); // Instance pour accéder à la méthode privée via réflexion

		try {
			var method = Interval.class.getDeclaredMethod("buildAllNotEmptyIntervals", int.class, int.class);
			method.setAccessible(true);

			@SuppressWarnings("unchecked")
			List<Interval> result = (List<Interval>) method.invoke(testInterval, 0, 9);

			// Vérifier qu'on obtient 55 intervalles
			assertEquals(55, result.size());

			// Vérifier quelques intervalles spécifiques
			assertTrue(result.contains(new Interval(0, 0)));
			assertTrue(result.contains(new Interval(0, 9)));
			assertTrue(result.contains(new Interval(5, 5)));
			assertTrue(result.contains(new Interval(5, 9)));
			assertTrue(result.contains(new Interval(9, 9)));

			// Vérifier que tous les intervalles sont non vides
			for (Interval interval : result) {
				assertFalse(interval.isEmpty());
			}

			System.out.println("✅ testBuildAllNotEmptyIntervals réussi: 55 intervalles générés pour [0,9]");

		} catch (Exception e) {
			fail("Erreur lors du test de buildAllNotEmptyIntervals: " + e.getMessage());
		}
	}

}
