package fr.univamu.solver.strategy;

import fr.univamu.solver.domain.Constraint;
import fr.univamu.solver.domain.Variable;
import fr.univamu.solver.engine.Checker;

import java.util.List;

/**
 * Stratégie par défaut du solveur.
 * Implémente la stratégie de base actuelle :
 * - Pas de prétraitement (before retourne true)
 * - Choix de la variable avec le plus petit domaine
 * - Exploration complète du domaine (step=1) ou découpage si domaine > 1000
 * - Vérification par intervalles
 */
public class DefaultStrategy implements IStrategy {

    private final Checker checker;

    /**
     * Constructeur de la stratégie par défaut.
     * Initialise le vérificateur de contraintes.
     */
    public DefaultStrategy() {
        // Le checker sera initialisé avec les bonnes listes lors de l'utilisation
        this.checker = null; // Sera configuré dynamiquement
    }

    /**
     * Constructeur avec checker fourni.
     * Utile pour les tests ou configurations spécifiques.
     *
     * @param checker le vérificateur de contraintes à utiliser
     */
    public DefaultStrategy(Checker checker) {
        this.checker = checker;
    }

    @Override
    public boolean before(List<Variable> variables, List<Constraint> constraints) {
        // Pas de prétraitement dans la stratégie par défaut
        return true;
    }

    @Override
    public Variable chooseVariable(List<Variable> variables) {
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

    @Override
    public int step(Variable v) {
        int min = v.getMin();
        int max = v.getMax();
        int size = v.getSize();

        // Si le domaine est trop grand (> 1000), on fait du découpage
        if (size > 1000) {
            if (min < 0 && max >= 0) {
                // Si le domaine contient 0, on utilise la distance à 0 comme pas
                return -min;
            } else {
                // Sinon, on utilise la moitié du domaine comme pas
                int mid = (min + max) / 2;
                return Math.max(1, mid - min + 1);
            }
        }

        // Exploration complète par défaut
        return 1;
    }

    @Override
    public boolean check(List<Constraint> constraints, List<Variable> variables) {
        if (checker != null) {
            return checker.checkAll();
        } else {
            // Fallback: créer un checker temporaire si nécessaire
            Checker tempChecker = new Checker(constraints, variables);
            return tempChecker.checkAll();
        }
    }

    @Override
    public void backup() {
        // Stratégie par défaut : aucun état supplémentaire à sauvegarder
    }

    @Override
    public void restore() {
        // Stratégie par défaut : aucun état à restaurer
    }
}
