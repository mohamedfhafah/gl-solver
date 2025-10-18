package com.example.planning;

import javafx.application.Application;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.converter.IntegerStringConverter;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MainApp extends Application {

    private final SolverBridge solverBridge = new SolverBridge();
    private ObservableList<PreferenceRow> rows;
    private List<String> activities;
    private List<PlannerResult.PlannerSolution> currentSolutions = new ArrayList<>();
    private int currentIndex = 0;
    private Mode currentMode = Mode.PERSONALIZED;
    private Label subtitle;

    @Override
    public void start(Stage stage) {
        stage.setTitle("Planificateur de soirée intelligent");

        PreferenceDataset dataset = solverBridge.getDataset();
        activities = dataset.activities();
        rows = FXCollections.observableArrayList();
        dataset.friends().forEach(entry -> rows.add(new PreferenceRow(entry.name(), activities, entry.preferences())));

        Label header = new Label("Planificateur intelligent de soirées");
        header.getStyleClass().add("title");

        subtitle = new Label();
        subtitle.getStyleClass().add("subtitle");
        updateSubtitle();

        ToggleButton personalBtn = new ToggleButton("Planning individuel");
        ToggleButton groupBtn = new ToggleButton("Activité commune");
        ToggleGroup modeGroup = new ToggleGroup();
        personalBtn.setToggleGroup(modeGroup);
        groupBtn.setToggleGroup(modeGroup);
        personalBtn.setSelected(true);
        personalBtn.getStyleClass().add("mode-toggle");
        groupBtn.getStyleClass().add("mode-toggle");
        HBox modeSwitch = new HBox(10, personalBtn, groupBtn);
        modeSwitch.getStyleClass().add("mode-switch");

        modeGroup.selectedToggleProperty().addListener((obs, old, selected) -> {
            if (selected == null) {
                personalBtn.setSelected(true);
            }
            if (modeGroup.getSelectedToggle() == groupBtn) {
                currentMode = Mode.GROUP;
            } else {
                currentMode = Mode.PERSONALIZED;
            }
            updateSubtitle();
        });

        TableView<PreferenceRow> table = buildTable();
        table.setPrefHeight(320);

        ListView<String> output = new ListView<>();
        output.setPlaceholder(new Label("Aucune solution pour le moment"));
        output.getStyleClass().add("solution-view");

        Button solveButton = new Button("Planifier la soirée");
        Button revertButton = new Button("Réinitialiser");
        Button nextButton = new Button("Solution suivante");
        nextButton.setDisable(true);
        Label statusLabel = new Label("En attente...");
        statusLabel.getStyleClass().add("status");

        nextButton.setOnAction(event -> {
            if (!currentSolutions.isEmpty()) {
                currentIndex = (currentIndex + 1) % currentSolutions.size();
                displaySolution(output, statusLabel);
            }
        });

        solveButton.setOnAction(event -> {
            statusLabel.setText("Calcul en cours...");
            var friends = rows.stream().map(PreferenceRow::getFriend).toList();
            int[][] costs = new int[friends.size()][activities.size()];
            for (int f = 0; f < friends.size(); f++) {
                for (int a = 0; a < activities.size(); a++) {
                    costs[f][a] = rows.get(f).valueProperty(activities.get(a)).get();
                }
            }

            Optional<PlannerResult> resultOpt = currentMode == Mode.PERSONALIZED
                ? solverBridge.solve(friends, activities, costs)
                : solverBridge.solveGroup(friends, activities, costs);

            resultOpt.ifPresentOrElse(planner -> {
                currentSolutions = planner.solutions();
                currentIndex = 0;
                nextButton.setDisable(currentSolutions.size() <= 1);
                displaySolution(output, statusLabel);
            }, () -> {
                output.getItems().clear();
                statusLabel.setText("Aucune solution trouvée");
                nextButton.setDisable(true);
            });
        });

        revertButton.setOnAction(event -> {
            rows.clear();
            solverBridge.getDataset().friends().forEach(entry -> rows.add(new PreferenceRow(entry.name(), activities, entry.preferences())));
        });

        table.setItems(rows);

        HBox buttons = new HBox(12, solveButton, revertButton, nextButton);
        buttons.getStyleClass().add("button-bar");

        VBox legend = new VBox(6,
            new Label("Échelle de préférence"),
            createLegendRow("1", "Activité préférée"),
            createLegendRow("5", "Neutralité"),
            createLegendRow("10", "À éviter"));
        legend.getStyleClass().add("legend");

        VBox leftCard = new VBox(18, header, modeSwitch, subtitle, legend, table);
        leftCard.getStyleClass().add("card");

        VBox controls = new VBox(12, buttons, statusLabel, output);
        controls.setPadding(new Insets(16));
        controls.getStyleClass().add("card");

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(24));
        root.setLeft(leftCard);
        BorderPane.setMargin(leftCard, new Insets(0, 18, 0, 0));
        root.setCenter(controls);

        Scene scene = new Scene(root, 980, 560);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        stage.setScene(scene);
        stage.show();
    }

    private HBox createLegendRow(String label, String description) {
        Label chip = new Label(label);
        chip.getStyleClass().add("legend-chip");
        Label text = new Label(description);
        HBox row = new HBox(8, chip, text);
        row.getStyleClass().add("legend-row");
        return row;
    }

    private void displaySolution(ListView<String> output, Label statusLabel) {
        if (currentSolutions.isEmpty()) {
            output.getItems().clear();
            statusLabel.setText("Aucune solution trouvée");
            return;
        }
        PlannerResult.PlannerSolution solution = currentSolutions.get(currentIndex);
        StringBuilder builder = new StringBuilder();
        builder.append("Solution ").append(currentIndex + 1).append(" / ")
            .append(currentSolutions.size()).append("\n")
            .append(solution.description()).append("\n")
            .append("Coût total : ").append(solution.cost())
            .append(" — Nœuds explorés : ").append(solution.exploredNodes())
            .append("\n\n");
        solution.assignment().forEach((friend, activity) ->
            builder.append("  • ").append(friend).append(" → ").append(activity).append("\n"));

        output.getItems().setAll(builder.toString());
        statusLabel.setText("Solution affichée : " + (currentIndex + 1) + " / " + currentSolutions.size());
        output.getSelectionModel().select(0);
        FadeTransition ft = new FadeTransition(Duration.millis(350), output);
        ft.setFromValue(0.0);
        ft.setToValue(1.0);
        ft.playFromStart();
    }

    private TableView<PreferenceRow> buildTable() {
        TableView<PreferenceRow> table = new TableView<>();
        table.setEditable(true);

        TableColumn<PreferenceRow, String> friendCol = new TableColumn<>("Ami");
        friendCol.setCellValueFactory(new PropertyValueFactory<>("friend"));
        friendCol.setPrefWidth(120);
        table.getColumns().add(friendCol);

        for (String activity : activities) {
            TableColumn<PreferenceRow, Integer> column = new TableColumn<>(activity);
            column.setCellValueFactory(cellData -> cellData.getValue().valueProperty(activity).asObject());
            column.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
            column.setOnEditCommit(event -> {
                int newValue = event.getNewValue() != null ? event.getNewValue() : event.getOldValue();
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

    private void updateSubtitle() {
        if (subtitle == null) {
            return;
        }
        if (currentMode == Mode.PERSONALIZED) {
            subtitle.setText("Ajustez les préférences de chacun : 1 = coup de cœur, 10 = à éviter. \nCliquez sur 'Planifier la soirée' pour répartir les activités individuellement.");
        } else {
            subtitle.setText("Mode sortie commune : 1 = activité adorée par tous, 10 = activité à éviter. \nLe solver choisira l'activité unique minimisant la somme des préférences.");
        }
    }

    private enum Mode {
        PERSONALIZED,
        GROUP
    }
}
