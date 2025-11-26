package com.markovic.javazadanie.fx;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class LoginController {

    @FXML
    private TextField loginEmailField;

    @FXML
    private PasswordField loginPasswordField;

    @FXML
    private TextField registerNameField;

    @FXML
    private TextField registerEmailField;

    @FXML
    private PasswordField registerPasswordField;

    @FXML
    private Label statusLabel;

    @FXML
    private ProgressIndicator progressIndicator;

    @FXML
    private TabPane tabPane;

    @FXML
    private Button loginButton;

    @FXML
    private Button registerButton;

    private final AuthApiClient authApiClient = new AuthApiClient();

    @FXML
    public void initialize() {
        statusLabel.setText("");
        progressIndicator.setVisible(false);
    }

    @FXML
    private void onLogin() {
        String email = loginEmailField.getText() != null ? loginEmailField.getText().trim() : "";
        String password = loginPasswordField.getText() != null ? loginPasswordField.getText() : "";

        if (email.isEmpty() || password.isEmpty()) {
            statusLabel.setStyle("-fx-text-fill: #ff6b6b;");
            statusLabel.setText("Zadaj e-mail aj heslo.");
            return;
        }

        statusLabel.setStyle("-fx-text-fill: #cccccc;");
        statusLabel.setText("Prihlasujem...");
        progressIndicator.setVisible(true);
        loginButton.setDisable(true);
        registerButton.setDisable(true);

        new Thread(() -> {
            try {
                // zavolá /api/auth/login, nastaví SessionManager
                String token = authApiClient.login(email, password);

                Platform.runLater(() -> {
                    statusLabel.setStyle("-fx-text-fill: #4caf50;");
                    statusLabel.setText("Prihlásenie úspešné.");

                    // otvoríme dashboard
                    openDashboard();
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    statusLabel.setStyle("-fx-text-fill: #ff6b6b;");
                    statusLabel.setText("Prihlásenie zlyhalo: " + e.getMessage());
                    progressIndicator.setVisible(false);
                    loginButton.setDisable(false);
                    registerButton.setDisable(false);
                });
            }
        }).start();
    }

    @FXML
    private void onRegister() {
        String name = registerNameField.getText() != null ? registerNameField.getText().trim() : "";
        String email = registerEmailField.getText() != null ? registerEmailField.getText().trim() : "";
        String password = registerPasswordField.getText() != null ? registerPasswordField.getText() : "";

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            statusLabel.setStyle("-fx-text-fill: #ff6b6b;");
            statusLabel.setText("Meno, e-mail aj heslo sú povinné.");
            return;
        }

        statusLabel.setStyle("-fx-text-fill: #cccccc;");
        statusLabel.setText("Registrujem používateľa...");
        progressIndicator.setVisible(true);
        loginButton.setDisable(true);
        registerButton.setDisable(true);

        new Thread(() -> {
            try {
                // zavolá /api/auth/register
                authApiClient.register(name, email, password);

                Platform.runLater(() -> {
                    statusLabel.setStyle("-fx-text-fill: #4caf50;");
                    statusLabel.setText("Účet vytvorený. Môžeš sa prihlásiť.");
                    progressIndicator.setVisible(false);
                    loginButton.setDisable(false);
                    registerButton.setDisable(false);

                    // prepni na tab „Prihlásenie“
                    if (tabPane != null) {
                        tabPane.getSelectionModel().selectFirst();
                    }

                    // predvyplníme e-mail do login tabu
                    loginEmailField.setText(email);
                    loginPasswordField.setText("");
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    statusLabel.setStyle("-fx-text-fill: #ff6b6b;");
                    statusLabel.setText("Registrácia zlyhala: " + e.getMessage());
                    progressIndicator.setVisible(false);
                    loginButton.setDisable(false);
                    registerButton.setDisable(false);
                });
            }
        }).start();
    }

    private void openDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/dashboard.fxml")
            );
            Parent root = loader.load();

            Stage stage = (Stage) loginEmailField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("StudyHub - Dashboard");
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setStyle("-fx-text-fill: #ff6b6b;");
            statusLabel.setText("Nepodarilo sa otvoriť dashboard: " + e.getMessage());
        }
    }
}
