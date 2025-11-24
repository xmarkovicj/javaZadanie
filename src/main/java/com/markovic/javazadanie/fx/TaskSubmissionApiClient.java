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

    public List<TaskSubmissionItem> getSubmissionsForTask(String jwtToken, Long taskId) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/submissions/task/" + taskId)) // TODO: uprav, ak máš inú URL
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

                Long id = node.has("id")
                        ? node.get("id").asLong()
                        : (node.has("submissionId") ? node.get("submissionId").asLong() : null);

                String userEmail = "";
                if (node.has("user") && node.get("user").has("email")) {
                    userEmail = node.get("user").get("email").asText();
                } else if (node.has("userEmail")) {
                    userEmail = node.get("userEmail").asText();
                }

                String content = "";
                if (node.has("content")) {
                    content = node.get("content").asText();
                } else if (node.has("answerText")) {
                    content = node.get("answerText").asText();
                }

                String submittedAt = node.has("submittedAt")
                        ? node.get("submittedAt").asText()
                        : "";

                String status = node.has("status")
                        ? node.get("status").asText()
                        : "";

                String grade = node.has("grade")
                        ? node.get("grade").asText()
                        : "";

                result.add(new TaskSubmissionItem(id, userEmail, content, submittedAt, status, grade));
            }
        }

        return result;
    }

    public TaskSubmissionItem submitSolution(String jwtToken, Long taskId, String content) throws Exception {

        JsonNode body = objectMapper.createObjectNode()
                .put("taskId", taskId)
                .put("content", content); // TODO: ak máš iné pole (solutionText, answerText), premenuj

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/submissions")) // TODO: uprav, ak máš inú URL
                .header("Authorization", "Bearer " + jwtToken)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        HttpResponse<String> response =
                httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200 && response.statusCode() != 201) {
            throw new RuntimeException("Failed to submit solution: " +
                    response.statusCode() + " " + response.body());
        }

        JsonNode node = objectMapper.readTree(response.body());

        Long id = node.has("id")
                ? node.get("id").asLong()
                : (node.has("submissionId") ? node.get("submissionId").asLong() : null);

        String userEmail = "";
        if (node.has("user") && node.get("user").has("email")) {
            userEmail = node.get("user").get("email").asText();
        } else if (node.has("userEmail")) {
            userEmail = node.get("userEmail").asText();
        }

        String submittedAt = node.has("submittedAt")
                ? node.get("submittedAt").asText()
                : "";

        String status = node.has("status")
                ? node.get("status").asText()
                : "";

        String grade = node.has("grade")
                ? node.get("grade").asText()
                : "";

        return new TaskSubmissionItem(id, userEmail, content, submittedAt, status, grade);
    }
    public TaskSubmissionItem getMySubmission(String token, Long taskId) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/submissions/my?taskId=" + taskId))
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();

        HttpResponse<String> response =
                httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 404) {
            return null; // nemá submission
        }

        if (response.statusCode() != 200) {
            throw new RuntimeException("Failed to load submission: " +
                    response.statusCode() + " " + response.body());
        }

        return objectMapper.readValue(response.body(), TaskSubmissionItem.class);
    }

}
