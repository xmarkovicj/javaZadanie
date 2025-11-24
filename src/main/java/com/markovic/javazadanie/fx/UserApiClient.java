package com.markovic.javazadanie.fx;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.markovic.javazadanie.fx.dto.UserDto;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class UserApiClient {

    private static final String BASE_URL = "http://localhost:8080/api/users";
    private final ObjectMapper mapper = new ObjectMapper();

    public List<UserDto> getUsers(String token) {
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
                return mapper.readValue(body, new TypeReference<List<UserDto>>() {});
            } else {
                throw new RuntimeException("Failed to load users: " + status + " " + body);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load users", e);
        }
    }

    public UserDto createUser(String token, String name, String email, String password) {
        try {
            URL url = new URL(BASE_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + token);
            conn.setDoOutput(true);

            String json = mapper.createObjectNode()
                    .put("name", name)
                    .put("email", email)
                    .put("password", password)
                    .toString();

            conn.getOutputStream().write(json.getBytes(StandardCharsets.UTF_8));

            int status = conn.getResponseCode();
            String body = new String(
                    (status >= 200 && status < 300 ? conn.getInputStream() : conn.getErrorStream())
                            .readAllBytes(),
                    StandardCharsets.UTF_8
            );

            if (status >= 200 && status < 300) {
                return mapper.readValue(body, UserDto.class);
            } else {
                throw new RuntimeException("Failed to create user: " + status + " " + body);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to create user", e);
        }
    }

    public void deleteUser(String token, Long id) {
        try {
            URL url = new URL(BASE_URL + "/" + id);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("DELETE");
            conn.setRequestProperty("Authorization", "Bearer " + token);

            int status = conn.getResponseCode();
            if (status < 200 || status >= 300) {
                String body = new String(conn.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);
                throw new RuntimeException("Failed to delete user: " + status + " " + body);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete user", e);
        }
    }
}
