package fr.univamu.solver;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.function.Consumer;
import java.util.ArrayList;

/**
 * Classe pour gérer les solutions trouvées par le solveur.
 * Utilise un système de callback pour traiter les solutions au fur et à mesure
 * de leur découverte, évitant ainsi de stocker toutes les solutions en mémoire.
 */
public class Solutions {
    
    private long count = 0;
    private Consumer<Map<String, Integer>> onSolutionFound = null;
    private boolean displaySolutions = true;
    
    /**
     * Ajoute une solution trouvée.
     * 
     * @param variables la liste des variables avec leurs valeurs finales
     */
    public void addSolution(List<Variable> variables) {
        count++;
        
        if (displaySolutions) {
            // Affichage simple sur la sortie standard
            variables.stream()
                .filter(Variable::isNamed)
                .forEach(v -> System.out.println(v));
            System.out.println();
        }
        
        if (onSolutionFound != null) {
            // Créer un map pour le callback
            Map<String, Integer> solution = new HashMap<>();
            variables.stream()
                .filter(Variable::isNamed)
                .forEach(v -> solution.put(v.getName(), v.getFixedValue()));
            
            onSolutionFound.accept(solution);
        }
    }
    
    /**
     * Retourne le nombre de solutions trouvées.
     * 
     * @return le nombre de solutions
     */
    public long getCount() {
        return count;
    }
    
    /**
     * Définit un callback qui sera appelé pour chaque solution trouvée.
     * 
     * @param callback le callback à appeler (null pour désactiver)
     */
    public void setOnSolutionFound(Consumer<Map<String, Integer>> callback) {
        this.onSolutionFound = callback;
    }
    
    /**
     * Active ou désactive l'affichage des solutions sur la sortie standard.
     * 
     * @param display true pour afficher, false pour masquer
     */
    public void setDisplaySolutions(boolean display) {
        this.displaySolutions = display;
    }
    
    /**
     * Remet le compteur à zéro.
     */
    public void reset() {
        this.count = 0;
    }
}
