package com.markovic.javazadanie.fx;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.scene.Scene;

import java.io.IOException;


public class DashboardController {

    @FXML
    private TableView<StudyGroupItem> groupsTable;

    @FXML
    private BorderPane dashboardRoot;

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
    @FXML
    private void onOpenGroup() {
        StudyGroupItem selected = groupsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusLabel.setStyle("-fx-text-fill: red;");
            statusLabel.setText("Please select a group first.");
            return;
        }

        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/fxml/group_detail.fxml")
            );
            javafx.scene.Parent root = loader.load();

            GroupDetailController controller = loader.getController();
            controller.initData(selected, LoginController.getJwtToken());

            javafx.stage.Stage stage = (javafx.stage.Stage) groupsTable.getScene().getWindow();
            stage.setScene(new javafx.scene.Scene(root));
            stage.setTitle("Group detail - " + selected.getName());
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setStyle("-fx-text-fill: red;");
            statusLabel.setText("Failed to open group: " + e.getMessage());
        }
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
    @FXML
    private void openLogs() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/fxml/logs.fxml")
            );
            javafx.scene.Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Systémové logy");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void openUsers() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/users.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Users");
            stage.setScene(new Scene(root));
            stage.initOwner(dashboardRoot.getScene().getWindow()); // ak máš root pre dashboard
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            // prípadne nejaký alert
        }
    }


}
