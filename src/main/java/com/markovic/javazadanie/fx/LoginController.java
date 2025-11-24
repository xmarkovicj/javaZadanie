package com.markovic.javazadanie.fx;

import com.markovic.javazadanie.fx.dto.UserDto;
import com.markovic.javazadanie.fx.dto.LoginResponseDto;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

/**
 * Controller pre login obrazovku.
 */
public class LoginController {

    @FXML
    private TextField loginEmailField;

    @FXML
    private PasswordField loginPasswordField;

    @FXML
    private Label statusLabel;

    private final AuthApiClient authApiClient = new AuthApiClient();

    @FXML
    public void initialize() {
        if (statusLabel != null) {
            statusLabel.setText("");
        }
    }

    @FXML
    private void onLogin() {
        if (statusLabel != null) {
            statusLabel.setStyle("-fx-text-fill: -fx-text-base-color;");
            statusLabel.setText("Prihlasujem...");
        }

        String email = loginEmailField.getText() != null ? loginEmailField.getText().trim() : "";
        String password = loginPasswordField.getText() != null ? loginPasswordField.getText().trim() : "";

        if (email.isEmpty() || password.isEmpty()) {
            if (statusLabel != null) {
                statusLabel.setStyle("-fx-text-fill: red;");
                statusLabel.setText("Zadaj email aj heslo.");
            }
            return;
        }

        new Thread(() -> {
            try {
                // predpokladám, že máš niečo ako LoginResponseDto { String token; UserDto user; }
                String token = authApiClient.login(email, password);
                SessionManager.getInstance().setToken(token);
                SessionManager.getInstance().setCurrentUser(new UserDto()); // alebo null, ak nemáš dáta o userovi

                Platform.runLater(() -> openDashboard());
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    if (statusLabel != null) {
                        statusLabel.setStyle("-fx-text-fill: red;");
                        statusLabel.setText("Prihlásenie zlyhalo: " + e.getMessage());
                    }
                });
            }
        }).start();
    }

    private void openDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/dashboard.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) loginEmailField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Study Platform - Dashboard");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            if (statusLabel != null) {
                statusLabel.setStyle("-fx-text-fill: red;");
                statusLabel.setText("Nepodarilo sa otvoriť dashboard: " + e.getMessage());
            }
        }
    }

    /**
     * Ak máš v login.fxml nejaký "Register" button s onAction="#onOpenRegister"
     */
    @FXML
    private void onRegister() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/register.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) loginEmailField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Study Platform - Register");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            if (statusLabel != null) {
                statusLabel.setStyle("-fx-text-fill: red;");
                statusLabel.setText("Nepodarilo sa otvoriť registráciu: " + e.getMessage());
            }
        }
    }
}
