package fr.univamu.solver.engine;

import fr.univamu.solver.api.ISolver;
import fr.univamu.solver.api.ProblemBuilder;
import fr.univamu.solver.domain.Constraint;
import fr.univamu.solver.domain.ConstraintType;
import fr.univamu.solver.domain.Problem;
import fr.univamu.solver.domain.Variable;
import fr.univamu.solver.strategy.AlwaysReduceStrategy;
import fr.univamu.solver.strategy.DefaultStrategy;
import fr.univamu.solver.strategy.IStrategy;
import fr.univamu.solver.strategy.ReduceAndCheckIntervalsStrategy;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Solver implements ISolver {

    private final List<Constraint> constraints = new LinkedList<>();
    private final List<Variable> variables = new LinkedList<>();
    private IStrategy strategy;
    private final Checker checker;
    private final Reducer reducer;

    private Solutions solutions = new Solutions();

    /**
     * Constructeur par défaut du Solver.
     */
    public Solver() {
        this.checker = new Checker(constraints, variables);
        this.reducer = new Reducer(constraints, variables);
        this.strategy = new DefaultStrategy(checker); // Stratégie par défaut
    }
    private long nodesCounter = 0;
    private long maxNodes = 1000_000_000L;
    private boolean verbose = true;

    public void reduceAndCheckIntervalsStrategy() {
        strategy = new ReduceAndCheckIntervalsStrategy(reducer, checker);
    }

    public void alwaysReduceStrategy() {
        strategy = new AlwaysReduceStrategy(reducer, checker, variables);
    }

    /**
     * Retourne le type de stratégie sous forme d'entier pour la compatibilité
     * avec ProblemBuilder (qui utilise encore l'ancien système).
     */
    private int getStrategyType() {
        if (strategy instanceof AlwaysReduceStrategy) {
            return 3; // ALWAYS_REDUCE_STRATEGY
        } else if (strategy instanceof ReduceAndCheckIntervalsStrategy) {
            return 2; // REDUCE_AND_CHECK_INTERVALS_STRATEGY
        } else {
            return 1; // CHECK_INTERVALS_STRATEGY (par défaut)
        }
    }


    private void reduce() {
        if (verbose) {
            System.out.println("Variables before reduction:");
            variables.forEach(System.out::println);
        }

        reducer.reduce();

        if (verbose) {
            System.out.println("Variables after reduction:");
            variables.forEach(System.out::println);
        }
    }



    private void findSolutions() {
        strategy.backup();
        try {
            if (++nodesCounter > maxNodes) {
                throw new IllegalStateException("too many nodes");
            }
            if (!strategy.check(constraints, variables)) {
                return;
            }
            var v = strategy.chooseVariable(variables);
            if (v == null) {
                solutions.addSolution(variables);
                return;
            }

            int min = v.getMin();
            int max = v.getMax();

            // Utiliser la stratégie pour déterminer le pas d'exploration
            int step = strategy.step(v);

            // explorer le domaine
            for (int value = min; value <= max; value += step) {
                int upperBound = Math.min(value + step - 1, max);
                strategy.backup();
                try {
                    v.init(value, upperBound);
                    findSolutions();
                } finally {
                    strategy.restore();
                }
            }
            v.init(min, max);
        } finally {
            strategy.restore();
        }
    }

    private void registerVariableIfNeeded(Variable variable) {
        if (variable == null) {
            return;
        }
        boolean alreadyRegistered = variables.stream().anyMatch(existing -> existing == variable);
        if (!alreadyRegistered) {
            variables.add(variable);
            if (reducer != null) {
                reducer.registerVariable(variable);
            }
        }
    }

    private Variable newVar(int min, int max) {
        var v = new Variable();
        registerVariableIfNeeded(v);
        v.init(min, max);
        return v;
    }

    private Variable newVar() {
        return newVar(Variable.MIN_VALUE, Variable.MAX_VALUE);
    }

    public Variable newVar(String name, int min, int max) {
        var v = new Variable(name);
        v.init(min, max);
        registerVariableIfNeeded(v);
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
        constraints.add(new Constraint(ConstraintType.DIFF, a, b, null));
    }

    public void addAllDiffRelation(Variable... variables) {
        for (Variable variable : variables) {
            registerVariableIfNeeded(variable);
        }
        for (int i = 0; i < variables.length; i++)
            for (int j = i + 1; j < variables.length; j++) {
                diff(variables[i], variables[j]);
            }
    }

    private void add(Variable result, Variable a, Variable b) {
        constraints.add(new Constraint(ConstraintType.ADD, result, a, b));
    }

    private void sub(Variable result, Variable a, Variable b) {
        add(a, result, b);
    }

    private void mul(Variable result, Variable a, Variable b) {
        constraints.add(new Constraint(ConstraintType.MUL, result, a, b));
    }

    private void div(Variable result, Variable a, Variable b) {
        constraints.add(new Constraint(ConstraintType.DIV, result, a, b));
    }

    private Variable parseSimpleTerm(List<Object> terms) {
        var first = terms.removeFirst();
        if (first instanceof Variable var) {
            registerVariableIfNeeded(var);
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
        registerVariableIfNeeded(a);
        // OPTIMISATION: Pour les relations simples avec constantes,
        // ajuster directement le domaine au lieu de créer une contrainte
        switch (relation) {
            case "=":
                // X = constante : réduire le domaine à une seule valeur
                if (constant >= a.getMin() && constant <= a.getMax()) {
                    a.init(constant, constant);
                } else {
                    // Contrainte impossible, domaine vide
                    a.init(1, 0); // Crée un domaine vide
                }
                return;
            case ">":
                // X > constante : domaine commence à constante+1
                a.init(Math.max(a.getMin(), constant + 1), a.getMax());
                return;
            case ">=":
                // X >= constante : domaine commence à constante
                a.init(Math.max(a.getMin(), constant), a.getMax());
                return;
            case "<":
                // X < constante : domaine finit à constante-1
                a.init(a.getMin(), Math.min(a.getMax(), constant - 1));
                return;
            case "<=":
                // X <= constante : domaine finit à constante
                a.init(a.getMin(), Math.min(a.getMax(), constant));
                return;
            default:
                // Pour les autres relations, utiliser l'approche normale
                addRelation(a, relation, newConstant(constant));
                return;
        }
    }

    public void addRelation(Variable a, String relation, Variable b) {
        registerVariableIfNeeded(a);
        registerVariableIfNeeded(b);
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

    /**
     * Analyse une expression avec optimisation automatique des variables intermédiaires.
     * Cette méthode applique une optimisation pour réduire le nombre de variables et contraintes
     * en éliminant les variables intermédiaires inutiles.
     *
     * Pour une expression comme A + 2 = B, transforme :
     *   T1 ∈ [2,2]        T1 ∈ [2,2]
     *   T2 = A + T1  -->   B = A + T1
     *   T2 = B
     *
     * @param terms termes de l'expression
     * @return la variable résultat optimisée
     */
    public Variable expressionOptimized(Object... terms) {
        // Pour l'instant, construction normale sans optimisation
        // L'optimisation sera appelée manuellement après avoir ajouté toutes les relations
        return expression(terms);
    }

    /**
     * Applique l'optimisation des variables intermédiaires.
     * Doit être appelée après avoir ajouté toutes les contraintes.
     */
    public void optimize() {
        // Trouver la variable résultat principale (celle qui n'est pas intermédiaire)
        Variable mainResult = null;
        for (Constraint c : constraints) {
            if (c.result() != null && !isIntermediateVariable(c.result())) {
                mainResult = c.result();
                break;
            }
        }

        if (mainResult != null) {
            optimizeIntermediateVariables(mainResult);
        } else {
            // Si pas de résultat principal trouvé, optimiser avec null
            optimizeIntermediateVariables(null);
        }
    }

    /**
     * Optimise les variables intermédiaires en les éliminant si possible.
     * Applique une optimisation conservatrice qui préserve l'équivalence des solutions.
     *
     * Pour une expression comme A + 2 = B, transforme :
     *   T1 ∈ [2,2]        T1 ∈ [2,2]
     *   T2 = A + T1  -->   B = A + T1
     *   T2 = B
     *
     * @param result la variable résultat de l'expression (non utilisée dans cette version)
     */
    private void optimizeIntermediateVariables(Variable result) {
        // Collecter tous les candidats à l'optimisation
        List<Variable> candidates = new ArrayList<>();

        // Identifier les variables intermédiaires (celles qui sont résultats d'opérations)
        for (Constraint c : constraints) {
            if (c.result() != null && isIntermediateVariable(c.result())) {
                candidates.add(c.result());
            }
        }

        // Traiter chaque candidat
        for (Variable intermediate : candidates) {
            if (canEliminateIntermediate(intermediate)) {
                performElimination(intermediate);
            }
        }
    }

    /**
     * Vérifie si une variable est considérée comme intermédiaire.
     * Une variable intermédiaire est celle dont le nom commence par un préfixe interne.
     */
    private boolean isIntermediateVariable(Variable var) {
        // Les variables intermédiaires ont des noms générés automatiquement
        // commençant par un caractère non alphabétique ou spécial
        String name = var.getName();
        return name.startsWith("T") || name.contains("$") || !Character.isLetter(name.charAt(0));
    }

    /**
     * Vérifie si une variable intermédiaire peut être éliminée.
     * Elle peut l'être si elle n'est utilisée que dans une égalité simple.
     */
    private boolean canEliminateIntermediate(Variable intermediate) {
        // Compter les utilisations de cette variable
        int usageCount = 0;
        Constraint equalityConstraint = null;

        for (Constraint c : constraints) {
            // Vérifier si c'est la définition de la variable intermédiaire (pas une égalité simple)
            if (c.result().equals(intermediate) && !(c.type() == ConstraintType.ADD && c.var1() != null && isConstantZero(c.var1()) && c.var2() != null)) {
                usageCount++;
            }
            // Vérifier si c'est une utilisation dans une égalité (intermediate = 0 + target)
            else if (c.type() == ConstraintType.ADD && c.result().equals(intermediate) &&
                     c.var1() != null && isConstantZero(c.var1()) && c.var2() != null) {
                equalityConstraint = c;
                usageCount++;
            }
            // Vérifier si c'est une utilisation ailleurs (comme opérande)
            else if ((c.var1() != null && c.var1().equals(intermediate)) ||
                     (c.var2() != null && c.var2().equals(intermediate))) {
                usageCount++;
            }
        }
        // Peut être éliminée seulement si : définition + 1 égalité + rien d'autre
        return usageCount == 2 && equalityConstraint != null;
    }

    /**
     * Effectue l'élimination d'une variable intermédiaire.
     * Remplace la variable dans sa définition et supprime les contraintes inutiles.
     */
    private void performElimination(Variable intermediate) {
        // Trouver la contrainte d'égalité (intermediate = 0 + target)
        Constraint equalityConstraint = null;
        for (Constraint c : constraints) {
            if (c.type() == ConstraintType.ADD && c.result().equals(intermediate) &&
                c.var1() != null && isConstantZero(c.var1()) && c.var2() != null) {
                equalityConstraint = c;
                break;
            }
        }

        if (equalityConstraint == null) return;

        // La variable cible de l'égalité
        Variable target = equalityConstraint.var2();

        // Trouver la contrainte de définition (result = intermediate)
        Constraint definitionConstraint = null;
        for (Constraint c : constraints) {
            if (c.result().equals(intermediate)) {
                definitionConstraint = c;
                break;
            }
        }

        if (definitionConstraint == null) return;

        // Créer la nouvelle contrainte : target = définition originale
        Constraint newConstraint = new Constraint(
            definitionConstraint.type(),
            target,  // Nouveau résultat = la cible de l'égalité
            definitionConstraint.var1(),
            definitionConstraint.var2()
        );

        // Remplacer l'ancienne contrainte de définition par la nouvelle
        constraints.remove(definitionConstraint);
        constraints.add(newConstraint);

        // Supprimer la contrainte d'égalité
        constraints.remove(equalityConstraint);

        // Supprimer la constante zéro si elle n'est plus utilisée
        Variable zero = equalityConstraint.var1();
        if (zero != null && isConstantZero(zero)) {
            boolean stillUsed = constraints.stream().anyMatch(c ->
                c.result() == zero || c.var1() == zero || c.var2() == zero);
            if (!stillUsed) {
                variables.remove(zero);
            }
        }

        // Supprimer la variable intermédiaire
        variables.remove(intermediate);
    }

    /**
     * Vérifie si une variable représente la constante 0.
     */
    private boolean isConstantZero(Variable var) {
        boolean isZero = var.getMin() == 0 && var.getMax() == 0;
        return isZero;
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
        builder.setStrategy(getStrategyType())
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
        solutions.reset();
        this.nodesCounter = 0;

        // Appliquer la phase before() de la stratégie (réduction éventuelle)
        strategy.before(variables, constraints);

        findSolutions();
        return solutions.getCount();
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
    
    public Solutions getSolutions() {
        return solutions;
    }

    public List<Variable> getVariables() {
        return variables;
    }

    public List<Constraint> getConstraints() {
        return constraints;
    }

}
