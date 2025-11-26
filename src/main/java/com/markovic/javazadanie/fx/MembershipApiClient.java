package com.markovic.javazadanie.fx;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.markovic.javazadanie.fx.dto.MemberDto;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class MembershipApiClient {

    private static final String BASE_URL = "http://localhost:8080/api/groups";
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void joinGroup(String jwtToken, Long groupId) throws Exception {
        if (jwtToken == null || jwtToken.isBlank()) {
            throw new RuntimeException("No JWT token – user is not logged in.");
        }

        URL url = new URL(BASE_URL + "/" + groupId + "/join");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Authorization", "Bearer " + jwtToken);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true); // aj keď neposielame body, POST to zvykne brať lepšie

        int code = conn.getResponseCode();

        if (code == 200 || code == 201) {
            // všetko ok – členstvo vytvorené
            conn.disconnect();
            return;
        }

        // načítame error body, aby si videl dôvod
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(conn.getErrorStream() != null ?
                        conn.getErrorStream() :
                        conn.getInputStream())
        )) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            throw new RuntimeException("Join failed: " + code + " " + sb);
        } finally {
            conn.disconnect();
        }
    }

    // LEAVE aktuálne prihláseného používateľa
    public void leaveGroup(String jwtToken, Long groupId) throws Exception {
        if (jwtToken == null || jwtToken.isBlank()) {
            throw new RuntimeException("No JWT token – user is not logged in.");
        }

        URL url = new URL(BASE_URL + "/" + groupId + "/leave");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Authorization", "Bearer " + jwtToken);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        int code = conn.getResponseCode();
        if (code == 200 || code == 204) {
            conn.disconnect();
            return;
        }

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(conn.getErrorStream() != null ?
                        conn.getErrorStream() :
                        conn.getInputStream())
        )) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            throw new RuntimeException("Leave failed: " + code + " " + sb);
        } finally {
            conn.disconnect();
        }
    }

    public List<MemberDto> getMembers(String jwt, Long groupId) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + groupId + "/members"))
                .header("Authorization", "Bearer " + jwt)
                .GET()
                .build();

        HttpResponse<String> response =
                httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Failed to load members");
        }

        String body = response.body();

        try {
            JsonNode root = objectMapper.readTree(body);
            List<MemberDto> result = new ArrayList<>();

            if (root.isArray()) {
                for (JsonNode mNode : root) {
                    MemberDto dto = new MemberDto();

                    // membershipId
                    if (mNode.hasNonNull("membershipId")) {
                        dto.setMembershipId(mNode.get("membershipId").asLong());
                    }

                    // user objekt vo vnútri membershipu
                    JsonNode userNode = mNode.path("user");
                    if (userNode.isObject()) {
                        if (userNode.hasNonNull("id")) {
                            dto.setId(userNode.get("id").asLong());
                        }
                        if (userNode.hasNonNull("name")) {
                            dto.setName(userNode.get("name").asText());
                        }
                        if (userNode.hasNonNull("email")) {
                            dto.setEmail(userNode.get("email").asText());
                        }
                    }

                    // role (je na root úrovni membershipu)
                    if (mNode.hasNonNull("role")) {
                        dto.setRole(mNode.get("role").asText());
                    }

                    result.add(dto);
                }
            }

            return result;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to load members", e);
        }
    }
}
