package com.markovic.javazadanie.fx;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;

public class DashboardController {

    @FXML
    private BorderPane dashboardRoot;

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
        colId.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleLongProperty(
                        cellData.getValue().getId() != null
                                ? cellData.getValue().getId()
                                : 0L
                )
        );

        colName.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getName() != null
                                ? cellData.getValue().getName()
                                : ""
                )
        );

        colDescription.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getDescription() != null
                                ? cellData.getValue().getDescription()
                                : ""
                )
        );

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
        try {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("New group");
            dialog.setHeaderText("Create new study group");
            dialog.setContentText("Group name:");

            var nameOpt = dialog.showAndWait();
            if (nameOpt.isEmpty()) {
                return; // user stlačil Cancel
            }

            String name = nameOpt.get().trim();
            if (name.isEmpty()) {
                statusLabel.setStyle("-fx-text-fill: #ff6b6b;");
                statusLabel.setText("Name cannot be empty.");
                return;
            }

            TextInputDialog descDialog = new TextInputDialog();
            descDialog.setTitle("New group");
            descDialog.setHeaderText("Description");
            descDialog.setContentText("Description:");

            var descOpt = descDialog.showAndWait();
            if (descOpt.isEmpty()) {
                return;
            }

            String desc = descOpt.get();

            // reálne vytvorenie skupiny
            createGroup(name, desc);

        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setStyle("-fx-text-fill: #ff6b6b;");
            statusLabel.setText("Failed to open new group dialog: " + e.getMessage());

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Cannot create group");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }


    @FXML
    private void onOpenGroup() {
        StudyGroupItem selected = groupsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusLabel.setStyle("-fx-text-fill: #ff6b6b;");
            statusLabel.setText("Please select a group first.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/group_detail.fxml")
            );
            Parent root = loader.load();

            GroupDetailController controller = loader.getController();
            controller.initData(selected, SessionManager.getInstance().getToken());

            Stage stage = (Stage) groupsTable.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Group detail - " + selected.getName());
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setStyle("-fx-text-fill: #ff6b6b;");
            statusLabel.setText("Failed to open group: " + e.getMessage());
        }
    }

    private void loadGroups() {
        statusLabel.setStyle("-fx-text-fill: #cccccc;");
        statusLabel.setText("Loading groups...");
        groups.clear();

        String token = SessionManager.getInstance().getToken();

        if (token == null || token.isEmpty()) {
            statusLabel.setStyle("-fx-text-fill: #ff6b6b;");
            statusLabel.setText("No JWT token. Please login again.");
            return;
        }

        new Thread(() -> {
            try {
                var list = apiClient.getGroups(token);
                Platform.runLater(() -> {
                    groups.setAll(list);
                    statusLabel.setStyle("-fx-text-fill: #4caf50;");
                    statusLabel.setText("Loaded " + list.size() + " groups.");
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    statusLabel.setStyle("-fx-text-fill: #ff6b6b;");
                    statusLabel.setText("Failed to load groups: " + e.getMessage());
                });
            }
        }).start();
    }

    private void createGroup(String name, String description) {
        statusLabel.setStyle("-fx-text-fill: #cccccc;");
        statusLabel.setText("Creating group...");

        String token = SessionManager.getInstance().getToken();

        if (token == null || token.isEmpty()) {
            statusLabel.setStyle("-fx-text-fill: #ff6b6b;");
            statusLabel.setText("No JWT token. Please login again.");
            return;
        }

        new Thread(() -> {
            try {
                StudyGroupItem created = apiClient.createGroup(token, name, description);
                Platform.runLater(() -> {
                    groups.add(created);
                    statusLabel.setStyle("-fx-text-fill: #4caf50;");
                    statusLabel.setText("Group created: " + created.getName());
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    statusLabel.setStyle("-fx-text-fill: #ff6b6b;");
                    statusLabel.setText("Failed to create group: " + e.getMessage());
                });
            }
        }).start();
    }

    @FXML
    private void openLogs() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/logs.fxml")
            );
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("System logs");
            stage.setScene(new Scene(root));

            // bezpečne nastav ownera, ak je k dispozícii
            if (dashboardRoot != null && dashboardRoot.getScene() != null) {
                Stage owner = (Stage) dashboardRoot.getScene().getWindow();
                stage.initOwner(owner);
            }

            stage.show();
            statusLabel.setStyle("-fx-text-fill: #4caf50;");
            statusLabel.setText("Logs window opened.");
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setStyle("-fx-text-fill: #ff6b6b;");
            statusLabel.setText("Failed to open logs: " + e.getMessage());

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Cannot open logs");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }


    @FXML
    private void openUsers() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/users.fxml")
            );
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Users");
            stage.setScene(new Scene(root));
            stage.initOwner(dashboardRoot.getScene().getWindow());
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
