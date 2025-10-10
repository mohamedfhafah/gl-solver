package fr.univamu.solver.engine;

import fr.univamu.solver.domain.Constraint;
import fr.univamu.solver.domain.Variable;

import java.util.List;

/**
 * Classe responsable de la vérification de la consistance des contraintes.
 * Centralise toutes les méthodes de vérification extraites du Solver.
 */
public class Checker {

    // Constantes pour les stratégies de vérification
    public static final int CHECK_INTERVALS_STRATEGY = 1;
    public static final int REDUCE_AND_CHECK_INTERVALS_STRATEGY = 2;

    private final List<Constraint> constraints;
    private final List<Variable> variables;
    private long checkCounter = 0;

    /**
     * Constructeur du Checker.
     *
     * @param constraints la liste des contraintes à vérifier
     * @param variables la liste des variables du problème
     */
    public Checker(List<Constraint> constraints, List<Variable> variables) {
        this.constraints = constraints;
        this.variables = variables;
    }

    /**
     * Vérifie toutes les contraintes.
     *
     * @return true si toutes les contraintes sont satisfaites, false sinon
     */
    public boolean checkAll() {
        for (Constraint c : constraints) {
            if (!checkConstraint(c, CHECK_INTERVALS_STRATEGY)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Vérifie une contrainte spécifique selon la stratégie donnée.
     *
     * @param c la contrainte à vérifier
     * @param strategy la stratégie de vérification (CHECK_INTERVALS_STRATEGY ou REDUCE_AND_CHECK_INTERVALS_STRATEGY)
     * @return true si la contrainte est satisfaite, false sinon
     */
    public boolean checkConstraint(Constraint c, int strategy) {
        checkCounter++;
        return switch (strategy) {
            case CHECK_INTERVALS_STRATEGY, REDUCE_AND_CHECK_INTERVALS_STRATEGY -> checkConstraintIntervalsStrategy(c);
            default -> throw new IllegalStateException("bad strategy: " + strategy);
        };
    }

    /**
     * Vérifie une contrainte en utilisant la stratégie d'intervalles.
     *
     * @param c la contrainte à vérifier
     * @return true si la contrainte est satisfaite selon les intervalles, false sinon
     */
    private boolean checkConstraintIntervalsStrategy(Constraint c) {
        return switch (c.type()) {
            case ADD -> checkAddConstraintIntervalsStrategy(c);
            case DIFF -> checkDiffConstraintIntervalsStrategy(c);
            case MUL -> checkMulConstraintIntervalsStrategy(c);
            case DIV -> checkDivConstraintIntervalsStrategy(c);
        };
    }

    /**
     * Vérifie une contrainte d'addition (A + B = C) selon les intervalles.
     *
     * @param c la contrainte d'addition
     * @return true si l'intervalle résultat contient l'intervalle de la somme A+B
     */
    private boolean checkAddConstraintIntervalsStrategy(Constraint c) {
        return c.var1().add(c.var2()).inter(c.result()).isNotEmpty();
    }

    /**
     * Vérifie une contrainte de multiplication (A * B = C) selon les intervalles.
     *
     * @param c la contrainte de multiplication
     * @return true si l'intervalle résultat contient l'intervalle du produit A*B
     */
    private boolean checkMulConstraintIntervalsStrategy(Constraint c) {
        return c.var1().mul(c.var2()).inter(c.result()).isNotEmpty();
    }

    /**
     * Vérifie une contrainte de division (A / B = C) selon les intervalles.
     *
     * @param c la contrainte de division
     * @return true si l'intervalle résultat contient l'intervalle du quotient A/B
     */
    private boolean checkDivConstraintIntervalsStrategy(Constraint c) {
        return c.var1().div(c.var2()).inter(c.result()).isNotEmpty();
    }

    /**
     * Vérifie une contrainte de différence (A ≠ B).
     *
     * @param c la contrainte de différence
     * @return true si la contrainte n'est pas trivialement fausse
     */
    private boolean checkDiffConstraintIntervalsStrategy(Constraint c) {
        var result = c.result();
        var ko = result.equals(c.var1()) && result.isOneValue();
        return (!ko);
    }

    /**
     * Retourne le nombre total de vérifications effectuées.
     *
     * @return le compteur de vérifications
     */
    public long getCheckCounter() {
        return checkCounter;
    }

    /**
     * Remet le compteur de vérifications à zéro.
     */
    public void resetCheckCounter() {
        checkCounter = 0;
    }
}

