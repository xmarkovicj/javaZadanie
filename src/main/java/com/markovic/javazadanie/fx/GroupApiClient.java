package com.markovic.javazadanie.fx;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class GroupApiClient {

    private static final String BASE_URL = "http://localhost:8080/api/groups";
    private final ObjectMapper mapper = new ObjectMapper();

    public List<StudyGroupItem> getGroups(String token) throws IOException {
        URL url = new URL(BASE_URL);
        HttpURLConnection conn = createConnection(url, "GET", token);
        int status = conn.getResponseCode();
        InputStream is = status >= 200 && status < 300 ? conn.getInputStream() : conn.getErrorStream();
        String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);

        if (status >= 200 && status < 300) {
            return mapper.readValue(body, new TypeReference<List<StudyGroupItem>>() {});
        } else {
            throw new IOException("Failed to load groups: " + status + " " + body);
        }
    }

    public StudyGroupItem createGroup(String token, String name, String description) throws IOException {
        URL url = new URL(BASE_URL);
        HttpURLConnection conn = createConnection(url, "POST", token);
        conn.setDoOutput(true);

        String json = mapper.createObjectNode()
                .put("name", name)
                .put("description", description)
                .toString();

        conn.getOutputStream().write(json.getBytes(StandardCharsets.UTF_8));

        int status = conn.getResponseCode();
        InputStream is = status >= 200 && status < 300 ? conn.getInputStream() : conn.getErrorStream();
        String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);

        if (status >= 200 && status < 300) {
            return mapper.readValue(body, StudyGroupItem.class);
        } else {
            throw new IOException("Failed to create group: " + status + " " + body);
        }
    }

    public void joinGroup(String token, Long groupId) throws IOException {
        URL url = new URL(BASE_URL + "/" + groupId + "/join");
        HttpURLConnection conn = createConnection(url, "POST", token);
        conn.setDoOutput(true);

        int status = conn.getResponseCode();
        if (status < 200 || status >= 300) {
            String error = new String(conn.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);
            throw new IOException("Failed to join group: " + status + " " + error);
        }
    }

    public void leaveGroup(String token, Long groupId) throws IOException {
        URL url = new URL(BASE_URL + "/" + groupId + "/leave");
        HttpURLConnection conn = createConnection(url, "POST", token);
        conn.setDoOutput(true);

        int status = conn.getResponseCode();
        if (status < 200 || status >= 300) {
            String error = new String(conn.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);
            throw new IOException("Failed to leave group: " + status + " " + error);
        }
    }

    public void deleteGroup(String token, Long groupId) throws IOException {
        URL url = new URL(BASE_URL + "/" + groupId);
        HttpURLConnection conn = createConnection(url, "DELETE", token);

        int status = conn.getResponseCode();
        if (status < 200 || status >= 300) {
            String error = new String(conn.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);
            throw new IOException("Failed to delete group: " + status + " " + error);
        }
    }

    private HttpURLConnection createConnection(URL url, String method, String token) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(method);
        conn.setRequestProperty("Accept", "application/json");
        conn.setRequestProperty("Content-Type", "application/json");
        if (token != null && !token.isEmpty()) {
            conn.setRequestProperty("Authorization", "Bearer " + token);
        }
        return conn;
    }
}
