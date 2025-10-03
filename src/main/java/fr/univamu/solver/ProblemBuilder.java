package fr.univamu.solver;

import java.util.ArrayList;
import java.util.List;

/**
 * Builder pour construire des problèmes de programmation par contraintes.
 * Permet de construire étape par étape un problème en ajoutant variables,
 * contraintes et paramètres de configuration.
 *
 * Pattern Builder fluide permettant une construction déclarative des problèmes.
 */
public class ProblemBuilder {

    // Champs mutables pour l'accumulation des données
    private final List<Variable> variables = new ArrayList<>();
    private final List<Constraint> constraints = new ArrayList<>();
    private int strategy = Problem.CHECK_INTERVALS_STRATEGY;
    private long maxNodes = 1000_000_000L; // Valeur par défaut
    private boolean verbose = true; // Valeur par défaut

    /**
     * Constructeur par défaut créant un builder vide.
     */
    public ProblemBuilder() {
        // Initialisation avec les valeurs par défaut
    }

    /**
     * Ajoute une variable au problème en cours de construction.
     *
     * @param variable La variable à ajouter (non null)
     * @return this pour chaînage fluide
     * @throws IllegalArgumentException si variable est null
     */
    public ProblemBuilder addVariable(Variable variable) {
        if (variable == null) {
            throw new IllegalArgumentException("variable ne peut pas être null");
        }
        this.variables.add(variable);
        return this;
    }

    /**
     * Ajoute une contrainte au problème en cours de construction.
     *
     * @param constraint La contrainte à ajouter (non null)
     * @return this pour chaînage fluide
     * @throws IllegalArgumentException si constraint est null
     */
    public ProblemBuilder addConstraint(Constraint constraint) {
        if (constraint == null) {
            throw new IllegalArgumentException("constraint ne peut pas être null");
        }
        this.constraints.add(constraint);
        return this;
    }

    /**
     * Définit la stratégie de résolution.
     *
     * @param strategy La stratégie (CHECK_INTERVALS_STRATEGY ou REDUCE_AND_CHECK_INTERVALS_STRATEGY)
     * @return this pour chaînage fluide
     * @throws IllegalArgumentException si la stratégie est invalide
     */
    public ProblemBuilder setStrategy(int strategy) {
        if (strategy != Problem.CHECK_INTERVALS_STRATEGY &&
            strategy != Problem.REDUCE_AND_CHECK_INTERVALS_STRATEGY) {
            throw new IllegalArgumentException("Stratégie invalide: " + strategy);
        }
        this.strategy = strategy;
        return this;
    }

    /**
     * Définit le nombre maximum de nœuds à explorer.
     *
     * @param maxNodes Le nombre maximum de nœuds (doit être positif)
     * @return this pour chaînage fluide
     * @throws IllegalArgumentException si maxNodes <= 0
     */
    public ProblemBuilder setMaxNodes(long maxNodes) {
        if (maxNodes <= 0) {
            throw new IllegalArgumentException("maxNodes doit être positif: " + maxNodes);
        }
        this.maxNodes = maxNodes;
        return this;
    }

    /**
     * Définit le mode verbeux pour les logs.
     *
     * @param verbose true pour activer les logs détaillés, false sinon
     * @return this pour chaînage fluide
     */
    public ProblemBuilder setVerbose(boolean verbose) {
        this.verbose = verbose;
        return this;
    }

    /**
     * Construit le problème immuable avec toutes les données accumulées.
     *
     * @return Un nouveau Problem immuable
     * @throws IllegalStateException si le problème n'a pas de variables
     */
    public Problem build() {
        if (variables.isEmpty()) {
            throw new IllegalStateException("Un problème doit avoir au moins une variable");
        }

        return new Problem(variables, constraints, strategy, maxNodes, verbose);
    }

    // Getters pour inspection (optionnels, principalement pour debug)

    /**
     * Retourne le nombre de variables ajoutées (pour debug).
     */
    public int getVariableCount() {
        return variables.size();
    }

    /**
     * Retourne le nombre de contraintes ajoutées (pour debug).
     */
    public int getConstraintCount() {
        return constraints.size();
    }
}
