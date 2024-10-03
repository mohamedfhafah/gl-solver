
package fr.univamu.solver;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class TestSolver {

	@Test
	public void testDomain() {
		var solver = new Solver();
		solver.newVar().domain(0, 9);
		solver.newVar().domain(100, 109);
		assertEquals(100, solver.solve());
	}

	@Test
	public void testDomainEmpty() {
		var solver = new Solver();
		solver.newVar().domain(20, 10);
		assertEquals(0, solver.solve());
	}

	@Test
	public void testConstraintAdd() {
		var solver = new Solver();
		var a = solver.newVar().domain(0, 9);
		var b = solver.newVar().domain(0, 9);
		solver.addConstraint(5, "=", a, "+", b);// 5=A+B
		assertEquals(6, solver.solve());
	}

	@Test
	public void testConstraintSub() {
		var solver = new Solver();
		var a = solver.newVar().domain(0, 9);
		var b = solver.newVar().domain(0, 9);
		solver.addConstraint(5, "=", a, "-", b);// 5=A-B
		assertEquals(5, solver.solve());
	}

	@Test
	public void testConstraintMul() {
		var solver = new Solver();
		var a = solver.newVar().domain(0, 9);
		var b = solver.newVar().domain(0, 9);
		solver.addConstraint(a, "=", b, "*", 2);// A=B*2
		assertEquals(5, solver.solve());
	}

	@Test
	public void testConstraintDiv() {
		var solver = new Solver();
		var a = solver.newVar().domain(0, 99);
		var b = solver.newVar().domain(0, 9);
		solver.addConstraint(a, "=", b, "/", 2);// A=B/2
		assertEquals(10, solver.solve());
	}

	@Test
	public void testConstraintEq() {
		var solver = new Solver();
		var a = solver.newVar().domain(0, 5);
		var b = solver.newVar().domain(4, 9);
		solver.addConstraint(a, "=", b);
		assertEquals(2, solver.solve());
	}

	@Test
	public void testConstraintDiff() {
		var solver = new Solver();
		var a = solver.newVar("A").domain(0, 9);
		solver.addConstraint(a, "<>", 5);
		assertEquals(9, solver.solve());
	}

	@Test
	public void testConstraintGreater() {
		var solver = new Solver();
		var a = solver.newVar("A").domain(0, 9);
		solver.addConstraint(a, ">", 5);
		assertEquals(4, solver.solve());
	}

	@Test
	public void testConstraintGreaterEqual() {
		var solver = new Solver();
		var a = solver.newVar("A").domain(0, 9);
		solver.addConstraint(a, ">=", 5);
		assertEquals(5, solver.solve());
	}

	@Test
	public void testConstraintLess() {
		var solver = new Solver();
		var a = solver.newVar("A").domain(0, 9);
		solver.addConstraint(a, "<", 3);
		assertEquals(3, solver.solve());
	}

	@Test
	public void testConstraintLessEqual() {
		var solver = new Solver();
		var a = solver.newVar("A").domain(0, 9);
		solver.addConstraint(a, "<=", 6);
		assertEquals(7, solver.solve());
	}

	@Test
	public void testABC() {
		// résoudre AB + BA = CBC
		var solver = new Solver();
		var a = solver.newVar("A").domain(1, 9);
		var b = solver.newVar("B").domain(1, 9);
		var c = solver.newVar("C").domain(1, 9);
		solver.addConstraint(//
				a, "*", 10, "+", b, "+", // AB +
				b, "*", 10, "+", a, "=", // BA =
				c, "*", 100, "+", b, "*", 10, "+", c); // CBC
		assertEquals(1, solver.solve());
		assertEquals(6499207L, solver.getNodesCounter());
		// nouvelle stratégie
		solver.reduceAndCheckIntervalsStrategy();
		assertEquals(1, solver.solve());
		assertEquals(954264L, solver.getNodesCounter());
	}

}
