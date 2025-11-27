package com.markovic.javazadanie.fx;

import com.markovic.javazadanie.fx.dto.GroupDto;
import com.markovic.javazadanie.fx.dto.MemberDto;
import javafx.application.Platform;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import com.markovic.javazadanie.fx.dto.GroupDto;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;


public class GroupDetailController {

    @FXML
    private BorderPane groupRoot;
    @FXML
    private Label groupNameLabel;

    @FXML
    private Label groupDescriptionLabel;

    @FXML
    private Label statusLabel;

    @FXML
    private Button joinButton;

    @FXML
    private Button leaveButton;

    // --- members ---
    @FXML
    private TableView<MemberDto> membersTable;

    @FXML
    private TableColumn<MemberDto, String> colMemberName;

    @FXML
    private TableColumn<MemberDto, String> colMemberEmail;

    // --- tasks ---
    @FXML
    private TableView<TaskItem> tasksTable;

    @FXML
    private TableColumn<TaskItem, Number> colTaskId;

    @FXML
    private TableColumn<TaskItem, String> colTitle;

    @FXML
    private TableColumn<TaskItem, String> colStatus;

    @FXML
    private TableColumn<TaskItem, String> colDeadline;

    private final ObservableList<TaskItem> tasks = FXCollections.observableArrayList();
    private final ObservableList<MemberDto> members = FXCollections.observableArrayList();

    private final TaskApiClient taskApiClient = new TaskApiClient();
    private final GroupApiClient groupApiClient = new GroupApiClient();
    private final MembershipApiClient membershipApiClient = new MembershipApiClient();
    private static GroupDetailController CURRENT_INSTANCE;
    // toto je DTO pre mazanie skupiny (ak ho používaš inde)
    private GroupDto currentGroup;

    // toto je položka z tabuľky skupín na dashboarde
    private StudyGroupItem group;
    private String jwtToken;
    private TaskWebSocketClient wsClient;

    // ---------------------------------------------------------
    // Inicializácia z dashboardu
    // ---------------------------------------------------------
    public void initData(StudyGroupItem group, String jwtToken) {
        this.group = group;
        this.jwtToken = jwtToken;


        groupNameLabel.setText(group.getName() != null ? group.getName() : "Group");
        if (groupDescriptionLabel != null) {
            groupDescriptionLabel.setText(
                    group.getDescription() != null ? group.getDescription() : ""
            );
        }
        GroupDto dto = new GroupDto();
        dto.setId(group.getId());
        dto.setName(group.getName());
        dto.setDescription(group.getDescription());
        this.currentGroup = dto;

        refreshGroupDetails();
        loadMembers();
        wsClient = new TaskWebSocketClient(
                group.getId(),
                v -> loadTasks()
        );
        wsClient.connect();


    }

    // ---------------------------------------------------------
    // JavaFX initialize()
    // ---------------------------------------------------------
    @FXML
    public void initialize() {
        if (statusLabel != null) {
            statusLabel.setText("");
        }
        CURRENT_INSTANCE = this;


        // tasks
        if (colTaskId != null) {
            colTaskId.setCellValueFactory(cell ->
                    new SimpleLongProperty(
                            cell.getValue().getId() != null ? cell.getValue().getId() : 0L
                    )
            );
        }

        if (colTitle != null) {
            colTitle.setCellValueFactory(cell ->
                    new SimpleStringProperty(
                            cell.getValue().getTitle() != null ? cell.getValue().getTitle() : ""
                    )
            );
        }

        if (colStatus != null) {
            colStatus.setCellValueFactory(cell ->
                    new SimpleStringProperty(
                            cell.getValue().getStatus() != null ? cell.getValue().getStatus() : ""
                    )
            );
        }

        if (colDeadline != null) {
            colDeadline.setCellValueFactory(cell ->
                    new SimpleStringProperty(
                            cell.getValue().getDueDate() != null ? cell.getValue().getDueDate() : ""
                    )
            );
        }

        if (tasksTable != null) {
            tasksTable.setItems(tasks);
        }

        // members
        if (colMemberName != null) {
            colMemberName.setCellValueFactory(cell ->
                    new SimpleStringProperty(
                            cell.getValue().getName() != null ? cell.getValue().getName() : ""
                    )
            );
        }

        if (colMemberEmail != null) {
            colMemberEmail.setCellValueFactory(cell ->
                    new SimpleStringProperty(
                            cell.getValue().getEmail() != null ? cell.getValue().getEmail() : ""
                    )
            );
        }

        if (membersTable != null) {
            membersTable.setItems(members);
        }
    }

    // ---------------------------------------------------------
    // Navigácia
    // ---------------------------------------------------------
    @FXML
    private void onBack() {
        try {
            if (wsClient != null) {
                wsClient.close();
            }
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

    // ---------------------------------------------------------
    // Tasks
    // ---------------------------------------------------------
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

        // 1) Názov
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

            // 2) Popis
            TextInputDialog descDialog = new TextInputDialog();
            descDialog.setTitle("New Task");
            descDialog.setHeaderText("Description");
            descDialog.setContentText("Description:");

            descDialog.showAndWait().ifPresent(desc -> {

                // 3) Deadline – celé číslo v dňoch
                TextInputDialog daysDialog = new TextInputDialog("1");
                daysDialog.setTitle("New Task");
                daysDialog.setHeaderText("Deadline");
                daysDialog.setContentText("Deadline in days from now (1, 2, 3...):");

                daysDialog.showAndWait().ifPresent(daysStr -> {
                    int days;
                    try {
                        days = Integer.parseInt(daysStr.trim());
                        if (days <= 0) {
                            throw new NumberFormatException("non-positive");
                        }
                    } catch (NumberFormatException ex) {
                        statusLabel.setStyle("-fx-text-fill: red;");
                        statusLabel.setText("Deadline must be a positive integer number of days.");
                        return;
                    }

                    // napr. o N dní o 23:59
                    LocalDateTime due = LocalDateTime.now()
                            .plusDays(days)
                            .withHour(23)
                            .withMinute(59)
                            .withSecond(0)
                            .withNano(0);

                    String dueStr = due.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

                    createTask(title, desc, dueStr);
                });
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
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/task_detail.fxml")
            );
            Parent root = loader.load();

            TaskDetailController controller = loader.getController();
            controller.initData(selected, group, jwtToken);

            Stage stage = (Stage) tasksTable.getScene().getWindow();
            stage.setScene(new javafx.scene.Scene(root));
            stage.setTitle("Task detail - " + selected.getTitle());
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setStyle("-fx-text-fill: red;");
            statusLabel.setText("Failed to open task: " + e.getMessage());
        }
    }

    // ---------------------------------------------------------
    // ---------------------------------------------------------
    @FXML
    private void onDeleteGroup() {
        if (group == null) {
            statusLabel.setText("Žiadna skupina nie je vybraná");
            return;
        }

        Long groupId = group.getId();

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete group");
        confirm.setHeaderText(null);
        confirm.setContentText("Naozaj chceš zmazať skupinu: " + group.getName() + "?");

        confirm.showAndWait().ifPresent(button -> {
            if (button == ButtonType.OK) {
                new Thread(() -> {
                    try {
                        String token = SessionManager.getInstance().getToken();
                        groupApiClient.deleteGroup(token, groupId);

                        Platform.runLater(() -> {
                            statusLabel.setText("Skupina bola zmazaná");
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

    // ---------------------------------------------------------
    // Join / Leave group (membership)
    // ---------------------------------------------------------
    @FXML
    private void onJoinGroup() {
        if (group == null) {
            statusLabel.setStyle("-fx-text-fill: red;");
            statusLabel.setText("Group not loaded.");
            return;
        }

        String token = SessionManager.getInstance().getToken();
        if (token == null || token.isEmpty()) {
            statusLabel.setStyle("-fx-text-fill: red;");
            statusLabel.setText("You are not logged in.");
            return;
        }

        statusLabel.setStyle("-fx-text-fill: -fx-text-base-color;");
        statusLabel.setText("Joining group...");

        new Thread(() -> {
            try {
                membershipApiClient.joinGroup(token, group.getId());

                javafx.application.Platform.runLater(() -> {
                    statusLabel.setStyle("-fx-text-fill: #7CFC00;");
                    statusLabel.setText("You have joined this group.");
                    refreshGroupDetails();
                    try {
                        loadMembers();
                    } catch (Exception ignored) {}
                });
            } catch (Exception e) {
                e.printStackTrace();
                javafx.application.Platform.runLater(() -> {
                    statusLabel.setStyle("-fx-text-fill: red;");
                    statusLabel.setText("Failed to join group: " + e.getMessage());
                });
            }
        }).start();
    }


    @FXML
    private void onLeaveGroup() {
        if (group == null) {
            statusLabel.setStyle("-fx-text-fill: red;");
            statusLabel.setText("Group not loaded.");
            return;
        }

        if (jwtToken == null || jwtToken.isEmpty()) {
            statusLabel.setStyle("-fx-text-fill: red;");
            statusLabel.setText("You are not logged in.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Leave group");
        confirm.setHeaderText("Do you really want to leave this group?");
        confirm.setContentText("Group: " + group.getName());

        var result = confirm.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) {
            return;
        }

        statusLabel.setStyle("-fx-text-fill: -fx-text-base-color;");
        statusLabel.setText("Leaving group...");

        new Thread(() -> {
            try {
                membershipApiClient.leaveGroup(jwtToken, group.getId());

                Platform.runLater(() -> {
                    statusLabel.setStyle("-fx-text-fill: #FFA500;");
                    statusLabel.setText("You have left this group.");
                    refreshGroupDetails();
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    statusLabel.setStyle("-fx-text-fill: red;");
                    statusLabel.setText("Failed to leave group: " + e.getMessage());
                });
            }
        }).start();
    }

    // ---------------------------------------------------------
    // Members
    // ---------------------------------------------------------
    private void loadMembers() {
        statusLabel.setStyle("-fx-text-fill: -fx-accent;");
        statusLabel.setText("Načítavam členov skupiny...");

        String token = (jwtToken != null && !jwtToken.isBlank())
                ? jwtToken
                : SessionManager.getInstance().getToken();

        if (token == null || token.isBlank()) {
            statusLabel.setStyle("-fx-text-fill: red;");
            statusLabel.setText("Chýba token – prihlás sa znova.");
            return;
        }

        if (group == null || group.getId() == null) {
            statusLabel.setStyle("-fx-text-fill: red;");
            statusLabel.setText("Skupina nie je inicializovaná.");
            return;
        }

        new Thread(() -> {
            try {
                var list = membershipApiClient.getMembers(token, group.getId());

                Platform.runLater(() -> {
                    members.setAll(list);
                    statusLabel.setStyle("-fx-text-fill: #8bc34a;");
                    statusLabel.setText("Načítaných " + list.size() + " členov.");
                });
            } catch (Exception ex) {
                ex.printStackTrace();
                Platform.runLater(() -> {
                    statusLabel.setStyle("-fx-text-fill: red;");
                    statusLabel.setText("Nepodarilo sa načítať členov: " + ex.getMessage());
                });
            }
        }).start();
    }
    @FXML
    private void onOpenTaskDetail() {
        // použijeme existujúcu logiku
        onOpenTask();
    }

    // ---------------------------------------------------------
    // Refresh detailu (tasks + members)
    // ---------------------------------------------------------
    private void refreshGroupDetails() {
        loadTasks();
        loadMembers();
    }
    @FXML
    private void onDeleteTask() {
        TaskItem selected = tasksTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusLabel.setStyle("-fx-text-fill: red;");
            statusLabel.setText("Please select a task first.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete task");
        confirm.setHeaderText("Do you really want to delete this task?");
        confirm.setContentText("Task: " + selected.getTitle());

        var result = confirm.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) {
            return;
        }

        statusLabel.setStyle("-fx-text-fill: -fx-text-base-color;");
        statusLabel.setText("Deleting task...");

        new Thread(() -> {
            try {
                // predpoklad: TaskApiClient má metódu deleteTask(token, taskId)
                taskApiClient.deleteTask(jwtToken, selected.getId());

                Platform.runLater(() -> {
                    tasks.remove(selected);
                    statusLabel.setStyle("-fx-text-fill: #ff9800;"); // oranžová
                    statusLabel.setText("Task deleted.");
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    statusLabel.setStyle("-fx-text-fill: red;");
                    statusLabel.setText("Failed to delete task: " + e.getMessage());
                });
            }
        }).start();
    }

    public static void refreshCurrentTasksStatic() {
        if (CURRENT_INSTANCE != null) {
            CURRENT_INSTANCE.loadTasks();
        }
    }
    @FXML
    private void onEditGroup() {
        if (group == null || group.getId() == null) {
            statusLabel.setStyle("-fx-text-fill: red;");
            statusLabel.setText("Group not loaded.");
            return;
        }

        TextInputDialog nameDlg = new TextInputDialog(group.getName());
        nameDlg.setTitle("Edit group");
        nameDlg.setHeaderText("Edit group name");
        nameDlg.setContentText("Name:");
        var nameOpt = nameDlg.showAndWait();
        if (nameOpt.isEmpty()) return;
        String newName = nameOpt.get().trim();
        if (newName.isEmpty()) return;

        TextInputDialog descDlg = new TextInputDialog(group.getDescription());
        descDlg.setTitle("Edit group");
        descDlg.setHeaderText("Edit description");
        descDlg.setContentText("Description:");
        var descOpt = descDlg.showAndWait();
        if (descOpt.isEmpty()) return;
        String newDesc = descOpt.get().trim();

        statusLabel.setStyle("-fx-text-fill: -fx-text-base-color;");
        statusLabel.setText("Updating group...");

        String token = SessionManager.getInstance().getToken();

        new Thread(() -> {
            try {
                StudyGroupItem updated =
                        groupApiClient.updateGroup(token, group.getId(), newName, newDesc);

                Platform.runLater(() -> {
                    this.group = updated;
                    groupNameLabel.setText("Group: " + updated.getName());
                    statusLabel.setStyle("-fx-text-fill: #8bc34a;");
                    statusLabel.setText("Group updated.");
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    statusLabel.setStyle("-fx-text-fill: red;");
                    statusLabel.setText("Failed to update group: " + e.getMessage());
                });
            }
        }).start();
    }
    @FXML
    private void onOpenStats() {
        if (group == null || jwtToken == null) {
            System.err.println("Group or token is null in onOpenStats()");
            Alert a = new Alert(Alert.AlertType.WARNING);
            a.setTitle("No group");
            a.setHeaderText("Group data not loaded");
            a.setContentText("Please reopen the group and try again.");
            a.showAndWait();
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/stats.fxml")
            );
            Parent root = loader.load();

            StatsController controller = loader.getController();
            controller.initData(group, jwtToken);

            Stage stage = new Stage();
            stage.setTitle("Statistics - " + group.getName());
            stage.setScene(new Scene(root));

            // Owner okna – len ak máme root v scene
            if (groupRoot != null && groupRoot.getScene() != null) {
                stage.initOwner(groupRoot.getScene().getWindow());
            }

            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }






}
