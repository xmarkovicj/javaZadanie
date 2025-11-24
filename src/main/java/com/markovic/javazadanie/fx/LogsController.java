package com.markovic.javazadanie.fx;

import com.markovic.javazadanie.fx.dto.ActionLogDto;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;

public class LogsController {

    @FXML
    private TableView<ActionLogDto> logsTable;

    @FXML
    private TableColumn<ActionLogDto, String> actionTypeColumn;

    @FXML
    private TableColumn<ActionLogDto, String> userColumn;

    @FXML
    private TableColumn<ActionLogDto, String> createdAtColumn;

    @FXML
    private TableColumn<ActionLogDto, String> descriptionColumn;

    @FXML
    private Label statusLabel;

    @FXML
    private TextField searchField;

    @FXML
    private ComboBox<String> actionFilter;

    private final LogApiClient logApiClient = new LogApiClient();
    private FilteredList<ActionLogDto> filteredLogs;

    @FXML
    public void initialize() {
        // stĺpce
        actionTypeColumn.setCellValueFactory(new PropertyValueFactory<>("actionType"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        createdAtColumn.setCellValueFactory(new PropertyValueFactory<>("createdAt"));

        userColumn.setCellValueFactory(cellData -> {
            var log = cellData.getValue();
            String email = "";
            if (log.getUser() != null && log.getUser().getEmail() != null) {
                email = log.getUser().getEmail();
            }
            return new SimpleStringProperty(email);
        });

        // prázdny text v statuse
        if (statusLabel != null) {
            statusLabel.setText("No logs loaded");
        }

        // filter zmeny
        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilter());
        }
        if (actionFilter != null) {
            actionFilter.valueProperty().addListener((obs, oldVal, newVal) -> applyFilter());
        }

        loadLogs();
    }

    private void loadLogs() {
        if (statusLabel != null) {
            statusLabel.setStyle("-fx-text-fill: #9ca3af;"); // sivá
            statusLabel.setText("Loading logs...");
        }

        logsTable.getItems().clear();

        new Thread(() -> {
            try {
                String token = SessionManager.getInstance().getToken();
                List<ActionLogDto> logs = logApiClient.getLogs(token);

                Platform.runLater(() -> {
                    filteredLogs = new FilteredList<>(FXCollections.observableArrayList(logs), p -> true);
                    logsTable.setItems(filteredLogs);

                    if (statusLabel != null) {
                        statusLabel.setStyle("-fx-text-fill: #22c55e;"); // zelená
                        statusLabel.setText(logs.size() + " logs loaded");
                    }

                    // naplniť filter typov
                    if (actionFilter != null) {
                        actionFilter.getItems().clear();
                        actionFilter.getItems().add("All");
                        logs.stream()
                                .map(ActionLogDto::getActionType)
                                .distinct()
                                .sorted()
                                .forEach(actionFilter.getItems()::add);
                        actionFilter.getSelectionModel().selectFirst();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    if (statusLabel != null) {
                        statusLabel.setStyle("-fx-text-fill: #ef4444;"); // červená
                        statusLabel.setText("Failed to load logs: " + e.getMessage());
                    }
                });
            }
        }).start();
    }

    private void applyFilter() {
        if (filteredLogs == null) return;

        String text = searchField != null ? searchField.getText() : "";
        String action = actionFilter != null ? actionFilter.getValue() : "All";

        final String lowerText = text == null ? "" : text.toLowerCase();
        final String actionFilterVal = action == null ? "All" : action;

        filteredLogs.setPredicate(log -> {
            boolean matchesText = lowerText.isEmpty()
                    || (log.getActionType() != null && log.getActionType().toLowerCase().contains(lowerText))
                    || (log.getDescription() != null && log.getDescription().toLowerCase().contains(lowerText))
                    || (log.getUser() != null && log.getUser().getEmail() != null
                    && log.getUser().getEmail().toLowerCase().contains(lowerText));

            boolean matchesAction = "All".equals(actionFilterVal)
                    || (log.getActionType() != null && log.getActionType().equals(actionFilterVal));

            return matchesText && matchesAction;
        });

        if (statusLabel != null) {
            statusLabel.setText(filteredLogs.size() + " logs visible");
        }
    }

    @FXML
    private void onRefresh() {
        loadLogs();
    }

    @FXML
    private void onClose() {
        if (logsTable != null && logsTable.getScene() != null) {
            javafx.stage.Stage stage = (javafx.stage.Stage) logsTable.getScene().getWindow();
            stage.close();
        }
    }
}
