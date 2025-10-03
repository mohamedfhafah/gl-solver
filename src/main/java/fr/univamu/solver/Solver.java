package fr.univamu.solver;

import java.util.LinkedList;
import java.util.List;

public class Solver implements ISolver {

    private static final int CHECK_INTERVALS_STRATEGY = 1;
    private static final int REDUCE_AND_CHECK_INTERVALS_STRATEGY = 2;

    private final List<Constraint> constraints = new LinkedList<>();
    private final List<Variable> variables = new LinkedList<>();
    private int strategy = CHECK_INTERVALS_STRATEGY;

    private long solutionsCounter = 0;
    private long nodesCounter = 0;
    private long maxNodes = 1000_000_000L;
    private boolean verbose = true;
    private boolean modified = false;

    public void reduceAndCheckIntervalsStrategy() {
        strategy = REDUCE_AND_CHECK_INTERVALS_STRATEGY;
    }

    private boolean checkAddConstraintIntervalsStrategy(Constraint c) {
        return c.var1().add(c.var2()).inter(c.result()).isNotEmpty();
    }

    private boolean checkMulConstraintIntervalsStrategy(Constraint c) {
        return c.var1().mul(c.var2()).inter(c.result()).isNotEmpty();
    }

    private boolean checkDivConstraintIntervalsStrategy(Constraint c) {
        return c.var1().div(c.var2()).inter(c.result()).isNotEmpty();
    }

    private boolean checkDiffConstraintIntervalsStrategy(Constraint c) {
        var result = c.result();
        var ko = result.equals(c.var1()) && result.isOneValue();
        return (!ko);
    }

    private boolean checkConstraintIntervalsStrategy(Constraint c) {
        return switch (c.type()) {
            case '+' -> checkAddConstraintIntervalsStrategy(c);
            case '#' -> checkDiffConstraintIntervalsStrategy(c);
            case '*' -> checkMulConstraintIntervalsStrategy(c);
            case '/' -> checkDivConstraintIntervalsStrategy(c);
            default -> throw new IllegalArgumentException("bad constraint: " + c);
        };
    }

    private void reduceAddConstraint(Constraint c) {
        modified = c.result().reduce(c.var1().add(c.var2())) || modified;
        modified = c.var1().reduce(c.result().sub(c.var2())) || modified;
        modified = c.var2().reduce(c.result().sub(c.var1())) || modified;
    }

    private void reduceMulConstraint(Constraint c) {
        for (int i = 0; i < 3; i++) {
            modified = c.result().reduce(c.var1().mul(c.var2())) || modified;
            modified = c.var2().reduce(c.result().inverseMul(c.var1())) || modified;
            modified = c.var1().reduce(c.result().inverseMul(c.var2())) || modified;
        }
    }

    private void reduce(Constraint c) {
        switch (c.type()) {
            case '+':
                reduceAddConstraint(c);
                break;
            case '*':
                reduceMulConstraint(c);
                break;
        }
    }

    private void reduce() {
        if (verbose) {
            System.out.println("Variables before reduction:");
            variables.forEach(System.out::println);
        }

        do {
            modified = false;
            constraints.forEach(this::reduce);
        } while (modified);

        if (verbose) {
            System.out.println("Variables after reduction:");
            variables.forEach(System.out::println);
        }
    }

    private boolean checkConstraint(Constraint c) {
        return switch (strategy) {
            case CHECK_INTERVALS_STRATEGY, REDUCE_AND_CHECK_INTERVALS_STRATEGY -> checkConstraintIntervalsStrategy(c);
            default -> throw new IllegalStateException("bad strategy: " + strategy);
        };
    }

    private boolean checkConstraints() {
        for (Constraint c : constraints) {
            if (!checkConstraint(c)) {
                return false;
            }
        }
        return true;
    }

    private Variable findVariable() {
        Variable best = null;
        for (Variable v : variables) {
            if (v.isOneValue()) continue;
            if (best == null) {
                best = v;
            } else if (v.getSize() < best.getSize()) {
                best = v;
            }
        }
        return best;
    }

    private void findSolutions() {
        if (++nodesCounter > maxNodes) {
            throw new IllegalStateException("too many nodes");
        }
        if (!checkConstraints()) {
            return;
        }
        var v = findVariable();
        if (v == null) {
            solutionsCounter++;
            if (verbose) {
                variables.stream().filter(Variable::isNamed).forEach(System.out::println);
                System.out.println();
            }
            return;
        }

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
            v.init(value, Math.min(value + step - 1, max));
            findSolutions();
        }
        v.init(min, max);
    }

    private Variable newVar(int min, int max) {
        var v = new Variable();
        variables.add(v);
        v.init(min, max);
        return v;
    }

    private Variable newVar() {
        return newVar(Variable.MIN_VALUE, Variable.MAX_VALUE);
    }

    public Variable newVar(String name, int min, int max) {
        var v = new Variable(name);
        v.init(min, max);
        variables.add(v);
        return v;
    }

    public Variable newConstant(int value) {
        return newVar(value, value);
    }

    private void eq(Variable a, Variable b) {
        sub(newConstant(0), a, b); // 0 = A - B
    }

    private void gt(Variable a, Variable b) {
        sub(newVar(1, Variable.MAX_VALUE), a, b); // Z=A-B,Z>0
    }

    private void get(Variable a, Variable b) {
        sub(newVar(0, Variable.MAX_VALUE), a, b); // Z=A-B, Z>=0
    }

    private void lt(Variable a, Variable b) {
        gt(b, a);
    }

    private void let(Variable a, Variable b) {
        get(b, a);
    }

    private void diff(Variable a, Variable b) {
        constraints.add(new Constraint('#', a, b, null));
    }

    public void addAllDiffRelation(Variable... variables) {
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

    public void addRelation(Variable a, String relation, int constant) {
        addRelation(a, relation, newConstant(constant));
    }

    public void addRelation(Variable a, String relation, Variable b) {
        switch (relation) {
            case "=":
                eq(a, b);
                return;
            case ">":
                gt(a, b);
                return;
            case ">=":
                get(a, b);
                return;
            case "<":
                lt(a, b);
                return;
            case "<=":
                let(a, b);
                return;
            case "<>":
                diff(a, b);
                return;
        }
        throw new IllegalArgumentException("bad relation: " + relation);
    }

    public Variable expression(Object... terms) {
        var termsList = new LinkedList<>(List.of(terms));
        var result = parseAdditionTerm(termsList);
        if (!termsList.isEmpty()) {
            throw new IllegalArgumentException("bad expression: " + termsList);
        }
        return result;
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

    public long getNodesCounter() {
        return nodesCounter;
    }

    public void setMaxNodes(long maxNodes) {
        this.maxNodes = maxNodes;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

}

