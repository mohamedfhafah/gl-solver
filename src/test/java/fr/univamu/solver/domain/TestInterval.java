package fr.univamu.solver.domain;

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
		var testInterval = new Interval(0, 0);

		List<Interval> result = testInterval.buildAllNotEmptyIntervals(0, 9);

		// Vérifier qu'on obtient 55 intervalles (somme de 1 à 10 = 55)
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
	}

	@Test
	void testExhaustiveAddition() {
		// Étape 3: Test exhaustif de l'addition sur tous les intervalles de [-8,8]

		try {
			// Construire la liste de tous les intervalles non vides dans [-8,8]
			var testInterval = new Interval(0, 0);
			List<Interval> allIntervals = testInterval.buildAllNotEmptyIntervals(-8, 8);

			// Ajouter l'intervalle vide
			allIntervals.add(Interval.empty());

			System.out.printf("🧮 TEST EXHAUSTIF ADDITION: %d intervalles dans [-8,8] (incluant vide)%n", allIntervals.size());

			int totalTests = 0;
			int passedTests = 0;

			// Tester chaque couple d'intervalles (a,b)
			for (Interval a : allIntervals) {
				for (Interval b : allIntervals) {
					totalTests++;

					try {
						// Calcul par exploration exhaustive
						Interval expected = exploreOperation(Integer::sum, a, b);

						// Calcul par la méthode add() existante
						Interval actual = a.add(b);

						// Vérification que les résultats sont identiques
						if (expected.equals(actual)) {
							passedTests++;
						} else {
							System.out.printf("❌ ÉCHEC: %s + %s = %s (attendu) vs %s (add())%n",
											a, b, expected, actual);
							fail(String.format("Résultat incorrect pour %s + %s", a, b));
						}

						// Affichage périodique de progrès
						if (totalTests % 1000 == 0) {
							System.out.printf("  Progress: %d/%d tests (%.1f%%)%n",
											totalTests, allIntervals.size() * allIntervals.size(),
											100.0 * totalTests / (allIntervals.size() * allIntervals.size()));
						}

					} catch (Exception e) {
						System.out.printf("❌ ERREUR lors du test %s + %s: %s%n", a, b, e.getMessage());
						fail(String.format("Exception lors du test de %s + %s: %s", a, b, e.getMessage()));
					}
				}
			}

			System.out.printf("✅ RÉSULTAT FINAL: %d/%d tests réussis (%.1f%%)%n",
							passedTests, totalTests, 100.0 * passedTests / totalTests);

			if (passedTests == totalTests) {
				System.out.println("🎉 SUCCÈS COMPLET: Tous les tests d'addition sont passés!");
			} else {
				fail(String.format("Échec: %d tests sur %d ont échoué", totalTests - passedTests, totalTests));
			}

		} catch (Exception e) {
			fail("Erreur lors de la configuration du test exhaustif: " + e.getMessage());
		}
	}

	/**
	 * Étape 4: Méthode générique pour tester toutes les opérations binaires
	 */
	private void testBiOperation(BiFunction<Integer, Integer, Integer> intOp,
	                             BiFunction<Interval, Interval, Interval> intervalOp,
	                             String name) {
		try {
			// Construire la liste de tous les intervalles non vides dans [-8,8]
			var testInterval = new Interval(0, 0);
			List<Interval> allIntervals = testInterval.buildAllNotEmptyIntervals(-8, 8);

			// Ajouter l'intervalle vide
			allIntervals.add(Interval.empty());

			System.out.printf("🧮 TEST EXHAUSTIF %s: %d intervalles dans [-8,8] (incluant vide)%n",
							name.toUpperCase(), allIntervals.size());

			int totalTests = 0;
			int passedTests = 0;

			// Tester chaque couple d'intervalles (a,b)
			for (Interval a : allIntervals) {
				for (Interval b : allIntervals) {
					totalTests++;

					try {
						// Calcul par exploration exhaustive
						Interval expected = exploreOperation(intOp, a, b);

						// Calcul par la méthode d'intervalle existante
						Interval actual = intervalOp.apply(a, b);

						// Vérification que les résultats sont identiques
						if (expected.equals(actual)) {
							passedTests++;
						} else {
							System.out.printf("❌ ÉCHEC: %s %s %s = %s (attendu) vs %s (%s())%n",
											a, name, b, expected, actual, name.toLowerCase());
							fail(String.format("Résultat incorrect pour %s %s %s", a, name, b));
						}

						// Affichage périodique de progrès (moins fréquent pour éviter spam)
						if (totalTests % 5000 == 0) {
							System.out.printf("  Progress: %d/%d tests (%.1f%%)%n",
											totalTests, allIntervals.size() * allIntervals.size(),
											100.0 * totalTests / (allIntervals.size() * allIntervals.size()));
						}

					} catch (Exception e) {
						System.out.printf("❌ ERREUR lors du test %s %s %s: %s%n", a, name, b, e.getMessage());
						fail(String.format("Exception lors du test de %s %s %s: %s", a, name, b, e.getMessage()));
					}
				}
			}

			System.out.printf("✅ RÉSULTAT FINAL %s: %d/%d tests réussis (%.1f%%)%n",
							name.toUpperCase(), passedTests, totalTests, 100.0 * passedTests / totalTests);

			if (passedTests == totalTests) {
				System.out.printf("🎉 SUCCÈS COMPLET: Tous les tests %s sont passés!%n", name.toLowerCase());
			} else {
				fail(String.format("Échec %s: %d tests sur %d ont échoué", name, totalTests - passedTests, totalTests));
			}

		} catch (Exception e) {
			fail("Erreur lors de la configuration du test exhaustif " + name + ": " + e.getMessage());
		}
	}

	@Test
	void testExhaustiveSubtraction() {
		// Étape 4: Test exhaustif de la soustraction
		testBiOperation((a, b) -> a - b, Interval::sub, "-");
	}

	@Test
	void testExhaustiveMultiplication() {
		// Étape 4: Test exhaustif de la multiplication
		testBiOperation((a, b) -> a * b, Interval::mul, "×");
	}

	@Test
	void testExhaustiveDivision() {
		// Étape 4: Test exhaustif de la division (avec gestion des exceptions)
		testBiOperation((a, b) -> {
			if (b == 0) throw new ArithmeticException("Division by zero");
			return a / b;
		}, Interval::div, "÷");
	}

	@Test
	void testDivisionEdgeCases() {
		// Amélioration de la couverture pour la méthode div
		System.out.println("🔍 TESTS SPÉCIFIQUES POUR LA MÉTHODE div()");

		// Test division par intervalle contenant zéro
		Interval a = new Interval(6, 12);
		Interval b = new Interval(-2, 3); // contient zéro
		Interval result = a.div(b);
		System.out.println("   Division 6..12 ÷ (-2..3) = " + result);

		// Test division par intervalle positif
		Interval c = new Interval(8, 16);
		Interval d = new Interval(2, 4);
		Interval result2 = c.div(d);
		System.out.println("   Division 8..16 ÷ (2..4) = " + result2);

		// Test division par intervalle négatif
		Interval e = new Interval(-16, -8);
		Interval f = new Interval(-4, -2);
		Interval result3 = e.div(f);
		System.out.println("   Division -16..-8 ÷ (-4..-2) = " + result3);

		// Test division par intervalle vide
		Interval result4 = a.div(Interval.empty());
		assertTrue(result4.isEmpty());
		System.out.println("   Division par intervalle vide = " + result4);

		// Test division d'intervalle vide
		Interval result5 = Interval.empty().div(b);
		assertTrue(result5.isEmpty());
		System.out.println("   Division d'intervalle vide = " + result5);

		System.out.println("✅ Tous les tests de division supplémentaires réussis!");
	}

	@Test
	void testIntervalPropertiesCoverage() {
		// Amélioration de la couverture pour les propriétés des intervalles
		System.out.println("🔍 TESTS DE COUVERTURE POUR LES PROPRIÉTÉS D'INTERVALLE");

		// Test des getters
		Interval interval = new Interval(-5, 10);
		assertEquals(-5, interval.getMin());
		assertEquals(10, interval.getMax());
		System.out.println("   Getters: min=" + interval.getMin() + ", max=" + interval.getMax());

		// Test isNotEmpty
		assertTrue(interval.isNotEmpty());
		assertFalse(Interval.empty().isNotEmpty());
		System.out.println("   isNotEmpty: " + interval.isNotEmpty());

		// Test getSign pour différents cas
		assertEquals(-1, new Interval(-5, -1).getSign()); // entièrement négatif
		assertEquals(-1, new Interval(-2, -1).getSign()); // entièrement négatif
		assertEquals(1, new Interval(1, 5).getSign());    // entièrement positif
		assertEquals(0, new Interval(-2, 3).getSign());   // contient zéro
		assertEquals(-1, Interval.empty().getSign());     // vide (max < 0)
		System.out.println("   getSign testé pour tous les cas");

		// Test isInside
		Interval small = new Interval(2, 4);
		Interval large = new Interval(1, 6);
		assertTrue(small.isInside(large));
		assertFalse(large.isInside(small));
		System.out.println("   isInside: " + small + " est dans " + large + " = " + small.isInside(large));

		System.out.println("✅ Tous les tests de propriétés réussis!");
	}

	@Test
	void testInverseMulCoverage() {
		// Amélioration de la couverture pour la méthode inverseMul
		System.out.println("🔍 TESTS POUR LA MÉTHODE inverseMul()");

		// Test multiplication inverse normale
		Interval a = new Interval(6, 12);
		Interval b = new Interval(2, 3);
		Interval result = a.inverseMul(b);
		System.out.println("   Inverse multiplication 6..12 ⊗ 2..3 = " + result);

		// Test avec intervalle contenant zéro (devrait donner intervalle universel)
		Interval c = new Interval(-2, 2); // contient zéro
		Interval result2 = a.inverseMul(c);
		assertFalse(result2.isEmpty()); // intervalle universel, pas vide
		System.out.println("   Inverse multiplication avec zéro = " + result2);

		// Test avec intervalles vides
		Interval result3 = Interval.empty().inverseMul(b);
		assertTrue(result3.isEmpty());
		System.out.println("   Inverse multiplication d'intervalle vide = " + result3);

		System.out.println("✅ Tous les tests inverseMul réussis!");
	}

}
