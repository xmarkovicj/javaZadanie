package com.markovic.javazadanie.fx;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class GroupApiClient {

    private static final String BASE_URL = "http://localhost:8080/api/groups";

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // === GET /api/groups ===
    public List<StudyGroupItem> getAllGroups(String token) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();

        HttpResponse<String> response =
                httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Failed to load groups: " +
                    response.statusCode() + " " + response.body());
        }

        JsonNode arr = objectMapper.readTree(response.body());
        List<StudyGroupItem> result = new ArrayList<>();

        if (arr.isArray()) {
            for (JsonNode node : arr) {
                Long id = node.has("id")
                        ? node.get("id").asLong()
                        : node.get("groupId").asLong();

                String name = node.has("name") ? node.get("name").asText() : "";
                String description = node.has("description") ? node.get("description").asText() : "";

                result.add(new StudyGroupItem(id, name, description));
            }
        }

        return result;
    }

    // === POST /api/groups ===
    public StudyGroupItem createGroup(String token, String name, String description) throws Exception {
        ObjectNode body = objectMapper.createObjectNode()
                .put("name", name)
                .put("description", description);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        HttpResponse<String> response =
                httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200 && response.statusCode() != 201) {
            throw new RuntimeException("Failed to create group: " +
                    response.statusCode() + " " + response.body());
        }

        JsonNode node = objectMapper.readTree(response.body());

        Long id = node.has("id")
                ? node.get("id").asLong()
                : node.get("groupId").asLong();

        String respName = node.has("name") ? node.get("name").asText() : name;
        String respDesc = node.has("description") ? node.get("description").asText() : description;

        return new StudyGroupItem(id, respName, respDesc);
    }

    // === PUT /api/groups/{id} ===
    public StudyGroupItem updateGroup(String token, Long groupId,
                                      String name, String description) throws Exception {

        ObjectNode body = objectMapper.createObjectNode()
                .put("name", name)
                .put("description", description);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + groupId))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        HttpResponse<String> response =
                httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Failed to update group: " +
                    response.statusCode() + " " + response.body());
        }

        JsonNode node = objectMapper.readTree(response.body());

        Long id = node.has("id")
                ? node.get("id").asLong()
                : node.get("groupId").asLong();

        String respName = node.has("name") ? node.get("name").asText() : name;
        String respDesc = node.has("description") ? node.get("description").asText() : description;

        return new StudyGroupItem(id, respName, respDesc);
    }

    // === DELETE /api/groups/{id} ===
    public void deleteGroup(String token, Long groupId) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + groupId))
                .header("Authorization", "Bearer " + token)
                .DELETE()
                .build();

        HttpResponse<String> response =
                httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200 && response.statusCode() != 204) {
            throw new RuntimeException("Failed to delete group: " +
                    response.statusCode() + " " + response.body());
        }
    }
}
