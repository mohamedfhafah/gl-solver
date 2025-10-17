package com.example.planning;

import java.util.Optional;

/**
 * Classe d'exemple montrant comment connecter l'application JavaFX au solver GL.
 * Placez le jar compilé du solver dans {@code demo/javafx-planning/libs/gl-solver.jar}
 * avant de lancer le programme.
 */
public class SolverBridge {

    public Optional<String> solveDemoProblem() {
        // TODO: instancier fr.univamu.solver.engine.Solver, définir les variables et contraintes.
        // Exemple : utiliser le problème d'affectation (amis vs activités) présenté dans le plan.
        // Cette implémentation retourne pour l'instant une valeur simulée.
        return Optional.of("Alice -> Cinéma, Bruno -> Escape Game, Chloé -> Restaurant, David -> Karaoké");
    }
}
