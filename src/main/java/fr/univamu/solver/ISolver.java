package fr.univamu.solver;

public interface ISolver {

    // build a problem
    Variable newVar(String name, int min, int max);

    Variable newConstant(int value);

    void addAllDiffRelation(Variable... variables);

    void addRelation(Variable a, String relation, int constant);
    void addRelation(Variable a, String relation, Variable b);

    Variable expression(Object... terms);

    // solve a problem
    long solve();

    // parameters
    void reduceAndCheckIntervalsStrategy();

    long getNodesCounter();

    void setMaxNodes(long maxNodes);

    void setVerbose(boolean verbose);

}
