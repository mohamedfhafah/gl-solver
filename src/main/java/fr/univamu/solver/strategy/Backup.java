package fr.univamu.solver.strategy;

import fr.univamu.solver.domain.Variable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * Gestionnaire de sauvegarde/restauration des domaines des variables.
 * Permet d'empiler des instantanés complets des domaines afin de pouvoir
 * revenir à un état antérieur après des réductions successives.
 */
public class Backup {

    /**
     * Représente l'état d'une variable au moment de la sauvegarde.
     */
    private static final class VariableSnapshot {
        private final Variable variable;
        private final int min;
        private final int max;

        private VariableSnapshot(Variable variable) {
            this.variable = variable;
            this.min = variable.getMin();
            this.max = variable.getMax();
        }

        private void restore() {
            variable.init(min, max);
        }
    }

    private final Deque<List<VariableSnapshot>> stack = new ArrayDeque<>();

    /**
     * Sauvegarde l'état courant de toutes les variables passées en paramètre.
     *
     * @param variables liste des variables à sauvegarder
     */
    public void save(List<Variable> variables) {
        var levelSnapshots = new ArrayList<VariableSnapshot>(variables.size());
        for (Variable variable : variables) {
            levelSnapshots.add(new VariableSnapshot(variable));
        }
        stack.push(levelSnapshots);
    }

    /**
     * Restaure le dernier état sauvegardé.
     * Si aucune sauvegarde n'est disponible, l'appel est ignoré.
     */
    public void restore() {
        if (stack.isEmpty()) {
            return;
        }
        for (VariableSnapshot snapshot : stack.pop()) {
            snapshot.restore();
        }
    }

    /**
     * Vide toutes les sauvegardes enregistrées.
     */
    public void clear() {
        stack.clear();
    }

    /**
     * Indique si aucune sauvegarde n'est disponible.
     *
     * @return true si la pile est vide, false sinon
     */
    public boolean isEmpty() {
        return stack.isEmpty();
    }
}

