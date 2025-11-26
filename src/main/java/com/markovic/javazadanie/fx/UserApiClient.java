package com.markovic.javazadanie.fx;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.markovic.javazadanie.fx.dto.UserDto;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import java.util.List;
import java.util.ArrayList;
public class UserApiClient {

    private static final String BASE_URL = "http://localhost:8080/api/users";
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // DTO pre JavaFX
    public static class UserProfile {
        private Long id;
        private String name;
        private String email;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }

    // GET /api/users/me
    public UserProfile getMe(String token) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/me"))
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();

        HttpResponse<String> response =
                httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Failed to load profile: "
                    + response.statusCode() + " " + response.body());
        }

        JsonNode node = objectMapper.readTree(response.body());

        UserProfile p = new UserProfile();
        p.setId(node.has("id") ? node.get("id").asLong() : null);
        p.setName(node.has("name") ? node.get("name").asText() : "");
        p.setEmail(node.has("email") ? node.get("email").asText() : "");

        return p;
    }

    // PUT /api/users/me
    public UserProfile updateMe(String token,
                                String name,
                                String email,
                                String newPasswordOrNull) throws Exception {

        var body = objectMapper.createObjectNode();
        body.put("name", name);
        body.put("email", email);

        // heslo posielame len keď ho user zadal
        if (newPasswordOrNull != null && !newPasswordOrNull.isBlank()) {
            body.put("password", newPasswordOrNull);
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/me"))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        HttpResponse<String> response =
                httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Failed to update profile: "
                    + response.statusCode() + " " + response.body());
        }

        JsonNode node = objectMapper.readTree(response.body());

        UserProfile p = new UserProfile();
        p.setId(node.has("id") ? node.get("id").asLong() : null);
        p.setName(node.has("name") ? node.get("name").asText() : "");
        p.setEmail(node.has("email") ? node.get("email").asText() : "");

        return p;
    }
    public List<UserDto> getUsers(String token) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();

        HttpResponse<String> response =
                httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Failed to load users: "
                    + response.statusCode() + " " + response.body());
        }

        List<UserDto> list = new ArrayList<>();
        JsonNode arr = objectMapper.readTree(response.body());

        if (arr.isArray()) {
            for (JsonNode node : arr) {
                UserDto dto = new UserDto();
                if (node.hasNonNull("id")) {
                    dto.setId(node.get("id").asLong());
                }
                if (node.hasNonNull("name")) {
                    dto.setName(node.get("name").asText());
                }
                if (node.hasNonNull("email")) {
                    dto.setEmail(node.get("email").asText());
                }
                list.add(dto);
            }
        }

        return list;
    }
    public UserDto createUser(String token, String name, String email, String password) throws Exception {
        ObjectNode body = objectMapper.createObjectNode();
        body.put("name", name);
        body.put("email", email);
        body.put("password", password);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        HttpResponse<String> response =
                httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200 && response.statusCode() != 201) {
            throw new RuntimeException("Failed to create user: " +
                    response.statusCode() + " " + response.body());
        }

        JsonNode node = objectMapper.readTree(response.body());
        UserDto dto = new UserDto();
        dto.setId(node.has("id") ? node.get("id").asLong() : null);
        dto.setName(node.has("name") ? node.get("name").asText() : "");
        dto.setEmail(node.has("email") ? node.get("email").asText() : "");

        return dto;
    }
    public void deleteUser(String token, Long userId) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + userId))
                .header("Authorization", "Bearer " + token)
                .DELETE()
                .build();

        HttpResponse<String> response =
                httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 204 && response.statusCode() != 200) {
            throw new RuntimeException("Failed to delete user: " +
                    response.statusCode() + " " + response.body());
        }
    }



}
