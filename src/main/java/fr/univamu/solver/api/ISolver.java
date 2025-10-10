package fr.univamu.solver.api;

import fr.univamu.solver.domain.Variable;
import fr.univamu.solver.engine.Solutions;

/**
 * Interface representing a constraint programming solver.
 * This solver allows the definition of integer variables, constraints,
 * and expressions, as well as the execution of a search to find solutions.
 */
public interface ISolver {

    // ---------------------------------------------------------------------
    // Problem construction
    // ---------------------------------------------------------------------

    /**
     * Creates a new integer variable with the given name and domain.
     *
     * @param name the name of the variable.
     * @param min  the minimum value of the variable domain (inclusive).
     * @param max  the maximum value of the variable domain (inclusive).
     * @return a new {@link Variable} representing the decision variable.
     */
    Variable newVar(String name, int min, int max);

    /**
     * Creates a constant variable with the given value.
     *
     * @param value the fixed integer value.
     * @return a {@link Variable} representing this constant.
     */
    Variable newConstant(int value);

    /**
     * Adds an "all-different" constraint over the given variables.
     * Ensures that all variables take distinct values in any solution.
     *
     * @param variables the variables subject to the all-different constraint.
     */
    void addAllDiffRelation(Variable... variables);

    /**
     * Adds a relation between a variable and a constant.
     *
     * @param a         the left-hand side variable.
     * @param relation  the relation operator (e.g., "=", "<", ">", "<=", ">=", "<>").
     * @param constant  the constant on the right-hand side.
     */
    void addRelation(Variable a, String relation, int constant);

    /**
     * Adds a relation between two variables.
     *
     * @param a         the left-hand side variable.
     * @param relation  the relation operator (e.g., "=", "<", ">", "<=", ">=", "<>").
     * @param b         the right-hand side variable.
     */
    void addRelation(Variable a, String relation, Variable b);

    /**
     * Creates an expression composed of variables, constants, and operators.
     * The terms can be {@link Variable}, {@link Integer}, or operator {@link String}.
     * For example: <code>expression(x, "+", y, "*", 2)</code>.
     *
     * @param terms the components of the expression (variables, constants, operators).
     * @return a {@link Variable} representing the resulting expression.
     */
    Variable expression(Object... terms);

    // ---------------------------------------------------------------------
    // Solving
    // ---------------------------------------------------------------------

    /**
     * Starts the solving process.
     * The solver searches for all solutions satisfying all constraints.
     *
     * @return a long value representing the number of solutions.
     */
    long solve();

    // ---------------------------------------------------------------------
    // Solver configuration and monitoring
    // ---------------------------------------------------------------------

    /**
     * Enables a strategy that reduces variable domains and checks consistency
     * during propagation.
     */
    void reduceAndCheckIntervalsStrategy();

    /**
     * Returns the number of search nodes explored so far.
     *
     * @return the number of nodes explored.
     */
    long getNodesCounter();

    /**
     * Sets the maximum number of nodes to explore during the search.
     *
     * @param maxNodes the maximum allowed number of nodes.
     */
    void setMaxNodes(long maxNodes);

    /**
     * Enables or disables verbose output during the solving process.
     *
     * @param verbose true to enable detailed logs, false to disable.
     */
    void setVerbose(boolean verbose);
    
    /**
     * Returns the Solutions instance associated with the last solving process.
     * This allows access to solution count, callbacks, and other solution management features.
     *
     * @return the Solutions instance for the last solve operation
     */
    Solutions getSolutions();
}

