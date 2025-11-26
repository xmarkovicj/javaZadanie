package com.markovic.javazadanie.fx;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class AuthApiClient {

    private static final String BASE_URL = "http://localhost:8080/api/auth";
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Zavolá POST /api/auth/login, naplní SessionManager a vráti JWT token.
     */
    public String login(String email, String password) throws Exception {
        // JSON body
        String json = objectMapper.createObjectNode()
                .put("email", email)
                .put("password", password)
                .toString();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response =
                httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            JsonNode node = objectMapper.readTree(response.body());

            // token z LoginResponseDto
            String token = node.path("token").asText(null);
            if (token == null || token.isBlank()) {
                throw new RuntimeException("Login failed: token missing in response: " + response.body());
            }

            // userId z LoginResponseDto (ak je)
            if (node.hasNonNull("userId")) {
                long userId = node.get("userId").asLong();
                SessionManager.getInstance().setUserId(userId);
            }

            // email z LoginResponseDto (ak je), inak použijeme ten z formulára
            String respEmail = email;
            if (node.hasNonNull("email")) {
                respEmail = node.get("email").asText();
            }

            SessionManager sm = SessionManager.getInstance();
            sm.setToken(token);
            sm.setUserEmail(respEmail);

            return token;
        } else {
            throw new RuntimeException("Login failed: "
                    + response.statusCode() + " " + response.body());
        }
    }

    public void register(String name, String email, String password) throws Exception {
        String json = objectMapper.createObjectNode()
                .put("name", name)
                .put("email", email)
                .put("password", password)
                .toString();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/register"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response =
                httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200 && response.statusCode() != 201) {
            throw new RuntimeException("Register failed: "
                    + response.statusCode() + " " + response.body());
        }
    }
}
