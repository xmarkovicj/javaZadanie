package com.markovic.javazadanie.fx;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.util.Map;

public class StatsController {

    @FXML
    private Label groupNameLabel;

    @FXML
    private PieChart statusPieChart;

    @FXML
    private BarChart<String, Number> participationChart;

    @FXML
    private LineChart<String, Number> activityChart;

    private final StatsApiClient statsApiClient = new StatsApiClient();

    private StudyGroupItem group;
    private String token;

    public void initData(StudyGroupItem group, String token) {
        this.group = group;
        this.token = token;
        groupNameLabel.setText(group.getName());

        loadStats();
    }

    private void loadStats() {
        new Thread(() -> {
            try {
                var stats = statsApiClient.loadGroupStats(token, group.getId());

                Platform.runLater(() -> {
                    // PieChart – stav úloh
                    statusPieChart.getData().clear();
                    statusPieChart.getData().add(new PieChart.Data("OPEN", stats.openTasks));
                    statusPieChart.getData().add(new PieChart.Data("SUBMITTED", stats.submittedTasks));
                    statusPieChart.getData().add(new PieChart.Data("CLOSED", stats.closedTasks));

                    // BarChart – účasti členov
                    participationChart.getData().clear();
                    XYChart.Series<String, Number> series = new XYChart.Series<>();
                    series.setName("Odovzdania");

                    stats.memberSubmissions.forEach((email, count) -> {
                        series.getData().add(new XYChart.Data<>(email, count));
                    });

                    participationChart.getData().add(series);

// otočenie textu
                    CategoryAxis xAxis = (CategoryAxis) participationChart.getXAxis();
                    xAxis.setTickLabelRotation(-45);

// POČKAJ, kým JavaFX vykreslí graf, potom centrovať
                    Platform.runLater(() -> {
                        participationChart.lookupAll(".axis-x .tick-label").forEach(node -> {
                            node.setStyle("-fx-text-alignment: center;");
                        });
                    });





                    // LineChart – aktivita v čase
                    activityChart.getData().clear();
                    XYChart.Series<String, Number> actSeries = new XYChart.Series<>();
                    actSeries.setName("Aktivity");
                    stats.activityPerDay.entrySet().stream()
                            .sorted(Map.Entry.comparingByKey())
                            .forEach(e -> actSeries.getData()
                                    .add(new XYChart.Data<>(e.getKey(), e.getValue())));
                    activityChart.getData().add(actSeries);
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    @FXML
    private void onClose() {
        Stage stage = (Stage) groupNameLabel.getScene().getWindow();
        stage.close();
    }
}
