package fr.univamu.solver;

import java.util.List;

/**
 * Classe responsable de la réduction des domaines des variables selon les contraintes.
 * Centralise toutes les méthodes de réduction extraites du Solver.
 */
public class Reducer {

    private final List<Constraint> constraints;
    private final List<Variable> variables;
    private boolean modified = false;

    /**
     * Constructeur du Reducer.
     *
     * @param constraints la liste des contraintes à réduire
     * @param variables la liste des variables du problème
     */
    public Reducer(List<Constraint> constraints, List<Variable> variables) {
        this.constraints = constraints;
        this.variables = variables;
    }

    /**
     * Réduit les domaines des variables en appliquant toutes les contraintes
     * jusqu'à ce qu'aucune modification ne soit possible.
     */
    public void reduce() {
        do {
            modified = false;
            constraints.forEach(this::reduceConstraint);
        } while (modified);
    }

    /**
     * Réduit les domaines selon une contrainte spécifique.
     *
     * @param c la contrainte à appliquer pour la réduction
     */
    public void reduceConstraint(Constraint c) {
        switch (c.type()) {
            case ADD:
                reduceAddConstraint(c);
                break;
            case MUL:
                reduceMulConstraint(c);
                break;
            case DIFF:
                reduceDiffConstraint(c);
                break;
        }
    }

    /**
     * Réduit les domaines selon une contrainte d'addition (A + B = C).
     * Applique les réductions dans les deux sens.
     *
     * @param c la contrainte d'addition
     */
    private void reduceAddConstraint(Constraint c) {
        modified = c.result().reduce(c.var1().add(c.var2())) || modified;
        modified = c.var1().reduce(c.result().sub(c.var2())) || modified;
        modified = c.var2().reduce(c.result().sub(c.var1())) || modified;
    }

    /**
     * Réduit les domaines selon une contrainte de multiplication (A * B = C).
     * Applique plusieurs itérations pour converger vers la solution optimale.
     *
     * @param c la contrainte de multiplication
     */
    private void reduceMulConstraint(Constraint c) {
        for (int i = 0; i < 3; i++) {
            modified = c.result().reduce(c.var1().mul(c.var2())) || modified;
            modified = c.var2().reduce(c.result().inverseMul(c.var1())) || modified;
            modified = c.var1().reduce(c.result().inverseMul(c.var2())) || modified;
        }
    }

    /**
     * Réduit les domaines selon une contrainte de différence (A ≠ B).
     * Implémente les 3 cas spécifiés dans l'énoncé :
     * - Si X est fixé et X.value == Y.min: Y.init(Y.min + 1, Y.max)
     * - Si X est fixé et X.value == Y.max: Y.init(Y.min, Y.max - 1)
     * - Si Y est fixé et Y.value == X.min: X.init(X.min + 1, X.max)
     * - Si Y est fixé et Y.value == X.max: X.init(X.min, X.max - 1)
     * - Si X et Y sont fixés et égaux: domaines vides (contradiction)
     *
     * @param c la contrainte de différence
     */
    private void reduceDiffConstraint(Constraint c) {
        Variable x = c.result(); // Dans DIFF, result représente la première variable (X)
        Variable y = c.var1();   // var1 représente la deuxième variable (Y)


        // Cas 3: X et Y sont tous deux fixés et égaux → contradiction (PRIORITAIRE)
        if (x.isOneValue() && y.isOneValue() && x.getMin() == y.getMin()) {
            // Crée des domaines vides (contradiction détectée)
            x.init(1, 0); // domaine vide
            y.init(1, 0); // domaine vide
            modified = true;
            return; // Important: sortir immédiatement après contradiction
        }

        // Cas 1: X est fixé
        if (x.isOneValue()) {
            int xValue = x.getMin();

            // Cas 1a: X.value == Y.min → Y.min++
            if (y.getMin() == xValue) {
                modified = y.reduce(y.getMin() + 1, y.getMax()) || modified;
            }

            // Cas 1b: X.value == Y.max → Y.max--
            if (y.getMax() == xValue) {
                modified = y.reduce(y.getMin(), y.getMax() - 1) || modified;
            }
        }

        // Cas 2: Y est fixé
        if (y.isOneValue()) {
            int yValue = y.getMin();

            // Cas 2a: Y.value == X.min → X.min++
            if (x.getMin() == yValue) {
                modified = x.reduce(x.getMin() + 1, x.getMax()) || modified;
            }

            // Cas 2b: Y.value == X.max → X.max--
            if (x.getMax() == yValue) {
                modified = x.reduce(x.getMin(), x.getMax() - 1) || modified;
            }
        }
    }

    /**
     * Indique si la dernière opération de réduction a modifié les domaines.
     *
     * @return true si des modifications ont été apportées, false sinon
     */
    public boolean isModified() {
        return modified;
    }

    /**
     * Remet le flag modified à false.
     */
    public void resetModified() {
        modified = false;
    }
}
