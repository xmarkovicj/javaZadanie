package com.markovic.javazadanie.fx;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class DashboardController {

    @FXML
    private TableView<StudyGroupItem> groupsTable;

    @FXML
    private TableColumn<StudyGroupItem, Number> colId;

    @FXML
    private TableColumn<StudyGroupItem, String> colName;

    @FXML
    private TableColumn<StudyGroupItem, String> colDescription;

    @FXML
    private Label statusLabel;

    private final DashboardApiClient apiClient = new DashboardApiClient();
    private final ObservableList<StudyGroupItem> groups = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        statusLabel.setText("");

        // nastavenie stĺpcov
        colId.setCellValueFactory(cellData -> new javafx.beans.property.SimpleLongProperty(
                cellData.getValue().getId() != null ? cellData.getValue().getId() : 0L
        ));
        colName.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getName()
        ));
        colDescription.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getDescription()
        ));

        groupsTable.setItems(groups);

        // načítaj hneď po štarte
        loadGroups();
    }

    @FXML
    private void onRefresh() {
        loadGroups();
    }

    @FXML
    private void onNewGroup() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("New Group");
        dialog.setHeaderText("Create new study group");
        dialog.setContentText("Group name:");

        dialog.showAndWait().ifPresent(name -> {
            if (name.trim().isEmpty()) {
                statusLabel.setText("Name cannot be empty");
                return;
            }

            TextInputDialog descDialog = new TextInputDialog();
            descDialog.setTitle("New Group");
            descDialog.setHeaderText("Description");
            descDialog.setContentText("Description:");

            descDialog.showAndWait().ifPresent(desc -> {
                createGroup(name, desc);
            });
        });
    }

    private void loadGroups() {
        statusLabel.setText("Loading groups...");
        groups.clear();

        String token = LoginController.getJwtToken();
        if (token == null || token.isEmpty()) {
            statusLabel.setText("No JWT token. Please login again.");
            return;
        }

        new Thread(() -> {
            try {
                var list = apiClient.getGroups(token);
                Platform.runLater(() -> {
                    groups.setAll(list);
                    statusLabel.setStyle("-fx-text-fill: green;");
                    statusLabel.setText("Loaded " + list.size() + " groups.");
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    statusLabel.setStyle("-fx-text-fill: red;");
                    statusLabel.setText("Failed to load groups: " + e.getMessage());
                });
            }
        }).start();
    }

    private void createGroup(String name, String description) {
        statusLabel.setText("Creating group...");

        String token = LoginController.getJwtToken();
        if (token == null || token.isEmpty()) {
            statusLabel.setText("No JWT token. Please login again.");
            return;
        }

        new Thread(() -> {
            try {
                StudyGroupItem created = apiClient.createGroup(token, name, description);
                Platform.runLater(() -> {
                    groups.add(created);
                    statusLabel.setStyle("-fx-text-fill: green;");
                    statusLabel.setText("Group created: " + created.getName());
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    statusLabel.setStyle("-fx-text-fill: red;");
                    statusLabel.setText("Failed to create group: " + e.getMessage());
                });
            }
        }).start();
    }
}
