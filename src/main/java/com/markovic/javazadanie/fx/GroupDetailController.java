package com.markovic.javazadanie.fx;

import com.markovic.javazadanie.fx.dto.GroupDto;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class GroupDetailController {

    @FXML
    private Label groupNameLabel;

    @FXML
    private TableView<TaskItem> tasksTable;

    @FXML
    private TableColumn<TaskItem, Number> colTaskId;

    @FXML
    private TableColumn<TaskItem, String> colTitle;

    @FXML
    private TableColumn<TaskItem, String> colDescription;

    @FXML
    private TableColumn<TaskItem, String> colDueDate;

    @FXML
    private Label statusLabel;

    private final ObservableList<TaskItem> tasks = FXCollections.observableArrayList();
    private final TaskApiClient taskApiClient = new TaskApiClient();
    private final GroupApiClient groupApiClient = new GroupApiClient();
    private GroupDto currentGroup;
    private StudyGroupItem group;
    private String jwtToken;

    public void initData(StudyGroupItem group, String jwtToken) {
        this.group = group;
        this.jwtToken = jwtToken;

        groupNameLabel.setText("Group: " + group.getName());
        loadTasks();
    }

    @FXML
    public void initialize() {
        statusLabel.setText("");

        colTaskId.setCellValueFactory(cell ->
                new javafx.beans.property.SimpleLongProperty(
                        cell.getValue().getId() != null ? cell.getValue().getId() : 0L
                )
        );

        colTitle.setCellValueFactory(cell ->
                new javafx.beans.property.SimpleStringProperty(cell.getValue().getTitle())
        );

        colDescription.setCellValueFactory(cell ->
                new javafx.beans.property.SimpleStringProperty(cell.getValue().getDescription())
        );

        colDueDate.setCellValueFactory(cell ->
                new javafx.beans.property.SimpleStringProperty(cell.getValue().getDueDate())
        );

        tasksTable.setItems(tasks);
    }

    @FXML
    private void onBack() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/dashboard.fxml")
            );
            Parent root = loader.load();

            Stage stage = (Stage) groupNameLabel.getScene().getWindow();
            stage.setScene(new javafx.scene.Scene(root));
            stage.setTitle("Study Platform - Dashboard");
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setStyle("-fx-text-fill: red;");
            statusLabel.setText("Failed to go back: " + e.getMessage());
        }
    }

    @FXML
    private void onRefreshTasks() {
        loadTasks();
    }

    private void loadTasks() {
        if (group == null || jwtToken == null) {
            statusLabel.setStyle("-fx-text-fill: red;");
            statusLabel.setText("Missing group or token.");
            return;
        }

        statusLabel.setText("Loading tasks...");
        tasks.clear();

        new Thread(() -> {
            try {
                var list = taskApiClient.getTasksForGroup(jwtToken, group.getId());
                Platform.runLater(() -> {
                    tasks.setAll(list);
                    statusLabel.setStyle("-fx-text-fill: green;");
                    statusLabel.setText("Loaded " + list.size() + " tasks.");
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    statusLabel.setStyle("-fx-text-fill: red;");
                    statusLabel.setText("Failed to load tasks: " + e.getMessage());
                });
            }
        }).start();
    }

    @FXML
    private void onNewTask() {
        if (group == null || jwtToken == null) {
            statusLabel.setStyle("-fx-text-fill: red;");
            statusLabel.setText("Missing group or token.");
            return;
        }

        TextInputDialog titleDialog = new TextInputDialog();
        titleDialog.setTitle("New Task");
        titleDialog.setHeaderText("Create new task");
        titleDialog.setContentText("Title:");

        titleDialog.showAndWait().ifPresent(title -> {
            if (title.trim().isEmpty()) {
                statusLabel.setStyle("-fx-text-fill: red;");
                statusLabel.setText("Title cannot be empty.");
                return;
            }

            TextInputDialog descDialog = new TextInputDialog();
            descDialog.setTitle("New Task");
            descDialog.setHeaderText("Description");
            descDialog.setContentText("Description:");

            descDialog.showAndWait().ifPresent(desc -> {

                // pre jednoduchosť dáme dueDate = zajtra 23:59
                LocalDateTime due = LocalDate.now()
                        .plusDays(1)
                        .atTime(23, 59);

                String dueStr = due.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

                createTask(title, desc, dueStr);
            });
        });
    }

    private void createTask(String title, String description, String dueDate) {
        statusLabel.setText("Creating task...");

        new Thread(() -> {
            try {
                TaskItem created = taskApiClient.createTask(jwtToken, group.getId(),
                        title, description, dueDate);

                Platform.runLater(() -> {
                    tasks.add(created);
                    statusLabel.setStyle("-fx-text-fill: green;");
                    statusLabel.setText("Task created: " + created.getTitle());
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    statusLabel.setStyle("-fx-text-fill: red;");
                    statusLabel.setText("Failed to create task: " + e.getMessage());
                });
            }
        }).start();
    }
    @FXML
    private void onOpenTask() {
        TaskItem selected = tasksTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusLabel.setStyle("-fx-text-fill: red;");
            statusLabel.setText("Please select a task first.");
            return;
        }

        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/fxml/task_detail.fxml")
            );
            javafx.scene.Parent root = loader.load();

            TaskDetailController controller = loader.getController();
            controller.initData(selected, group, jwtToken);

            javafx.stage.Stage stage = (javafx.stage.Stage) tasksTable.getScene().getWindow();
            stage.setScene(new javafx.scene.Scene(root));
            stage.setTitle("Task detail - " + selected.getTitle());
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setStyle("-fx-text-fill: red;");
            statusLabel.setText("Failed to open task: " + e.getMessage());
        }
    }
    @FXML
    private void onDeleteGroup() {
        if (currentGroup == null) {
            statusLabel.setText("Žiadna skupina nie je vybraná");
            return;
        }

        Long groupId = currentGroup.getId();
        // ak máš field `getGroupId()`, tak použi ten: currentGroup.getGroupId()

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete group");
        confirm.setHeaderText(null);
        confirm.setContentText("Naozaj chceš zmazať skupinu: " + currentGroup.getName() + "?");

        confirm.showAndWait().ifPresent(button -> {
            if (button == ButtonType.OK) {
                new Thread(() -> {
                    try {
                        String token = SessionManager.getInstance().getToken();
                        groupApiClient.deleteGroup(token, groupId);

                        Platform.runLater(() -> {
                            statusLabel.setText("Skupina bola zmazaná");
                            // podľa potreby:
                            // - zavrieť okno
                            // - refreshnúť zoznam skupín inde
                            Stage stage = (Stage) statusLabel.getScene().getWindow();
                            stage.close();
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                        Platform.runLater(() ->
                                statusLabel.setText("Chyba pri mazaní skupiny: " + e.getMessage()));
                    }
                }).start();
            }
        });
    }


}
