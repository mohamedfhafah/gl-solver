package fr.univamu.solver.strategy;

import fr.univamu.solver.domain.Assignment;
import fr.univamu.solver.domain.Variable;
import fr.univamu.solver.engine.Checker;
import fr.univamu.solver.engine.Reducer;
import fr.univamu.solver.engine.Solutions;

import java.util.ArrayList;
import java.util.List;

/**
 * Stratégie de réduction avec objectif de minimisation.
 * Hérite de {@link AlwaysReduceStrategy} et resserre le domaine de la variable
 * objectif dès qu'une meilleure solution est trouvée afin d'explorer en priorité
 * les valeurs prometteuses.
 */
public class OptimizationStrategy extends AlwaysReduceStrategy {

    private final Variable objective;
    private final String objectiveName;
    private final Reducer reducer;
    private final Solutions solutions;
    private final List<List<Assignment>> bestSolutions = new ArrayList<>();

    private boolean hasBest = false;
    private int bestValue = Integer.MAX_VALUE;

    public OptimizationStrategy(Reducer reducer,
                                Checker checker,
                                List<Variable> variables,
                                Solutions solutions,
                                Variable objective) {
        super(reducer, checker, variables);
        this.reducer = reducer;
        this.solutions = solutions;
        this.objective = objective;
        this.objectiveName = objective.getName();
        this.solutions.setOnSolutionSnapshot(this::processSolutionSnapshot);
    }

    private void processSolutionSnapshot(List<Assignment> assignments) {
        int objectiveValue = extractObjectiveValue(assignments);

        if (!hasBest || objectiveValue < bestValue) {
            hasBest = true;
            bestValue = objectiveValue;
            bestSolutions.clear();
            bestSolutions.add(List.copyOf(assignments));
            applyObjectiveUpperBound();
            reducer.reduce();
        } else if (objectiveValue == bestValue) {
            bestSolutions.add(List.copyOf(assignments));
        }
    }

    private int extractObjectiveValue(List<Assignment> assignments) {
        for (Assignment assignment : assignments) {
            if (assignment.variableName().equals(objectiveName)) {
                return assignment.value();
            }
        }
        throw new IllegalStateException("Objective variable '" + objectiveName + "' not found in solution snapshot");
    }

    private void applyObjectiveUpperBound() {
        if (!hasBest) {
            return;
        }
        objective.reduce(objective.getMin(), bestValue);
    }

    @Override
    public void restore() {
        super.restore();
        applyObjectiveUpperBound();
    }

    /**
     * Prépare la stratégie pour un nouveau lancement de recherche.
     */
    public void prepareForSearch() {
        hasBest = false;
        bestValue = Integer.MAX_VALUE;
        bestSolutions.clear();
        solutions.setOnSolutionSnapshot(this::processSolutionSnapshot);
    }

    /**
     * Injecte les meilleures solutions collectées dans l'objet {@link Solutions}
     * afin que le solveur expose uniquement les solutions optimales.
     */
    public void applyBestSolutions() {
        solutions.replaceStoredSolutions(bestSolutions);
        solutions.setOnSolutionSnapshot(null);
    }
}
