package com.example.planning;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Prototype d'application JavaFX illustrant comment utiliser le solver.
 * Cette classe charge un tableau d'amis/activités et délègue la résolution
 * à {@link SolverBridge}. Le contenu est volontairement minimal pour servir de point de départ.
 */
public class MainApp extends Application {

    private final SolverBridge solverBridge = new SolverBridge();

    @Override
    public void start(Stage stage) {
        stage.setTitle("Planning de soirée – Prototype");

        TableView<AssignmentRow> table = buildTable();
        Button solveButton = new Button("Planifier la soirée");
        Label resultLabel = new Label("Préférences non résolues");

        solveButton.setOnAction(event -> {
            var solution = solverBridge.solveDemoProblem();
            if (solution.isEmpty()) {
                resultLabel.setText("Aucune solution trouvée");
            } else {
                resultLabel.setText("Solution optimale : " + solution.get());
            }
        });

        VBox controls = new VBox(12, solveButton, resultLabel);
        controls.setPadding(new Insets(16));

        BorderPane root = new BorderPane();
        root.setCenter(table);
        root.setRight(controls);
        root.setPadding(new Insets(16));

        stage.setScene(new Scene(root, 820, 420));
        stage.show();
    }

    private TableView<AssignmentRow> buildTable() {
        TableView<AssignmentRow> table = new TableView<>();
        table.setPlaceholder(new Label("Configurer les préférences ici"));

        TableColumn<AssignmentRow, String> friendCol = new TableColumn<>("Ami");
        friendCol.setCellValueFactory(new PropertyValueFactory<>("friend"));

        TableColumn<AssignmentRow, String> activityCol = new TableColumn<>("Activité suggérée");
        activityCol.setCellValueFactory(new PropertyValueFactory<>("activity"));

        table.getColumns().add(friendCol);
        table.getColumns().add(activityCol);

        table.getItems().addAll(
            new AssignmentRow("Alice", "?"),
            new AssignmentRow("Bruno", "?"),
            new AssignmentRow("Chloé", "?"),
            new AssignmentRow("David", "?")
        );

        return table;
    }

    public static void main(String[] args) {
        launch();
    }

    /**
     * Donnée affichée dans la table.
     */
    public static class AssignmentRow {
        private final String friend;
        private final String activity;

        public AssignmentRow(String friend, String activity) {
            this.friend = friend;
            this.activity = activity;
        }

        public String getFriend() {
            return friend;
        }

        public String getActivity() {
            return activity;
        }
    }
}
