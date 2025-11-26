package com.markovic.javazadanie.fx;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import com.markovic.javazadanie.fx.AlreadySubmittedException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TaskDetailController {

    @FXML
    private Label taskTitleLabel;

    @FXML
    private Label statusLabel;       // stav úlohy (napr. OPEN / DONE)

    @FXML
    private Label deadlineLabel;

    @FXML
    private Label createdByLabel;

    @FXML
    private TextArea descriptionArea;

    @FXML
    private TextArea solutionArea;

    @FXML
    private Label submissionStatusLabel;

    @FXML
    private Button deleteTaskButton;

    private final TaskApiClient taskApiClient = new TaskApiClient();
    private final TaskSubmissionApiClient submissionApiClient = new TaskSubmissionApiClient();


    private TaskItem task;
    private StudyGroupItem group;
    private String jwtToken;

    @FXML
    public void initialize() {
        // Žiadne TableColumn/Submission tabuľky – FXML ich nemá
        submissionStatusLabel.setText("");
    }

    /**
     * Volá sa z GroupDetailController.onOpenTask(...)
     */
    public void initData(TaskItem task, StudyGroupItem group, String jwtToken) {
        this.task = task;
        this.group = group;
        this.jwtToken = jwtToken;

        taskTitleLabel.setText(task.getTitle() != null ? task.getTitle() : "Task detail");
        statusLabel.setText(task.getStatus() != null ? task.getStatus() : "UNKNOWN");
        String dd = task.getDueDate();

        if (dd != null && !dd.isBlank() && !"null".equalsIgnoreCase(dd)) {
            deadlineLabel.setText(dd);
        } else {
            deadlineLabel.setText("-");
        }

        createdByLabel.setText(
                task.getCreatedBy() != null && !task.getCreatedBy().isBlank()
                        ? task.getCreatedBy()
                        : "-"
        );


        descriptionArea.setText(task.getDescription() != null ? task.getDescription() : "");
    }

    @FXML
    private void onBack() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/group_detail.fxml")
            );
            Parent root = loader.load();

            GroupDetailController controller = loader.getController();
            // vrátime sa späť do detailu skupiny s tou istou group + tokenom
            controller.initData(group, jwtToken);

            Stage stage = (Stage) taskTitleLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Group detail - " + group.getName());
        } catch (Exception e) {
            e.printStackTrace();
            // tu nemáme statusLabel z group_detail, tak aspoň popup
            new Alert(Alert.AlertType.ERROR,
                    "Failed to go back: " + e.getMessage(),
                    ButtonType.OK).showAndWait();
        }
    }

    @FXML
    private void onDeleteTask() {
        if (task == null || task.getId() == null) {
            submissionStatusLabel.setStyle("-fx-text-fill: red;");
            submissionStatusLabel.setText("Task not loaded.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete task");
        confirm.setHeaderText("Do you really want to delete this task?");
        confirm.setContentText(task.getTitle());

        var result = confirm.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) {
            return;
        }

        submissionStatusLabel.setStyle("-fx-text-fill: -fx-text-base-color;");
        submissionStatusLabel.setText("Deleting task...");

        new Thread(() -> {
            try {
                taskApiClient.deleteTask(jwtToken, task.getId());

                Platform.runLater(() -> {
                    submissionStatusLabel.setStyle("-fx-text-fill: #ff5555;");
                    submissionStatusLabel.setText("Task deleted.");

                    // po zmazaní sa vrátime do group detail
                    onBack();
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    submissionStatusLabel.setStyle("-fx-text-fill: red;");
                    submissionStatusLabel.setText("Failed to delete task: " + e.getMessage());
                });
            }
        }).start();
    }

    @FXML
    private void onRefreshSubmissions() {
        if (task == null || task.getId() == null) {
            submissionStatusLabel.setStyle("-fx-text-fill: red;");
            submissionStatusLabel.setText("Task not loaded.");
            return;
        }

        if (jwtToken == null || jwtToken.isBlank()) {
            submissionStatusLabel.setStyle("-fx-text-fill: red;");
            submissionStatusLabel.setText("You are not logged in.");
            return;
        }

        submissionStatusLabel.setStyle("-fx-text-fill: -fx-text-base-color;");
        submissionStatusLabel.setText("Loading your submission...");

        new Thread(() -> {
            try {
                TaskSubmissionItem mySub =
                        submissionApiClient.getMySubmission(jwtToken, task.getId());

                Platform.runLater(() -> {
                    if (mySub == null) {
                        submissionStatusLabel.setStyle("-fx-text-fill: #FFA500;");
                        submissionStatusLabel.setText("You have not submitted this task yet.");
                        // necháme solutionArea tak, aby si mohol niečo napísať
                    } else {
                        // zobrazíme tvoje uložené riešenie
                        solutionArea.setText(mySub.getContent() != null ? mySub.getContent() : "");
                        submissionStatusLabel.setStyle("-fx-text-fill: #8bc34a;");
                        submissionStatusLabel.setText("Last submitted at: " + mySub.getSubmittedAt());

                        // voliteľne zmeniť stav v detaile
                        statusLabel.setText("SUBMITTED");
                        if (task != null) {
                            task.setStatus("SUBMITTED");
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    submissionStatusLabel.setStyle("-fx-text-fill: red;");
                    submissionStatusLabel.setText("Failed to load submission: " + e.getMessage());
                });
            }
        }).start();
    }


    @FXML
    private void onSubmitSolution() {
        String solution = solutionArea.getText() != null
                ? solutionArea.getText().trim()
                : "";

        if (solution.isEmpty()) {
            submissionStatusLabel.setStyle("-fx-text-fill: red;");
            submissionStatusLabel.setText("Solution is empty.");
            return;
        }

        if (task == null || task.getId() == null) {
            submissionStatusLabel.setStyle("-fx-text-fill: red;");
            submissionStatusLabel.setText("Task not loaded.");
            return;
        }

        if (jwtToken == null || jwtToken.isBlank()) {
            submissionStatusLabel.setStyle("-fx-text-fill: red;");
            submissionStatusLabel.setText("You are not logged in.");
            return;
        }

        submissionStatusLabel.setStyle("-fx-text-fill: -fx-text-base-color;");
        submissionStatusLabel.setText("Submitting solution...");

        new Thread(() -> {
            try {
                TaskSubmissionItem saved =
                        submissionApiClient.submitSolution(jwtToken, task.getId(), solution);

                Platform.runLater(() -> {
                    submissionStatusLabel.setStyle("-fx-text-fill: #8bc34a;");
                    submissionStatusLabel.setText("Solution submitted at: " + saved.getSubmittedAt());

                    statusLabel.setText("SUBMITTED");
                    if (task != null) {
                        task.setStatus("SUBMITTED");
                    }
                });

            }  catch (AlreadySubmittedException ex) {
            // špeciálny prípad – už odovzdané
            Platform.runLater(() -> {
                submissionStatusLabel.setStyle("-fx-text-fill: #FFA500;");
                submissionStatusLabel.setText("You already submitted this task. Loading your submission...");
            });

            loadMySubmissionAsync();

            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    submissionStatusLabel.setStyle("-fx-text-fill: red;");
                    submissionStatusLabel.setText("Failed to submit solution: " + e.getMessage());
                });
            }
        }).start();
    }



    private void loadMySubmissionAsync() {
        if (task == null || task.getId() == null) {
            submissionStatusLabel.setStyle("-fx-text-fill: red;");
            submissionStatusLabel.setText("Task not loaded.");
            return;
        }

        if (jwtToken == null || jwtToken.isBlank()) {
            submissionStatusLabel.setStyle("-fx-text-fill: red;");
            submissionStatusLabel.setText("You are not logged in.");
            return;
        }

        submissionStatusLabel.setStyle("-fx-text-fill: -fx-text-base-color;");
        submissionStatusLabel.setText("Loading your submission...");

        new Thread(() -> {
            try {
                TaskSubmissionItem my =
                        submissionApiClient.getMySubmission(jwtToken, task.getId());

                Platform.runLater(() -> {
                    if (my == null) {
                        submissionStatusLabel.setStyle("-fx-text-fill: #FFA500;");
                        submissionStatusLabel.setText("You have not submitted this task yet.");
                    } else {
                        // zobrazíme obsah
                        solutionArea.setText(my.getContent());
                        submissionStatusLabel.setStyle("-fx-text-fill: #8bc34a;");
                        submissionStatusLabel.setText("Your submission from: " + my.getSubmittedAt());

                        statusLabel.setText("SUBMITTED");
                        if (task != null) {
                            task.setStatus("SUBMITTED");
                        }
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    submissionStatusLabel.setStyle("-fx-text-fill: red;");
                    submissionStatusLabel.setText("Failed to load submission: " + e.getMessage());
                });
            }
        }).start();
    }
    @FXML
    private void onEditTask() {
        if (task == null || task.getId() == null) {
            submissionStatusLabel.setStyle("-fx-text-fill: red;");
            submissionStatusLabel.setText("Task not loaded.");
            return;
        }

        // 1) Title
        TextInputDialog titleDlg = new TextInputDialog(task.getTitle());
        titleDlg.setTitle("Edit task");
        titleDlg.setHeaderText("Edit title");
        titleDlg.setContentText("Title:");
        var titleOpt = titleDlg.showAndWait();
        if (titleOpt.isEmpty()) return;
        String newTitle = titleOpt.get().trim();
        if (newTitle.isEmpty()) return;

        // 2) Description
        TextInputDialog descDlg = new TextInputDialog(task.getDescription());
        descDlg.setTitle("Edit task");
        descDlg.setHeaderText("Edit description");
        descDlg.setContentText("Description:");
        var descOpt = descDlg.showAndWait();
        if (descOpt.isEmpty()) return;
        String newDesc = descOpt.get().trim();

        // 3) Deadline – počet dní od teraz (celé číslo)
        TextInputDialog deadlineDlg = new TextInputDialog("1");
        deadlineDlg.setTitle("Edit task");
        deadlineDlg.setHeaderText("Change deadline (days from now)");
        deadlineDlg.setContentText("Days from now:");
        var deadlineOpt = deadlineDlg.showAndWait();
        if (deadlineOpt.isEmpty()) return;

        String daysStr = deadlineOpt.get().trim();
        long days;
        try {
            days = Long.parseLong(daysStr);
            if (days <= 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException ex) {
            submissionStatusLabel.setStyle("-fx-text-fill: red;");
            submissionStatusLabel.setText("Invalid number of days: " + daysStr);
            return;
        }

        // prepočítame na LocalDateTime a na ISO string
        LocalDateTime newDeadline = LocalDateTime.now()
                .plusDays(days)
                .withHour(23).withMinute(59).withSecond(0).withNano(0);

        String newDeadlineStr = newDeadline.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        // 4) Zavoláme update
        submissionStatusLabel.setStyle("-fx-text-fill: -fx-text-base-color;");
        submissionStatusLabel.setText("Updating task...");

        new Thread(() -> {
            try {
                TaskItem updated = taskApiClient.updateTask(
                        jwtToken,
                        task.getId(),
                        newTitle,
                        newDesc,
                        newDeadlineStr   // ← teraz posielame validný ISO datetime, nie "1"
                );

                Platform.runLater(() -> {
                    this.task = updated;
                    taskTitleLabel.setText(updated.getTitle());
                    descriptionArea.setText(updated.getDescription());
                    deadlineLabel.setText(updated.getDueDate());
                    if (updated.getStatus() != null) {
                        statusLabel.setText(updated.getStatus());
                    }
                    submissionStatusLabel.setStyle("-fx-text-fill: #8bc34a;");
                    submissionStatusLabel.setText("Task updated.");
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    submissionStatusLabel.setStyle("-fx-text-fill: red;");
                    submissionStatusLabel.setText("Failed to update task: " + e.getMessage());
                });
            }
        }).start();
    }






}
