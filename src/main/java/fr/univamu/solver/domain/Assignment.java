package fr.univamu.solver.domain;

/**
 * Record représentant une affectation de variable dans une solution.
 * Une affectation associe un nom de variable à sa valeur entière.
 * 
 * @param variableName le nom de la variable
 * @param value la valeur assignée à la variable
 */
public record Assignment(String variableName, int value) {
    
    /**
     * Représentation textuelle de l'affectation.
     * Format: "variableName = value"
     */
    @Override
    public String toString() {
        return String.format("%s = %d", variableName, value);
    }
}

