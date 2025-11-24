package com.markovic.javazadanie.fx;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.markovic.javazadanie.fx.dto.GroupDto;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class GroupApiClient {

    private static final String BASE_URL = "http://localhost:8080/api/groups";
    private final ObjectMapper mapper = new ObjectMapper();

    public List<GroupDto> getAllGroups(String token) {
        try {
            URL url = new URL(BASE_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + token);

            int status = conn.getResponseCode();
            InputStream is = (status >= 200 && status < 300)
                    ? conn.getInputStream()
                    : conn.getErrorStream();

            String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);

            if (status >= 200 && status < 300) {
                return mapper.readValue(body, new TypeReference<List<GroupDto>>() {});
            } else {
                throw new RuntimeException("Failed to load groups: " + status + " " + body);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load groups", e);
        }
    }

    public void deleteGroup(String token, Long groupId) {
        try {
            URL url = new URL(BASE_URL + "/" + groupId);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("DELETE");
            conn.setRequestProperty("Authorization", "Bearer " + token);

            int status = conn.getResponseCode();
            if (status < 200 || status >= 300) {
                String body = new String(conn.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);
                throw new RuntimeException("Failed to delete group: " + status + " " + body);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete group", e);
        }
    }
}
