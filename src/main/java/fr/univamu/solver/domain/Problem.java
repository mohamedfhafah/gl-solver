package fr.univamu.solver.domain;

import java.util.List;
import java.util.Objects;

/**
 * Représente un problème de programmation par contraintes à résoudre.
 * Cette classe est immuable et thread-safe.
 *
 * Un problème est défini par :
 * - Un ensemble de variables avec leurs domaines
 * - Un ensemble de contraintes entre ces variables
 * - Une stratégie de résolution
 * - Des paramètres de configuration (limite de nœuds, verbosité)
 *
 * @param variables Liste des variables du problème (non null, copie défensive)
 * @param constraints Liste des contraintes du problème (non null, copie défensive)
 * @param strategy Stratégie de résolution (1=CHECK_INTERVALS, 2=REDUCE_AND_CHECK_INTERVALS, 3=ALWAYS_REDUCE)
 * @param maxNodes Nombre maximum de nœuds à explorer (doit être positif)
 * @param verbose Mode verbeux pour les logs de résolution
 */
public record Problem(
    List<Variable> variables,
    List<Constraint> constraints,
    int strategy,
    long maxNodes,
    boolean verbose
) {
    // Constantes de stratégie (pour cohérence avec Solver)
    public static final int CHECK_INTERVALS_STRATEGY = 1;
    public static final int REDUCE_AND_CHECK_INTERVALS_STRATEGY = 2;
    public static final int ALWAYS_REDUCE_STRATEGY = 3;

    /**
     * Constructeur compact avec validations.
     * Effectue des copies défensives des listes et valide les paramètres.
     */
    public Problem {
        // Validation et copies défensives
        Objects.requireNonNull(variables, "variables ne peut pas être null");
        Objects.requireNonNull(constraints, "constraints ne peut pas être null");

        // Copies défensives pour garantir l'immuabilité
        variables = List.copyOf(variables);
        constraints = List.copyOf(constraints);

        // Validation de la stratégie
        if (strategy != CHECK_INTERVALS_STRATEGY &&
            strategy != REDUCE_AND_CHECK_INTERVALS_STRATEGY &&
            strategy != ALWAYS_REDUCE_STRATEGY) {
            throw new IllegalArgumentException("Stratégie invalide: " + strategy +
                ". Doit être CHECK_INTERVALS_STRATEGY, REDUCE_AND_CHECK_INTERVALS_STRATEGY ou ALWAYS_REDUCE_STRATEGY");
        }

        // Validation de maxNodes
        if (maxNodes <= 0) {
            throw new IllegalArgumentException("maxNodes doit être positif: " + maxNodes);
        }
    }

    /**
     * Recherche une variable par son nom.
     *
     * @param name Le nom de la variable recherchée
     * @return La variable trouvée, ou null si aucune variable ne porte ce nom
     */
    public Variable getVariableByName(String name) {
        Objects.requireNonNull(name, "name ne peut pas être null");

        return variables.stream()
            .filter(v -> name.equals(v.getName()))
            .findFirst()
            .orElse(null);
    }

    /**
     * Vérifie si toutes les variables ont des domaines non vides.
     *
     * @return true si toutes les variables sont valides
     */
    public boolean hasValidVariables() {
        return variables.stream().allMatch(v -> !v.isEmpty());
    }

    /**
     * Retourne le nombre total de variables dans le problème.
     *
     * @return le nombre de variables
     */
    public int getVariableCount() {
        return variables.size();
    }

    /**
     * Retourne le nombre total de contraintes dans le problème.
     *
     * @return le nombre de contraintes
     */
    public int getConstraintCount() {
        return constraints.size();
    }

    /**
     * Représentation textuelle du problème.
     */
    @Override
    public String toString() {
        return String.format("Problem{variables=%d, constraints=%d, strategy=%d, maxNodes=%d, verbose=%s}",
            variables.size(), constraints.size(), strategy, maxNodes, verbose);
    }
}
