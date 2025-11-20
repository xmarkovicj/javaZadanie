package com.markovic.javazadanie.fx;

import com.markovic.javazadanie.JavaZadanieApplication;
import javafx.application.Application;
import org.springframework.boot.SpringApplication;

public class FXLauncher {
    public static void main(String[] args) {
        // Spusti backend
        SpringApplication.run(JavaZadanieApplication.class, args);

        // Spusti JavaFX UI
        Application.launch(MainApp.class, args);
    }
}
