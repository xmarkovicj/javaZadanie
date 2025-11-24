package com.markovic.javazadanie.fx;

import com.markovic.javazadanie.fx.dto.UserDto;
import javafx.application.Platform;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.util.List;

public class UsersController {

    @FXML
    private TableView<UserDto> usersTable;

    @FXML
    private TableColumn<UserDto, Number> colId;

    @FXML
    private TableColumn<UserDto, String> colName;

    @FXML
    private TableColumn<UserDto, String> colEmail;

    @FXML
    private TableColumn<UserDto, String> colCreated;

    @FXML
    private TextField searchField;

    @FXML
    private TextField nameField;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label statusLabel;

    private final UserApiClient userApiClient = new UserApiClient();

    private final ObservableList<UserDto> masterUsers = FXCollections.observableArrayList();
    private FilteredList<UserDto> filtered;

    @FXML
    public void initialize() {
        colId.setCellValueFactory(cellData -> new SimpleLongProperty(cellData.getValue().getId()));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colCreated.setCellValueFactory(new PropertyValueFactory<>("createdDate"));

        filtered = new FilteredList<>(masterUsers, u -> true);
        usersTable.setItems(filtered);

        if (searchField != null) {
            searchField.textProperty().addListener((obs, o, n) -> applyFilter());
        }

        loadUsers();
    }

    private void applyFilter() {
        String text = searchField.getText();
        String search = text == null ? "" : text.trim().toLowerCase();

        filtered.setPredicate(u -> {
            if (u == null) return false;
            if (search.isEmpty()) return true;
            String name = u.getName() != null ? u.getName().toLowerCase() : "";
            String email = u.getEmail() != null ? u.getEmail().toLowerCase() : "";
            return name.contains(search) || email.contains(search);
        });

        statusLabel.setText(filtered.size() + " users z " + masterUsers.size());
    }

    private void loadUsers() {
        statusLabel.setText("Loading users...");
        masterUsers.clear();

        new Thread(() -> {
            try {
                String token = SessionManager.getInstance().getToken();
                List<UserDto> list = userApiClient.getUsers(token);
                Platform.runLater(() -> {
                    masterUsers.setAll(list);
                    applyFilter();
                    statusLabel.setText(masterUsers.size() + " users loaded");
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() ->
                        statusLabel.setText("Failed to load users: " + e.getMessage()));
            }
        }).start();
    }

    @FXML
    private void onRefresh() {
        loadUsers();
    }

    @FXML
    private void onCreateUser() {
        String name = nameField.getText();
        String email = emailField.getText();
        String pwd = passwordField.getText();

        if (name == null || name.isBlank()
                || email == null || email.isBlank()
                || pwd == null || pwd.isBlank()) {
            statusLabel.setText("Name, email a password sú povinné");
            return;
        }

        statusLabel.setText("Creating user...");

        new Thread(() -> {
            try {
                String token = SessionManager.getInstance().getToken();
                UserDto created = userApiClient.createUser(token, name, email, pwd);
                Platform.runLater(() -> {
                    masterUsers.add(created);
                    applyFilter();
                    nameField.clear();
                    emailField.clear();
                    passwordField.clear();
                    statusLabel.setText("User created: " + created.getEmail());
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() ->
                        statusLabel.setText("Failed to create user: " + e.getMessage()));
            }
        }).start();
    }

    @FXML
    private void onDeleteSelected() {
        UserDto selected = usersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusLabel.setText("Žiadny user nevybraný");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete user");
        confirm.setHeaderText(null);
        confirm.setContentText("Naozaj chceš zmazať usera: " + selected.getEmail() + "?");

        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                new Thread(() -> {
                    try {
                        String token = SessionManager.getInstance().getToken();
                        userApiClient.deleteUser(token, selected.getId());
                        Platform.runLater(() -> {
                            masterUsers.remove(selected);
                            applyFilter();
                            statusLabel.setText("User deleted");
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                        Platform.runLater(() ->
                                statusLabel.setText("Failed to delete user: " + e.getMessage()));
                    }
                }).start();
            }
        });
    }

    @FXML
    private void onClose() {
        Stage stage = (Stage) usersTable.getScene().getWindow();
        stage.close();
    }
}
