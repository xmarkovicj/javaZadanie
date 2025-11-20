package com.markovic.javazadanie.fx;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class DashboardApiClient {

    private static final String BASE_URL = "http://localhost:8080/api";
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<StudyGroupItem> getGroups(String jwtToken) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/groups"))
                .header("Authorization", "Bearer " + jwtToken)
                .GET()
                .build();

        HttpResponse<String> response =
                httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Failed to load groups: " +
                    response.statusCode() + " " + response.body());
        }

        List<StudyGroupItem> result = new ArrayList<>();
        JsonNode arr = objectMapper.readTree(response.body());

        if (arr.isArray()) {
            for (JsonNode node : arr) {
                // prispôsobíme sa podľa toho, ako sa entity volajú
                Long id = null;

                if (node.has("id")) {
                    id = node.get("id").asLong();
                } else if (node.has("groupId")) {
                    id = node.get("groupId").asLong();
                }

                String name = node.has("name") ? node.get("name").asText() : "";
                String description = node.has("description") ? node.get("description").asText() : "";

                result.add(new StudyGroupItem(id, name, description));
            }
        }

        return result;
    }

    public StudyGroupItem createGroup(String jwtToken, String name, String description) throws Exception {
        JsonNode body = objectMapper.createObjectNode()
                .put("name", name)
                .put("description", description);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/groups"))
                .header("Authorization", "Bearer " + jwtToken)
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

        Long id = null;
        if (node.has("id")) {
            id = node.get("id").asLong();
        } else if (node.has("groupId")) {
            id = node.get("groupId").asLong();
        }

        String gName = node.has("name") ? node.get("name").asText() : name;
        String gDesc = node.has("description") ? node.get("description").asText() : description;

        return new StudyGroupItem(id, gName, gDesc);
    }
}
