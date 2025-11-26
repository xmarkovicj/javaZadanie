package com.markovic.javazadanie.fx;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class TaskSubmissionApiClient {

    private static final String BASE_URL = "http://localhost:8080/api";
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * GET /api/tasks/{taskId}/submissions  – všetky odovzdania pre úlohu
     */
    public List<TaskSubmissionItem> getSubmissionsForTask(String jwtToken, Long taskId) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/tasks/" + taskId + "/submissions"))
                .header("Authorization", "Bearer " + jwtToken)
                .GET()
                .build();

        HttpResponse<String> response =
                httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Failed to load submissions: " +
                    response.statusCode() + " " + response.body());
        }

        List<TaskSubmissionItem> result = new ArrayList<>();
        JsonNode arr = objectMapper.readTree(response.body());

        if (arr.isArray()) {
            for (JsonNode node : arr) {
                result.add(parseSubmissionNode(node));
            }
        }

        return result;
    }

    /**
     * POST /api/tasks/{taskId}/submissions – odoslanie riešenia
     */
    public TaskSubmissionItem submitSolution(String jwtToken, Long taskId, String content) throws Exception {

        JsonNode body = objectMapper.createObjectNode()
                .put("content", content);
        // attachmentUrl vieš doplniť neskôr

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/tasks/" + taskId + "/submissions"))
                .header("Authorization", "Bearer " + jwtToken)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        HttpResponse<String> response =
                httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        // backend pri "už odovzdal" hádže RuntimeException -> 500 s textom "User already submitted this task"
        if (response.statusCode() >= 400) {
            String bodyText = response.body();
            if (bodyText != null && bodyText.contains("User already submitted this task")) {
                // špeciálny marker, ktorý chytáš v TaskDetailController
                throw new AlreadySubmittedException();
            }
            throw new RuntimeException("Failed to submit solution: " +
                    response.statusCode() + " " + bodyText);
        }

        JsonNode node = objectMapper.readTree(response.body());
        return parseSubmissionNode(node);
    }

    /**
     * GET /api/tasks/{taskId}/submissions/my – moje riešenie pre danú úlohu
     */
    public TaskSubmissionItem getMySubmission(String token, Long taskId) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/tasks/" + taskId + "/submissions/my"))
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();

        HttpResponse<String> response =
                httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 404) {
            // užívateľ ešte nemá submission
            return null;
        }

        if (response.statusCode() != 200) {
            throw new RuntimeException("Failed to load submission: " +
                    response.statusCode() + " " + response.body());
        }

        JsonNode node = objectMapper.readTree(response.body());
        return parseSubmissionNode(node);
    }

    /**
     * Pomocná metóda na mapovanie JSON -> TaskSubmissionItem
     */
    private TaskSubmissionItem parseSubmissionNode(JsonNode node) {
        Long id = null;
        if (node.has("id") && !node.get("id").isNull()) {
            id = node.get("id").asLong();
        } else if (node.has("submissionId") && !node.get("submissionId").isNull()) {
            id = node.get("submissionId").asLong();
        }

        String userEmail = "";
        if (node.has("user") && node.get("user").has("email")) {
            userEmail = node.get("user").get("email").asText();
        } else if (node.has("userEmail")) {
            userEmail = node.get("userEmail").asText();
        }

        String content = node.has("content") && !node.get("content").isNull()
                ? node.get("content").asText()
                : "";

        String submittedAt = node.has("submittedAt") && !node.get("submittedAt").isNull()
                ? node.get("submittedAt").asText()
                : "";

        // backend zatiaľ status nemá → môžeme si ho vymyslieť
        String status = "SUBMITTED";

        String grade = node.has("grade") && !node.get("grade").isNull()
                ? node.get("grade").asText()
                : "";

        return new TaskSubmissionItem(id, userEmail, content, submittedAt, status, grade);
    }
}
