package com.markovic.javazadanie.fx;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {

    @FXML
    private TextField loginEmailField;

    @FXML
    private PasswordField loginPasswordField;

    @FXML
    private TextField regNameField;

    @FXML
    private TextField regEmailField;

    @FXML
    private PasswordField regPasswordField;

    @FXML
    private Label statusLabel;

    private final AuthApiClient authApiClient = new AuthApiClient();

    // sem si môžeš uložiť token po logine (neskôr ho využijeme v ďalších requestoch)
    private static String jwtToken;

    @FXML
    public void initialize() {
        statusLabel.setText("");
    }

    @FXML
    private void onLogin() {
        String email = loginEmailField.getText();
        String password = loginPasswordField.getText();

        statusLabel.setText("");

        new Thread(() -> {
            try {
                String token = authApiClient.login(email, password);
                jwtToken = token;

                Platform.runLater(() -> {
                    statusLabel.setStyle("-fx-text-fill: green;");
                    statusLabel.setText("Login successful!");

                    try {
                        // načítaj dashboard.fxml
                        javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                                getClass().getResource("/fxml/dashboard.fxml")
                        );
                        javafx.scene.Scene scene = new javafx.scene.Scene(loader.load());

                        javafx.stage.Stage stage =
                                (javafx.stage.Stage) loginEmailField.getScene().getWindow();
                        stage.setScene(scene);
                        stage.setTitle("Study Platform - Dashboard");
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        statusLabel.setStyle("-fx-text-fill: red;");
                        statusLabel.setText("Failed to open dashboard: " + ex.getMessage());
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    statusLabel.setStyle("-fx-text-fill: red;");
                    statusLabel.setText("Login failed: " + e.getMessage());
                });
            }
        }).start();
    }

    @FXML
    private void onRegister() {
        String name = regNameField.getText();
        String email = regEmailField.getText();
        String password = regPasswordField.getText();

        statusLabel.setText("");

        new Thread(() -> {
            try {
                authApiClient.register(name, email, password);
                Platform.runLater(() -> {
                    statusLabel.setStyle("-fx-text-fill: green;");
                    statusLabel.setText("Registration successful, you can log in now.");
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    statusLabel.setStyle("-fx-text-fill: red;");
                    statusLabel.setText("Registration failed: " + e.getMessage());
                });
            }
        }).start();
    }

    public static String getJwtToken() {
        return jwtToken;
    }
}
