package com.markovic.javazadanie.fx;

import javafx.application.Platform;
import javafx.scene.control.Alert;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;

public class TaskWebSocketClient implements WebSocket.Listener {

    private WebSocket webSocket;
    private final long groupId;
    private final Consumer<Void> onRefresh;  // callback => čo spraviť pri REFRESH správe

    public TaskWebSocketClient(long groupId, Consumer<Void> onRefresh) {
        this.groupId = groupId;
        this.onRefresh = onRefresh;
    }

    public void connect() {
        HttpClient client = HttpClient.newHttpClient();

        client.newWebSocketBuilder()
                .buildAsync(URI.create("ws://localhost:8080/ws"), this)
                .thenAccept(ws -> {
                    this.webSocket = ws;
                    // pri STOMP by si tu ešte posielal SUBSCRIBE,
                    // ale na jednoduchý príklad budeme zatiaľ počúvať všetko
                })
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    return null;
                });
    }

    @Override
    public void onOpen(WebSocket webSocket) {
        System.out.println("WS connected for group " + groupId);
        WebSocket.Listener.super.onOpen(webSocket);
    }

    @Override
    public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
        String message = data.toString();
        System.out.println("[FX] WS message: " + message);

        // Ak príde notifikácia o novom tasku
        if (message.startsWith("NEW_TASK_CREATED:")) {
            String taskName = message.substring("NEW_TASK_CREATED:".length()).trim();
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("New Task Created");
                alert.setHeaderText("A new task was added to your group!");
                alert.setContentText("📝 " + taskName);
                alert.showAndWait();

                // voliteľne môžeš refreshnúť úlohy:
                try {
                    GroupDetailController.refreshCurrentTasksStatic();
                } catch (Exception ignored) {}
            });
        }

        webSocket.request(1);
        return null;
    }


    public void close() {
        if (webSocket != null) {
            webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "bye");
        }
    }
}
