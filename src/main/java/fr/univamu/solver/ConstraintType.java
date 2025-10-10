package fr.univamu.solver;

/**
 * Enum représentant les types de contraintes supportés par le solveur.
 * Remplace l'ancien système de char pour une meilleure type safety.
 */
public enum ConstraintType {
    /**
     * Contrainte d'addition: result = var1 + var2
     */
    ADD,
    
    /**
     * Contrainte de multiplication: result = var1 * var2
     */
    MUL,
    
    /**
     * Contrainte de division: result = var1 / var2
     */
    DIV,
    
    /**
     * Contrainte de différence (all-different): var1 != var2
     */
    DIFF
}
