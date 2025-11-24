package com.markovic.javazadanie.fx;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class TaskApiClient {

    private static final String BASE_URL = "http://localhost:8080/api";
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Načíta tasks pre danú group.
     * UPRAV si URL podľa svojho backendu, napr:
     *  - /api/tasks/group/{groupId}
     *  - alebo /api/tasks?groupId={groupId}
     */
    public List<TaskItem> getTasksForGroup(String jwtToken, Long groupId) throws Exception {
        // PRÍKLAD: GET /api/tasks/group/{id}
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/tasks/group/" + groupId))
                .header("Authorization", "Bearer " + jwtToken)
                .GET()
                .build();

        HttpResponse<String> response =
                httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Failed to load tasks: " +
                    response.statusCode() + " " + response.body());
        }

        List<TaskItem> result = new ArrayList<>();
        JsonNode arr = objectMapper.readTree(response.body());

        if (arr.isArray()) {
            for (JsonNode node : arr) {

                Long id = null;
                if (node.has("id")) {
                    id = node.get("id").asLong();
                } else if (node.has("taskId")) {
                    id = node.get("taskId").asLong();
                }

                String title = node.has("title") ? node.get("title").asText() : "";
                String description = node.has("description") ? node.get("description").asText() : "";
                String dueDate = "";
                if (node.has("dueDate")) {
                    dueDate = node.get("dueDate").asText();
                } else if (node.has("deadline")) {
                    dueDate = node.get("deadline").asText();
                }

                result.add(new TaskItem(id, title, description, dueDate));
            }
        }

        return result;
    }

    /**
     * Vytvorenie tasku v skupine.
     * UPRAV URL + body podľa TaskCreateRequest v backende.
     */
    public TaskItem createTask(String jwtToken, Long groupId,
                               String title, String description, String dueDate) throws Exception {

        JsonNode body = objectMapper.createObjectNode()
                .put("groupId", groupId)
                .put("title", title)
                .put("description", description)
                .put("dueDate", dueDate); // napr. "2025-11-20T12:00:00"

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/tasks"))
                .header("Authorization", "Bearer " + jwtToken)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        HttpResponse<String> response =
                httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200 && response.statusCode() != 201) {
            throw new RuntimeException("Failed to create task: " +
                    response.statusCode() + " " + response.body());
        }

        JsonNode node = objectMapper.readTree(response.body());

        Long id = null;
        if (node.has("id")) {
            id = node.get("id").asLong();
        } else if (node.has("taskId")) {
            id = node.get("taskId").asLong();
        }

        String tTitle = node.has("title") ? node.get("title").asText() : title;
        String tDesc = node.has("description") ? node.get("description").asText() : description;
        String tDue = node.has("dueDate") ? node.get("dueDate").asText() : dueDate;

        return new TaskItem(id, tTitle, tDesc, tDue);
    }
    public void deleteTask(String token, Long taskId) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(BASE_URL + "/" + taskId);  // napr. http://localhost:8080/api/tasks/5
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("DELETE");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + token);

            int status = conn.getResponseCode();

            // 200 OK alebo 204 No Content berieme ako úspech
            if (status == HttpURLConnection.HTTP_OK || status == HttpURLConnection.HTTP_NO_CONTENT) {
                return;
            }

            // inak si prečítame error body a hodíme výnimku
            InputStream err = conn.getErrorStream();
            String body = "";
            if (err != null) {
                body = new String(err.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
            }
            throw new RuntimeException("Failed to delete task: " + status + " " + body);

        } catch (IOException e) {
            throw new RuntimeException("Failed to delete task", e);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }
}
