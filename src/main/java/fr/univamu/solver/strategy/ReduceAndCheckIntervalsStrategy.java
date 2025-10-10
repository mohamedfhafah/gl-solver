package fr.univamu.solver.strategy;

import fr.univamu.solver.domain.Constraint;
import fr.univamu.solver.domain.Variable;
import fr.univamu.solver.engine.Checker;
import fr.univamu.solver.engine.Reducer;

import java.util.List;

/**
 * Stratégie qui applique la réduction d'intervalles avant la recherche.
 * Hérite de DefaultStrategy pour la logique de base, mais ajoute
 * une phase de réduction des domaines dans before().
 */
public class ReduceAndCheckIntervalsStrategy extends DefaultStrategy {

    private final Reducer reducer;

    /**
     * Constructeur de la stratégie avec réduction.
     * Initialise le réducteur de contraintes.
     */
    public ReduceAndCheckIntervalsStrategy() {
        // Le reducer sera configuré dynamiquement lors de l'utilisation
        this.reducer = null;
    }

    /**
     * Constructeur avec reducer fourni.
     * Utile pour les tests ou configurations spécifiques.
     *
     * @param reducer le réducteur de contraintes à utiliser
     */
    public ReduceAndCheckIntervalsStrategy(Reducer reducer) {
        this.reducer = reducer;
    }

    /**
     * Constructeur avec reducer et checker fournis.
     *
     * @param reducer le réducteur de contraintes
     * @param checker le vérificateur de contraintes
     */
    public ReduceAndCheckIntervalsStrategy(Reducer reducer, Checker checker) {
        super(checker);
        this.reducer = reducer;
    }

    @Override
    public boolean before(List<Variable> variables, List<Constraint> constraints) {
        // Appliquer la réduction d'intervalles avant la recherche
        if (reducer != null) {
            reducer.reduce();
            return reducer.isModified(); // Indiquer si des modifications ont été faites
        } else {
            // Fallback: créer un reducer temporaire si nécessaire
            Reducer tempReducer = new Reducer(constraints, variables);
            tempReducer.reduce();
            return tempReducer.isModified();
        }
    }
}

