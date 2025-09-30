
package fr.univamu.solver;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class TestSolver {

    @Test
    public void testDomain() {
        ISolver solver = new Solver();
        solver.newVar("X", 0, 9);
        solver.newVar("Y", 100, 109);
        assertEquals(100, solver.solve());
    }

    @Test
    public void testDomainEmpty() {
        ISolver solver = new Solver();
        solver.newVar("X", 20, 10);
        assertEquals(0, solver.solve());
    }

    @Test
    public void testConstraintAdd() {
        ISolver solver = new Solver();
        var a = solver.newVar("a", 0, 9);
        var b = solver.newVar("b", 0, 9);
        var aPlusB = solver.expression(a, "+", b);
        solver.addRelation(aPlusB, "=", 5);// A+B=5
        assertEquals(6, solver.solve());
    }

    @Test
    public void testConstraintSub() {
        ISolver solver = new Solver();
        var a = solver.newVar("a", 0, 9);
        var b = solver.newVar("b", 0, 9);
        solver.addRelation(solver.expression(a, "-", b), "=", 5);// 5=A-B
        assertEquals(5, solver.solve());
    }

    @Test
    public void testConstraintMul() {
        ISolver solver = new Solver();
        var a = solver.newVar("a", 0, 9);
        var b = solver.newVar("b", 0, 9);
        solver.addRelation(a, "=", solver.expression(b, "*", 2));// A=B*2
        assertEquals(5, solver.solve());
    }

    @Test
    public void testConstraintMul2025() {
        ISolver solver = new Solver();
        var a = solver.newVar("a", 0, 2025);
        solver.addRelation(solver.expression(a, "*", a), "=", 2025);// 2025=A*A
        assertEquals(1, solver.solve());
    }

    @Test
    public void testConstraintDiv() {
        ISolver solver = new Solver();
        var a = solver.newVar("a", 0, 99);
        var b = solver.newVar("b", 0, 9);
        solver.addRelation(a, "=", solver.expression(b, "/", 2));// A=B/2
        assertEquals(10, solver.solve());
    }

    @Test
    public void testConstraintEq() {
        ISolver solver = new Solver();
        var a = solver.newVar("a", 0, 5);
        var b = solver.newVar("b", 4, 9);
        solver.addRelation(a, "=", b);
        assertEquals(2, solver.solve());
    }

    @Test
    public void testConstraintDiff() {
        ISolver solver = new Solver();
        var a = solver.newVar("A", 0, 9);
        solver.addRelation(a, "<>", 5);
        assertEquals(9, solver.solve());
    }

    @Test
    public void testConstraintGreater() {
        ISolver solver = new Solver();
        var a = solver.newVar("A", 0, 9);
        solver.addRelation(a, ">", 5);
        assertEquals(4, solver.solve());
    }

    @Test
    public void testConstraintGreaterEqual() {
        ISolver solver = new Solver();
        var a = solver.newVar("A", 0, 9);
        solver.addRelation(a, ">=", 5);
        assertEquals(5, solver.solve());
    }

    @Test
    public void testConstraintLess() {
        ISolver solver = new Solver();
        var a = solver.newVar("A", 0, 9);
        solver.addRelation(a, "<", 3);
        assertEquals(3, solver.solve());
    }

    @Test
    public void testConstraintLessEqual() {
        ISolver solver = new Solver();
        var a = solver.newVar("A", 0, 9);
        solver.addRelation(a, "<=", 6);
        assertEquals(7, solver.solve());
    }

    @Test
    public void testABC() {
        // résoudre AB + BA = CBC
        ISolver solver = new Solver();
        var a = solver.newVar("A", 1, 9);
        var b = solver.newVar("B", 1, 9);
        var c = solver.newVar("C", 1, 9);
        var ab = solver.expression(a, "*", 10, "+", b);
        var ba = solver.expression(b, "*", 10, "+", a);
        var cbc = solver.expression(c, "*", 100, "+", b, "*", 10, "+", c);
        var abPlusBa = solver.expression(ab, "+", ba);
        solver.addRelation(abPlusBa, "=", cbc);
        assertEquals(1, solver.solve());
        assertEquals(6499207L, solver.getNodesCounter());
        // nouvelle stratégie
        solver.reduceAndCheckIntervalsStrategy();
        assertEquals(1, solver.solve());
        assertEquals(49964L, solver.getNodesCounter());
    }

}
