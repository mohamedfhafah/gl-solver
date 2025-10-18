package com.example.planning;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MainApp extends Application {

    private final SolverBridge solverBridge = new SolverBridge();

    @Override
    public void start(Stage stage) {
        stage.setTitle("Planning de soirée – Prototype");

        ListView<String> output = new ListView<>();
        output.setPlaceholder(new Label("Aucune solution pour le moment"));

        Button solveButton = new Button("Planifier la soirée");
        Label statusLabel = new Label("En attente...");

        solveButton.setOnAction(event -> {
            statusLabel.setText("Calcul en cours...");
            solverBridge.solveDemoProblem().ifPresentOrElse(result -> {
                output.getItems().clear();
                int index = 1;
                for (PlannerResult.PlannerSolution solution : result.solutions()) {
                    StringBuilder builder = new StringBuilder();
                    builder.append("Solution ").append(index++).append(" (coût = ")
                        .append(solution.cost()).append(")");
                    builder.append("\n");
                    solution.assignment().forEach((friend, activity) ->
                        builder.append("  • ").append(friend).append(" → ").append(activity).append("\n"));
                    output.getItems().add(builder.toString());
                }
                statusLabel.setText("Solutions optimales affichées");
            }, () -> {
                output.getItems().clear();
                statusLabel.setText("Aucune solution trouvée");
            });
        });

        VBox controls = new VBox(12, solveButton, statusLabel, output);
        controls.setPadding(new Insets(16));

        BorderPane root = new BorderPane();
        root.setCenter(new Label("Prototype : les préférences proviennent de preferences.json"));
        root.setBottom(controls);
        root.setPadding(new Insets(16));

        stage.setScene(new Scene(root, 640, 420));
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
