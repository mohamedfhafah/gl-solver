package fr.univamu.solver.strategy;

import fr.univamu.solver.domain.Constraint;
import fr.univamu.solver.domain.Variable;
import fr.univamu.solver.engine.Checker;
import fr.univamu.solver.engine.Reducer;

import java.util.List;

/**
 * Stratégie qui applique systématiquement la réduction des domaines
 * à chaque étape de la recherche. Elle s'appuie sur {@link Backup}
 * pour restaurer l'état précédent lors du retour arrière.
 */
public class AlwaysReduceStrategy extends ReduceAndCheckIntervalsStrategy {

    private final Reducer reducer;
    private final List<Variable> variables;
    private final Backup backup = new Backup();

    /**
     * @param reducer   réducteur partagé du solveur
     * @param checker   vérificateur partagé du solveur
     * @param variables liste des variables à sauvegarder/restaurer
     */
    public AlwaysReduceStrategy(Reducer reducer, Checker checker, List<Variable> variables) {
        super(reducer, checker);
        this.reducer = reducer;
        this.variables = variables;
    }

    @Override
    public boolean before(List<Variable> variables, List<Constraint> constraints) {
        // Propagation initiale via ReduceAndCheckIntervalsStrategy
        return super.before(variables, constraints);
    }

    @Override
    public boolean check(List<Constraint> constraints, List<Variable> variables) {
        reducer.reduce();
        // Si une variable est vide, la branche est inconsistante
        for (Variable variable : variables) {
            if (variable.isEmpty()) {
                return false;
            }
        }
        return super.check(constraints, variables);
    }

    @Override
    public void backup() {
        backup.save(variables);
    }

    @Override
    public void restore() {
        backup.restore();
    }
}
