package fr.univamu.solver.engine;

import fr.univamu.solver.domain.Assignment;
import fr.univamu.solver.domain.Variable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Gestionnaire des solutions produites par le solveur.
 * Permet de compter, mémoriser et éventuellement afficher les solutions.
 */
public class Solutions {

    private long count = 0;
    private final List<List<Assignment>> storedSolutions = new ArrayList<>();
    private Consumer<Map<String, Integer>> onSolutionFound = null;
    private boolean displaySolutions = true;
    private Consumer<List<Assignment>> onSolutionSnapshot = null;

    /**
     * Ajoute une solution trouvée par le solveur.
     *
     * @param variables la liste des variables avec leurs domaines réduits à une valeur
     */
    public void addSolution(List<Variable> variables) {
        count++;

        List<Assignment> snapshot = variables.stream()
            .filter(Variable::isNamed)
            .map(v -> new Assignment(v.getName(), v.getFixedValue()))
            .toList();

        if (onSolutionSnapshot != null) {
            onSolutionSnapshot.accept(List.copyOf(snapshot));
        }
        storedSolutions.add(snapshot);

        if (displaySolutions) {
            snapshot.forEach(assignment -> System.out.println(assignment));
            System.out.println();
        }

        if (onSolutionFound != null) {
            Map<String, Integer> solutionMap = new HashMap<>();
            snapshot.forEach(a -> solutionMap.put(a.variableName(), a.value()));
            onSolutionFound.accept(solutionMap);
        }
    }

    /**
     * Retourne le nombre de solutions trouvées depuis le dernier reset.
     */
    public long getCount() {
        return count;
    }

    /**
     * Retourne une copie non modifiable des solutions mémorisées.
     */
    public List<List<Assignment>> getStoredSolutions() {
        return Collections.unmodifiableList(storedSolutions.stream()
            .map(List::copyOf)
            .toList());
    }

    /**
     * Définit un callback exécuté à chaque solution trouvée.
     *
     * @param callback fonction invoquée avec la solution représentée sous forme de Map
     */
    public void setOnSolutionFound(Consumer<Map<String, Integer>> callback) {
        this.onSolutionFound = callback;
    }

    /**
     * Définit un callback exécuté à chaque solution trouvée avec la liste complète des affectations.
     *
     * @param callback fonction invoquée avec les affectations de la solution (liste immuable)
     */
    public void setOnSolutionSnapshot(Consumer<List<Assignment>> callback) {
        this.onSolutionSnapshot = callback;
    }

    /**
     * Active ou désactive l'affichage des solutions.
     */
    public void setDisplaySolutions(boolean display) {
        this.displaySolutions = display;
    }

    /**
     * Vide l'historique et remet le compteur à zéro.
     */
    public void reset() {
        this.count = 0;
        this.storedSolutions.clear();
    }

    /**
     * Remplace les solutions stockées par une nouvelle collection (typiquement issue d'une optimisation).
     *
     * @param newSolutions nouvelles solutions à conserver
     */
    public void replaceStoredSolutions(List<List<Assignment>> newSolutions) {
        this.storedSolutions.clear();
        for (List<Assignment> solution : newSolutions) {
            this.storedSolutions.add(List.copyOf(solution));
        }
        this.count = this.storedSolutions.size();
    }
}
