package fr.univamu.solver.strategy;

import fr.univamu.solver.domain.Constraint;
import fr.univamu.solver.domain.Variable;

import java.util.List;

/**
 * Interface définissant une stratégie de résolution pour le solveur de contraintes.
 * Une stratégie contrôle quatre aspects de la recherche :
 * - La simplification du problème avant la recherche
 * - Le choix de la prochaine variable à instancier
 * - La manière d'explorer le domaine de cette variable
 * - La vérification de consistance après chaque exploration
 */
public interface IStrategy {

    /**
     * Effectue un travail de simplification du problème avant le début de la recherche.
     * Par exemple, réduction des domaines des variables selon les contraintes.
     *
     * @param variables la liste des variables du problème
     * @param constraints la liste des contraintes du problème
     * @return true si des modifications ont été apportées, false sinon
     */
    boolean before(List<Variable> variables, List<Constraint> constraints);

    /**
     * Choisit la prochaine variable à instancier parmi celles qui ne sont pas encore fixées.
     * Généralement, on choisit la variable avec le domaine le plus petit.
     *
     * @param variables la liste des variables du problème
     * @return la variable choisie pour l'instantiation, ou null si toutes sont fixées
     */
    Variable chooseVariable(List<Variable> variables);

    /**
     * Détermine comment explorer le domaine d'une variable.
     * Retourne le pas d'exploration (1 pour exploration complète, >1 pour échantillonnage).
     *
     * @param v la variable dont on va explorer le domaine
     * @return le pas d'exploration (toujours >= 1)
     */
    int step(Variable v);

    /**
     * Vérifie la consistance du problème après avoir exploré une valeur du domaine d'une variable.
     * Cette vérification peut être complète ou partielle selon la stratégie.
     *
     * @param constraints la liste des contraintes à vérifier
     * @param variables la liste des variables du problème
     * @return true si le problème est encore consistant, false sinon
     */
    boolean check(List<Constraint> constraints, List<Variable> variables);

    /**
     * Sauvegarde l'état nécessaire de la stratégie avant une exploration récursive.
     * Par défaut, ne fait rien. Les stratégies avançées peuvent l'utiliser pour
     * mémoriser les domaines des variables ou d'autres informations de contexte.
     */
    default void backup() {
        // Par défaut, aucune sauvegarde nécessaire
    }

    /**
     * Restaure l'état précédemment sauvegardé par {@link #backup()} après un retour arrière.
     * Par défaut, ne fait rien.
     */
    default void restore() {
        // Par défaut, rien à restaurer
    }
}
