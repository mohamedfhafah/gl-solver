package fr.univamu.solver;

/**
 * Représente une contrainte entre variables dans le solveur de contraintes.
 * Cette classe est immuable et thread-safe.
 *
 * @param type Le type d'opération (+, *, /, # pour all-different)
 * @param result La variable résultat de l'opération
 * @param var1 La première opérande
 * @param var2 La deuxième opérande (peut être null pour les contraintes #)
 */
public record Constraint(
    char type,
    Variable result,
    Variable var1,
    Variable var2
) {
    /**
     * Représentation textuelle de la contrainte.
     * Format: TYPE(RESULT,VAR1,VAR2)
     */
    @Override
    public String toString() {
        return String.format("%s(%s,%s,%s)", type, result, var1, var2);
    }
}
