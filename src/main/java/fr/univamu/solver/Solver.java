package fr.univamu.solver;

import java.util.LinkedList;
import java.util.List;

class Constraint {
	private char type;
	private Variable result;
	private Variable var1;
	private Variable var2;

	public String toString() {
		return String.format("%s(%s,%s,%s)", type, result, var1, var2);
	}

	public Constraint(char type, Variable result, Variable var1, Variable var2) {
		this.type = type;
		this.result = result;
		this.var1 = var1;
		this.var2 = var2;
	}

	public char getType() {
		return type;
	}

	public Variable getResult() {
		return result;
	}

	public Variable getVar1() {
		return var1;
	}

	public Variable getVar2() {
		return var2;
	}
}

public class Solver {

	private static final int CHECK_INTERVALS_STRATEGY = 1;
	private static final int REDUCE_AND_CHECK_INTERVALS_STRATEGY = 2;

	private List<Constraint> constraints = new LinkedList<>();
	private List<Variable> variables = new LinkedList<>();
	private int strategy = CHECK_INTERVALS_STRATEGY;

	private long solutionsCounter = 0;
	private long nodesCounter = 0;
	private long maxNodes = 1000_000_000L;
	private boolean verbose = true;

	public void reduceAndCheckIntervalsStrategy() {
		strategy = REDUCE_AND_CHECK_INTERVALS_STRATEGY;
	}

	private boolean checkAddConstraintIntervalsStrategy(Constraint c) {
		return c.getVar1().add(c.getVar2()).inter(c.getResult()).isNotEmpty();
	}

	private boolean checkMulConstraintIntervalsStrategy(Constraint c) {
		return c.getVar1().mul(c.getVar2()).inter(c.getResult()).isNotEmpty();
	}

	private boolean checkDivConstraintIntervalsStrategy(Constraint c) {
		return c.getVar1().div(c.getVar2()).inter(c.getResult()).isNotEmpty();
	}

	private boolean checkDiffConstraintIntervalsStrategy(Constraint c) {
		var result = c.getResult();
		var ko = result.equals(c.getVar1()) && result.isFixed();
		return (!ko);
	}

	private boolean reduceAddConstraint(Constraint c) {
		var change = c.getResult().reduce(c.getVar1().add(c.getVar2()));
		change = c.getVar1().reduce(c.getResult().sub(c.getVar2())) || change;
		change = c.getVar2().reduce(c.getResult().sub(c.getVar1())) || change;
		return change;
	}

	private boolean reduceMulConstraint(Constraint c) {
		var change = false;
		for (int i = 0; i < 3; i++) {
			change = c.getResult().reduce(c.getVar1().mul(c.getVar2())) || change;
			change = c.getVar2().reduce(c.getResult().div2(c.getVar1())) || change;
			change = c.getVar1().reduce(c.getResult().div2(c.getVar2())) || change;
		}
		return change;
	}

	private boolean checkConstraintIntervalsStrategy(Constraint c) {
		switch (c.getType()) {
		case '+':
			return checkAddConstraintIntervalsStrategy(c);
		case '#':
			return checkDiffConstraintIntervalsStrategy(c);
		case '*':
			return checkMulConstraintIntervalsStrategy(c);
		case '/':
			return checkDivConstraintIntervalsStrategy(c);
		default:
			break;
		}
		throw new IllegalArgumentException("bad constraint: " + c);
	}

	private boolean reduce(Constraint c) {
		switch (c.getType()) {
		case '+':
			return reduceAddConstraint(c);
		case '*':
			return reduceMulConstraint(c);
//		case '#':
//		case '/':
		}
		return false;
	}

	private boolean checkConstraint(Constraint c) {
		switch (strategy) {
		case CHECK_INTERVALS_STRATEGY:
		case REDUCE_AND_CHECK_INTERVALS_STRATEGY:
			return checkConstraintIntervalsStrategy(c);
		}
		throw new IllegalStateException("bad strategy: " + strategy);
	}

	private boolean checkConstraints() {
		for (Constraint c : constraints) {
			if (!checkConstraint(c)) {
				return false;
			}
		}
		return true;
	}

	public Variable findVariable() {
		Variable best = null;
		for (Variable v : variables) {
			if (v.isFixed()) {
				// ne pas choisir
			} else if (best == null) {
				best = v;
			} else if (v.getSize() < best.getSize()) {
				best = v;
			}
		}
		return best;
	}

	public boolean findSolutions() {
		if (++nodesCounter > maxNodes) {
			throw new IllegalStateException("too many nodes");
		}
		if (checkConstraints() == false) {
			return false;
		}
		var v = findVariable();
		if (v == null) {
			solutionsCounter++;
			if (verbose) {
				variables.stream().filter(Variable::isNamed).forEach(System.out::println);
				System.out.println();
			}
			return true;
		}

		boolean result = false;
		int min = v.getMin();
		int max = v.getMax();

		// Comment découper le domaine ?
		int step = 1;
		if (v.getSize() > 1000) {
			if (min < 0 && max >= 0) {
				step = -min;
			} else {
				int mid = (min + max) / 2;
				step = (1 + mid - min);
			}
		}

		// explorer le domaine
		for (int value = min; value <= max; value += step) {
			v.setMin(value);
			v.setMax(Math.min(value + step - 1, max));
			if (findSolutions()) {
				result = true;
			}
		}
		v.setMin(min);
		v.setMax(max);
		return result;
	}

	public Variable newVar() {
		var v = new Variable(variables.size());
		variables.add(v);
		return v;
	}

	public Variable newVar(String name) {
		var v = new Variable(name);
		variables.add(v);
		return v;
	}

	public Variable newConstant(int value) {
		return newVar().domain(value);
	}

	public void eq(Variable a, Variable b) {
		sub(newConstant(0), a, b); // 0 = A - B
	}

	public void gt(Variable a, Variable b) {
		sub(newVar().domain(1, Variable.MAX_VALUE), a, b); // Z=A-B,Z>0
	}

	public void get(Variable a, Variable b) {
		sub(newVar().domain(0, Variable.MAX_VALUE), a, b); // Z=A-B, Z>=0
	}

	public void lt(Variable a, Variable b) {
		gt(b, a);
	}

	public void let(Variable a, Variable b) {
		get(b, a);
	}

	public void diff(Variable a, Variable b) {
		constraints.add(new Constraint('#', a, b, null));
	}

	public void allDiff(Variable... variables) {
		for (int i = 0; i < variables.length; i++)
			for (int j = i + 1; j < variables.length; j++) {
				diff(variables[i], variables[j]);
			}
	}

	private void add(Variable result, Variable a, Variable b) {
		constraints.add(new Constraint('+', result, a, b));
	}

	private void sub(Variable result, Variable a, Variable b) {
		add(a, result, b);
	}

	private void mul(Variable result, Variable a, Variable b) {
		constraints.add(new Constraint('*', result, a, b));
	}

	private void div(Variable result, Variable a, Variable b) {
		constraints.add(new Constraint('/', result, a, b));
	}

	private Variable parseSimpleTerm(List<Object> terms) {
		var first = terms.removeFirst();
		if (first instanceof Variable var) {
			return var;
		}
		if (first instanceof Integer cst) {
			return this.newConstant(cst);
		}
		throw new IllegalArgumentException("bad expression: " + first);
	}

	private boolean parseToken(String token, List<Object> terms) {
		if (!terms.isEmpty()) {
			if (token.equals(terms.getFirst())) {
				terms.removeFirst();
				return true;
			}
		}
		return false;
	}

	private Variable parseMultiplicationTerm(List<Object> terms) {
		var first = parseSimpleTerm(terms);
		if (parseToken("*", terms)) {
			var second = parseMultiplicationTerm(terms);
			var result = newVar();
			mul(result, first, second);
			return result;
		}
		if (parseToken("/", terms)) {
			var second = parseMultiplicationTerm(terms);
			var result = newVar();
			div(result, first, second);
			return result;
		}
		return first;
	}

	private Variable parseAdditionTerm(List<Object> terms) {
		var first = parseMultiplicationTerm(terms);
		if (parseToken("+", terms)) {
			var second = parseAdditionTerm(terms);
			var result = newVar();
			add(result, first, second);
			return result;
		}
		if (parseToken("-", terms)) {
			var second = parseAdditionTerm(terms);
			var result = newVar();
			add(first, result, second);
			return result;
		}
		return first;
	}

	public void addConstraint(Object... terms) {
		var termsList = new LinkedList<>();
		termsList.addAll(List.of(terms));
		var var1 = parseAdditionTerm(termsList);
		var relation = termsList.removeFirst();
		var var2 = parseAdditionTerm(termsList);
		if (!termsList.isEmpty()) {
			throw new IllegalArgumentException("bad expression: " + termsList);
		}
		if (relation instanceof String carRelation) {
			switch (carRelation) {
			case "=":
				eq(var1, var2);
				return;
			case ">":
				gt(var1, var2);
				return;
			case ">=":
				get(var1, var2);
				return;
			case "<":
				lt(var1, var2);
				return;
			case "<=":
				let(var1, var2);
				return;
			case "<>":
				diff(var1, var2);
				return;
			}
		}
		throw new IllegalArgumentException("bad relation: " + relation);
	}

	public Variable expression2(Object... terms) {
		var termsList = new LinkedList<>(List.of(terms));
		var result = parseAdditionTerm(termsList);
		if (!termsList.isEmpty()) {
			throw new IllegalArgumentException("bad expression: " + termsList);
		}
		return result;
	}

	private void reduce() {
		if (verbose) {
			System.out.println("Variables before reduction:");
			variables.forEach(System.out::println);
		}

		for (boolean change = true; change;) {
			change = false;
			for (Constraint c : constraints) {
				if (reduce(c)) {
					change = true;
				}
			}
		}

		if (verbose) {
			System.out.println("Variables after reduction:");
			variables.forEach(System.out::println);
		}
	}

	public long solve() {
		this.solutionsCounter = 0;
		this.nodesCounter = 0;
		if (strategy == REDUCE_AND_CHECK_INTERVALS_STRATEGY) {
			reduce();
		}
		findSolutions();
		return solutionsCounter;
	}

	public int getStrategy() {
		return strategy;
	}

	public long getNodesCounter() {
		return nodesCounter;
	}

	public long getMaxNodes() {
		return maxNodes;
	}

	public void setStrategy(int strategy) {
		this.strategy = strategy;
	}

	public void setMaxNodes(long maxNodes) {
		this.maxNodes = maxNodes;
	}

	public boolean isVerbose() {
		return verbose;
	}

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

}
