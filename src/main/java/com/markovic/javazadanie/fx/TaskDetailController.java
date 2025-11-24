package com.markovic.javazadanie.fx;

import javafx.application.Platform;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TaskDetailController {

    @FXML
    private Label taskTitleLabel;

    @FXML
    private Label taskDescriptionLabel;

    @FXML
    private Label taskDeadlineLabel;

    @FXML
    private Label taskStatusLabel;

    @FXML
    private TableView<TaskSubmissionItem> submissionsTable;

    @FXML
    private TableColumn<TaskSubmissionItem, Number> colSubmissionId;

    @FXML
    private TableColumn<TaskSubmissionItem, String> colUserEmail;

    @FXML
    private TableColumn<TaskSubmissionItem, String> colContent;

    @FXML
    private TableColumn<TaskSubmissionItem, String> colSubmittedAt;

    @FXML
    private TableColumn<TaskSubmissionItem, String> colStatus;

    @FXML
    private TableColumn<TaskSubmissionItem, String> colGrade;

    @FXML
    private Label statusLabel;

    private final TaskSubmissionApiClient submissionApiClient = new TaskSubmissionApiClient();

    private TaskItem task;
    private StudyGroupItem group;
    private String jwtToken;
    private boolean alreadySubmitted = false;

    public void initData(TaskItem task, StudyGroupItem group, String jwtToken) {
        this.task = task;
        this.group = group;
        this.jwtToken = jwtToken;

        taskTitleLabel.setText(task.getTitle());
        taskDescriptionLabel.setText(task.getDescription() != null ? task.getDescription() : "");

        taskDeadlineLabel.setText("Deadline: " + (task.getDueDate() != null ? task.getDueDate() : ""));
        taskStatusLabel.setText("");

        loadSubmissions();
    }

    @FXML
    public void initialize() {
        statusLabel.setText("");

        colSubmissionId.setCellValueFactory(cell ->
                new SimpleLongProperty(cell.getValue().getId() != null ? cell.getValue().getId() : 0L)
        );

        colUserEmail.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getUserEmail())
        );

        colContent.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getContent())
        );

        colSubmittedAt.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getSubmittedAt())
        );

        colStatus.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getStatus())
        );

        colGrade.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getGrade())
        );
    }

    @FXML
    private void onBack() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/group_detail.fxml")
            );
            Parent root = loader.load();

            GroupDetailController controller = loader.getController();
            controller.initData(group, jwtToken);

            Stage stage = (Stage) taskTitleLabel.getScene().getWindow();
            stage.setScene(new javafx.scene.Scene(root));
            stage.setTitle("Group detail - " + group.getName());
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setStyle("-fx-text-fill: red;");
            statusLabel.setText("Failed to go back: " + e.getMessage());
        }
    }

    @FXML
    private void onRefreshSubmissions() {
        loadSubmissions();
    }

    private void loadSubmissions() {
        if (task == null || jwtToken == null) {
            statusLabel.setStyle("-fx-text-fill: red;");
            statusLabel.setText("Missing task or token.");
            return;
        }

        statusLabel.setText("Loading submissions...");
        submissionsTable.getItems().clear();

        new Thread(() -> {
            try {
                var list = submissionApiClient.getSubmissionsForTask(jwtToken, task.getId());

                Platform.runLater(() -> {
                    submissionsTable.getItems().setAll(list);
                    statusLabel.setStyle("-fx-text-fill: green;");
                    statusLabel.setText("Loaded " + list.size() + " submissions.");
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    statusLabel.setStyle("-fx-text-fill: red;");
                    statusLabel.setText("Failed to load submissions: " + e.getMessage());
                });
            }
        }).start();
    }

    @FXML
    private void onSubmitSolution() {
        if (task == null || jwtToken == null) {
            statusLabel.setStyle("-fx-text-fill: red;");
            statusLabel.setText("Missing task or token.");
            return;
        }

        javafx.scene.control.TextInputDialog dialog =
                new javafx.scene.control.TextInputDialog();
        dialog.setTitle("Submit solution");
        dialog.setHeaderText("Submit solution for task: " + task.getTitle());
        dialog.setContentText("Your answer / link:");

        dialog.showAndWait().ifPresent(text -> {
            if (text.trim().isEmpty()) {
                statusLabel.setStyle("-fx-text-fill: red;");
                statusLabel.setText("Solution cannot be empty.");
                return;
            }

            statusLabel.setText("Submitting...");

            new Thread(() -> {
                try {
                    TaskSubmissionItem created =
                            submissionApiClient.submitSolution(jwtToken, task.getId(), text);

                    Platform.runLater(() -> {
                        submissionsTable.getItems().add(created);
                        statusLabel.setStyle("-fx-text-fill: green;");
                        statusLabel.setText("Solution submitted.");
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    Platform.runLater(() -> {
                        statusLabel.setStyle("-fx-text-fill: red;");
                        statusLabel.setText("Failed to submit: " + e.getMessage());
                    });
                }
            }).start();
        });
    }
}
