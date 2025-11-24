package com.markovic.javazadanie.fx;

import com.markovic.javazadanie.fx.dto.ActionLogDto;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.util.List;

public class LogsController {

    @FXML
    private TableView<ActionLogDto> logsTable;

    @FXML
    private TableColumn<ActionLogDto, String> colTime;

    @FXML
    private TableColumn<ActionLogDto, String> colUser;

    @FXML
    private TableColumn<ActionLogDto, String> colType;

    @FXML
    private TableColumn<ActionLogDto, String> colDescription;

    @FXML
    private Label statusLabel;

    @FXML
    private TextField searchField;

    @FXML
    private ComboBox<String> actionFilter;

    private final LogApiClient logApiClient = new LogApiClient();

    // master data zoznam
    private final ObservableList<ActionLogDto> masterLogs = FXCollections.observableArrayList();
    private FilteredList<ActionLogDto> filteredLogs;

    @FXML
    public void initialize() {
        // väzby na properties z ActionLogDto
        colTime.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        colType.setCellValueFactory(new PropertyValueFactory<>("actionType"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));

        colUser.setCellValueFactory(cellData -> {
            ActionLogDto log = cellData.getValue();
            String email = "";
            if (log.getUser() != null && log.getUser().getEmail() != null) {
                email = log.getUser().getEmail();
            }
            return new SimpleStringProperty(email);
        });

        // FilteredList nad masterLogs
        filteredLogs = new FilteredList<>(masterLogs, log -> true);
        logsTable.setItems(filteredLogs);

        // naplniť actionFilter
        if (actionFilter != null) {
            actionFilter.getItems().setAll(
                    "ALL",
                    "USER_REGISTERED",
                    "USER_LOGIN_SUCCESS",
                    "TASK_CREATED",
                    "TASK_SUBMITTED"
            );
            actionFilter.getSelectionModel().selectFirst();

            actionFilter.getSelectionModel()
                    .selectedItemProperty()
                    .addListener((obs, oldVal, newVal) -> applyFilters());
        }

        // listener na fulltext search
        if (searchField != null) {
            searchField.textProperty()
                    .addListener((obs, oldVal, newVal) -> applyFilters());
        }

        loadLogs();
    }

    private void applyFilters() {
        String text = searchField != null ? searchField.getText() : "";
        String selectedAction = actionFilter != null
                ? actionFilter.getSelectionModel().getSelectedItem()
                : "ALL";

        String search = text == null ? "" : text.trim().toLowerCase();
        boolean filterAllActions = (selectedAction == null
                || selectedAction.isBlank()
                || selectedAction.equalsIgnoreCase("ALL"));

        filteredLogs.setPredicate(log -> {
            if (log == null) return false;

            // filter podľa typu akcie
            if (!filterAllActions) {
                if (log.getActionType() == null
                        || !log.getActionType().equalsIgnoreCase(selectedAction)) {
                    return false;
                }
            }

            // fulltext filter
            if (search.isEmpty()) {
                return true;
            }

            String action = log.getActionType() != null ? log.getActionType().toLowerCase() : "";
            String desc = log.getDescription() != null ? log.getDescription().toLowerCase() : "";
            String email = "";
            String name = "";

            if (log.getUser() != null) {
                if (log.getUser().getEmail() != null) {
                    email = log.getUser().getEmail().toLowerCase();
                }
                if (log.getUser().getName() != null) {
                    name = log.getUser().getName().toLowerCase();
                }
            }

            return action.contains(search)
                    || desc.contains(search)
                    || email.contains(search)
                    || name.contains(search);
        });

        statusLabel.setText(filteredLogs.size() + " logov zobrazených (z " + masterLogs.size() + ")");
    }

    private void loadLogs() {
        statusLabel.setText("Načítavam logy...");
        masterLogs.clear();

        new Thread(() -> {
            try {
                String token = SessionManager.getInstance().getToken();
                List<ActionLogDto> logs = logApiClient.getLogs(token);

                Platform.runLater(() -> {
                    masterLogs.setAll(logs);   // naplníme master list
                    applyFilters();            // hneď aplikujeme filtre
                    statusLabel.setText(masterLogs.size() + " logov načítaných");
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() ->
                        statusLabel.setText("Nepodarilo sa načítať logy: " + e.getMessage())
                );
            }
        }).start();
    }

    @FXML
    private void onRefresh() {
        loadLogs();
    }

    @FXML
    private void onClose() {
        Stage stage = (Stage) logsTable.getScene().getWindow();
        stage.close();
    }
}
