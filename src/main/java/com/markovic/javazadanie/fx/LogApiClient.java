package com.markovic.javazadanie.fx;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.markovic.javazadanie.fx.dto.ActionLogDto;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class LogApiClient {

    private static final String BASE_URL = "http://localhost:8080/api/activity-log";
    private final ObjectMapper mapper = new ObjectMapper();

    public List<ActionLogDto> getLogs(String token) {
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
                return mapper.readValue(body, new TypeReference<List<ActionLogDto>>() {});
            } else {
                throw new RuntimeException("Failed to load logs: " + status + " " + body);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load logs", e);
        }
    }
}
