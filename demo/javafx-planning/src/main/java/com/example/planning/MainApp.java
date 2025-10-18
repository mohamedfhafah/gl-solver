package com.example.planning;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MainApp extends Application {

    private final SolverBridge solverBridge = new SolverBridge();

    @Override
    public void start(Stage stage) {
        stage.setTitle("Planning de soirée – Prototype");

        TextArea output = new TextArea();
        output.setEditable(false);
        output.setPrefRowCount(10);
        output.setText("Cliquez sur 'Planifier la soirée' pour lancer le solver.");

        Button solveButton = new Button("Planifier la soirée");
        Label statusLabel = new Label("En attente...");

        solveButton.setOnAction(event -> {
            statusLabel.setText("Calcul en cours...");
            solverBridge.solveDemoProblem().ifPresentOrElse(result -> {
                output.setText(result);
                statusLabel.setText("Solution optimale trouvée");
            }, () -> {
                output.setText("Aucune solution trouvée");
                statusLabel.setText("Échec");
            });
        });

        VBox controls = new VBox(12, solveButton, statusLabel, output);
        controls.setPadding(new Insets(16));

        BorderPane root = new BorderPane();
        root.setCenter(new Label("Prototype : les préférences sont codées en dur dans SolverBridge."));
        root.setBottom(controls);
        root.setPadding(new Insets(16));

        stage.setScene(new Scene(root, 640, 360));
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
