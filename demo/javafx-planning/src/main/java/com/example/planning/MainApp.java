package com.example.planning;

import javafx.application.Application;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.converter.IntegerStringConverter;

import java.util.ArrayList;
import java.util.List;

public class MainApp extends Application {

    private final SolverBridge solverBridge = new SolverBridge();
    private ObservableList<PreferenceRow> rows;
    private List<String> activities;

    @Override
    public void start(Stage stage) {
        stage.setTitle("Planning de soirée – Prototype");

        PreferenceDataset dataset = solverBridge.getDataset();
        activities = dataset.activities();
        rows = FXCollections.observableArrayList();
        dataset.friends().forEach(entry -> rows.add(new PreferenceRow(entry.name(), activities, entry.preferences())));

        TableView<PreferenceRow> table = buildTable();
        ListView<String> output = new ListView<>();
        output.setPlaceholder(new Label("Aucune solution pour le moment"));

        Button solveButton = new Button("Planifier la soirée");
        Button revertButton = new Button("Réinitialiser");
        Label statusLabel = new Label("En attente...");

        solveButton.setOnAction(event -> {
            statusLabel.setText("Calcul en cours...");
            var friends = rows.stream().map(PreferenceRow::getFriend).toList();
            int[][] costs = new int[friends.size()][activities.size()];
            for (int f = 0; f < friends.size(); f++) {
                for (int a = 0; a < activities.size(); a++) {
                    costs[f][a] = rows.get(f).valueProperty(activities.get(a)).get();
                }
            }

            solverBridge.solve(friends, activities, costs).ifPresentOrElse(result -> {
                output.getItems().clear();
                int index = 1;
                for (PlannerResult.PlannerSolution solution : result.solutions()) {
                    StringBuilder builder = new StringBuilder();
                    builder.append("Solution ").append(index++).append(" (coût = ")
                        .append(solution.cost()).append(")\n");
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

        revertButton.setOnAction(event -> {
            rows.clear();
            solverBridge.getDataset().friends().forEach(entry -> rows.add(new PreferenceRow(entry.name(), activities, entry.preferences())));
        });

        table.setItems(rows);

        HBox buttons = new HBox(12, solveButton, revertButton);
        VBox controls = new VBox(12, buttons, statusLabel, output);
        controls.setPadding(new Insets(16));

        BorderPane root = new BorderPane();
        root.setCenter(table);
        root.setRight(controls);
        root.setPadding(new Insets(16));

        stage.setScene(new Scene(root, 940, 520));
        stage.show();
    }

    private TableView<PreferenceRow> buildTable() {
        TableView<PreferenceRow> table = new TableView<>();
        table.setEditable(true);

        TableColumn<PreferenceRow, String> friendCol = new TableColumn<>("Ami");
        friendCol.setCellValueFactory(new PropertyValueFactory<>("friend"));
        friendCol.setPrefWidth(120);
        table.getColumns().add(friendCol);

        for (String activity : activities) {
            TableColumn<PreferenceRow, Number> column = new TableColumn<>(activity);
            column.setCellValueFactory(cellData -> cellData.getValue().valueProperty(activity));
            column.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
            column.setOnEditCommit(event -> {
                int newValue = event.getNewValue() != null ? event.getNewValue().intValue() : event.getOldValue().intValue();
                event.getRowValue().valueProperty(activity).set(newValue);
            });
            column.setPrefWidth(120);
            table.getColumns().add(column);
        }

        return table;
    }

    public static void main(String[] args) {
        launch();
    }

    public static class PreferenceRow {
        private final StringProperty friend = new SimpleStringProperty();
        private final List<String> activities;
        private final List<IntegerProperty> values;

        public PreferenceRow(String friendName, List<String> activities, java.util.Map<String, Integer> preferences) {
            this.friend.set(friendName);
            this.activities = activities;
            this.values = new ArrayList<>();
            for (String activity : activities) {
                int value = preferences.getOrDefault(activity, 5);
                this.values.add(new SimpleIntegerProperty(value));
            }
        }

        public String getFriend() {
            return friend.get();
        }

        public StringProperty friendProperty() {
            return friend;
        }

        public IntegerProperty valueProperty(String activity) {
            int index = activities.indexOf(activity);
            return values.get(index);
        }
    }
}
