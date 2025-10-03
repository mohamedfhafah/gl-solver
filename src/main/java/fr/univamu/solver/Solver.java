package fr.univamu.solver;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

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

    // Pour l'optimisation des variables intermédiaires
    private final Set<Variable> intermediateVariables = new HashSet<>();
    private Variable pendingOptimizationResult = null;

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

    private Variable parseMultiplicationTerm(List<Object> terms, boolean optimize) {
        var first = parseSimpleTerm(terms);
        if (parseToken("*", terms)) {
            var second = parseMultiplicationTerm(terms, optimize);
            var result = newVar();
            if (optimize) {
                markAsIntermediate(result); // Marquer comme variable intermédiaire
            }
            mul(result, first, second);
            return result;
        }
        if (parseToken("/", terms)) {
            var second = parseMultiplicationTerm(terms, optimize);
            var result = newVar();
            if (optimize) {
                markAsIntermediate(result); // Marquer comme variable intermédiaire
            }
            div(result, first, second);
            return result;
        }
        return first;
    }

    private Variable parseAdditionTerm(List<Object> terms, boolean optimize) {
        var first = parseMultiplicationTerm(terms, optimize);
        if (parseToken("+", terms)) {
            var second = parseAdditionTerm(terms, optimize);
            var result = newVar();
            if (optimize) {
                markAsIntermediate(result); // Marquer comme variable intermédiaire
            }
            add(result, first, second);
            return result;
        }
        if (parseToken("-", terms)) {
            var second = parseAdditionTerm(terms, optimize);
            var result = newVar();
            if (optimize) {
                markAsIntermediate(result); // Marquer comme variable intermédiaire
            }
            add(first, result, second);
            return result;
        }
        return first;
    }

    public void addRelation(Variable a, String relation, int constant) {
        addRelation(a, relation, newConstant(constant));
    }

    public void addRelation(Variable a, String relation, Variable b) {
        // Si 'a' est le résultat d'une expression optimisée, déclencher l'optimisation
        if (pendingOptimizationResult != null && pendingOptimizationResult.equals(a)) {
            // Appliquer l'optimisation avant d'ajouter la relation
            optimizeIntermediateVariables(a);

            // Nettoyer le marqueur d'optimisation
            pendingOptimizationResult = null;
        }

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
        return expression(false, terms);
    }

    /**
     * Analyse une expression avec possibilité d'optimisation.
     *
     * @param optimize true pour activer l'élimination des variables intermédiaires
     * @param terms termes de l'expression
     * @return la variable résultat
     */
    public Variable expression(boolean optimize, Object... terms) {
        var termsList = new LinkedList<>(List.of(terms));
        var result = parseAdditionTerm(termsList, optimize);
        if (!termsList.isEmpty()) {
            throw new IllegalArgumentException("bad expression: " + termsList);
        }

        // L'optimisation doit être faite immédiatement après la création de l'expression,
        // avant que d'autres relations soient ajoutées
        if (optimize) {
            // Marquer le résultat comme étant une expression optimisée
            // L'optimisation se fera lors du prochain addRelation
            pendingOptimizationResult = result;
        }

        return result;
    }

    /**
     * Marque une variable comme étant intermédiaire (candidate à l'élimination).
     */
    private void markAsIntermediate(Variable var) {
        intermediateVariables.add(var);
    }

    /**
     * Optimise les variables intermédiaires en les éliminant si possible.
     * Cette méthode est appelée après l'analyse d'une expression optimisée.
     *
     * @param result la variable résultat de l'expression
     */
    private void optimizeIntermediateVariables(Variable result) {
        // Pour chaque variable intermédiaire
        for (Variable intermediate : intermediateVariables) {
            if (canEliminateIntermediate(intermediate, result)) {
                // Remplacer la variable intermédiaire dans sa contrainte de définition
                substituteIntermediateVariable(null, intermediate, result);

                // Supprimer la variable intermédiaire et ses contraintes associées
                removeIntermediateVariable(intermediate);
            }
        }

        // Nettoyer la liste des variables intermédiaires après optimisation
        intermediateVariables.clear();
    }

    /**
     * Cherche une contrainte d'égalité où la variable donnée est impliquée.
     * Une égalité A = B est représentée comme 0 = A - B.
     */
    private Constraint findEqualityConstraint(Variable var) {
        for (Constraint c : constraints) {
            // Chercher une contrainte de soustraction où var est var1 et result est 0 (constante)
            if (c.type() == '-' && c.var1().equals(var) && isConstantZero(c.result())) {
                return c;
            }
        }
        return null;
    }

    /**
     * Vérifie si une variable représente la constante 0.
     */
    private boolean isConstantZero(Variable var) {
        return var.getMin() == 0 && var.getMax() == 0;
    }

    /**
     * Vérifie si une variable intermédiaire peut être éliminée.
     * Une variable peut être éliminée si elle n'est utilisée que dans une égalité simple.
     */
    private boolean canEliminateIntermediate(Variable intermediate, Variable result) {
        // Compter combien de fois la variable intermédiaire est utilisée
        int usageCount = 0;
        boolean hasDefinition = false;
        boolean hasEquality = false;

        for (Constraint c : constraints) {
            // Vérifier si c'est la contrainte de définition (intermediate est le résultat)
            if (c.result().equals(intermediate)) {
                hasDefinition = true;
                usageCount++;
            }
            // Vérifier si c'est utilisé dans une égalité (représentée comme 0 = intermediate - something)
            else if (c.type() == '-' && isConstantZero(c.result()) && c.var1().equals(intermediate)) {
                hasEquality = true;
                usageCount++;
            }
            // Vérifier si c'est utilisé ailleurs (comme opérande)
            else if (c.var1().equals(intermediate) || (c.var2() != null && c.var2().equals(intermediate))) {
                usageCount++;
            }
        }

        // Peut être éliminée si elle a une définition ET une égalité, et n'est pas utilisée ailleurs
        return hasDefinition && hasEquality && usageCount == 2;
    }

    /**
     * Substitue une variable intermédiaire dans sa contrainte de définition.
     * Transforme "intermediate = expression" en "target = expression"
     */
    private void substituteIntermediateVariable(Constraint equalityConstraint, Variable intermediate, Variable target) {
        // Trouver la contrainte de définition de la variable intermédiaire
        Constraint definitionConstraint = findDefinitionConstraint(intermediate);

        if (definitionConstraint != null) {
            // Créer une nouvelle contrainte avec target comme résultat
            Constraint newConstraint = new Constraint(
                definitionConstraint.type(),
                target,  // Nouveau résultat
                definitionConstraint.var1(),
                definitionConstraint.var2()
            );

            // Remplacer la contrainte de définition
            constraints.remove(definitionConstraint);
            constraints.add(newConstraint);
        }
    }

    /**
     * Trouve la contrainte de définition d'une variable (où elle est le résultat).
     */
    private Constraint findDefinitionConstraint(Variable var) {
        for (Constraint c : constraints) {
            if (c.result().equals(var)) {
                return c;
            }
        }
        return null;
    }

    /**
     * Supprime une variable intermédiaire et toutes ses contraintes associées.
     */
    private void removeIntermediateVariable(Variable intermediate) {
        // Supprimer toutes les contraintes qui utilisent cette variable
        constraints.removeIf(c ->
            c.var1().equals(intermediate) ||
            c.result().equals(intermediate) ||
            (c.var2() != null && c.var2().equals(intermediate))
        );

        // Supprimer la variable elle-même
        variables.remove(intermediate);
    }

    /**
     * Construit un Problem immuable à partir des données internes du Solver.
     * Utilise ProblemBuilder pour créer une représentation structurée du problème.
     */
    private Problem buildProblem() {
        var builder = new ProblemBuilder();

        // Ajouter toutes les variables
        for (Variable variable : variables) {
            builder.addVariable(variable);
        }

        // Ajouter toutes les contraintes
        for (Constraint constraint : constraints) {
            builder.addConstraint(constraint);
        }

        // Configurer les paramètres
        builder.setStrategy(strategy)
               .setMaxNodes(maxNodes)
               .setVerbose(verbose);

        return builder.build();
    }

    public long solve() {
        // Construire le problème immuable avec ProblemBuilder
        Problem problem = buildProblem();

        // Log du problème construit (si verbose)
        if (verbose) {
            System.out.println("🔧 Problème construit avec ProblemBuilder:");
            System.out.println("   • " + problem.getVariableCount() + " variables");
            System.out.println("   • " + problem.getConstraintCount() + " contraintes");
            System.out.println("   • Stratégie: " + problem.strategy());
            System.out.println("   • Max nœuds: " + problem.maxNodes());
        }

        // Réinitialiser les compteurs et résoudre
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

