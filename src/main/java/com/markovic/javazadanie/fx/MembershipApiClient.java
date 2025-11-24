package com.markovic.javazadanie.fx;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.markovic.javazadanie.fx.dto.MemberDto;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class MembershipApiClient {

    private static final String BASE_URL = "http://localhost:8080/api/groups";
    private final ObjectMapper mapper = new ObjectMapper();

    // volá existujúci backendový endpoint:
    // POST /api/groups/{groupId}/members/{userId}
    public void joinGroup(String token, Long groupId) {
        Long userId = SessionManager.getInstance().getUserId();
        if (userId == null) {
            throw new RuntimeException("No userId in SessionManager – set it after login.");
        }

        HttpURLConnection conn = null;
        try {
            URL url = new URL(BASE_URL + "/" + groupId + "/members/" + userId);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + token);
            conn.setDoOutput(false); // role posielaš ako body len ak chceš, my pošleme null

            int status = conn.getResponseCode();
            if (status >= 200 && status < 300) {
                return; // OK, člen pridaný
            }

            InputStream err = conn.getErrorStream();
            String body = err != null ? new String(err.readAllBytes(), StandardCharsets.UTF_8) : "";
            throw new RuntimeException("Join failed: " + status + " " + body);

        } catch (IOException e) {
            throw new RuntimeException("Join failed", e);
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    // DELETE /api/groups/{groupId}/members/{userId}
    public void leaveGroup(String token, Long groupId) {
        Long userId = SessionManager.getInstance().getUserId();
        if (userId == null) {
            throw new RuntimeException("No userId in SessionManager – set it after login.");
        }

        HttpURLConnection conn = null;
        try {
            URL url = new URL(BASE_URL + "/" + groupId + "/members/" + userId);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("DELETE");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + token);

            int status = conn.getResponseCode();
            if (status == HttpURLConnection.HTTP_NO_CONTENT || status == HttpURLConnection.HTTP_OK) {
                return; // OK, člen odobratý
            }

            InputStream err = conn.getErrorStream();
            String body = err != null ? new String(err.readAllBytes(), StandardCharsets.UTF_8) : "";
            throw new RuntimeException("Leave failed: " + status + " " + body);

        } catch (IOException e) {
            throw new RuntimeException("Leave failed", e);
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    // GET /api/groups/{groupId}/members
    public List<MemberDto> getMembers(String token, Long groupId) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(BASE_URL + "/" + groupId + "/members");
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + token);

            int status = conn.getResponseCode();
            InputStream is = (status >= 200 && status < 300)
                    ? conn.getInputStream()
                    : conn.getErrorStream();

            String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);

            if (status >= 200 && status < 300) {
                // pozor: backend vracia List<Membership>,
                // ale my mapujeme na MemberDto – tu budeš možno potrebovať upraviť backend,
                // aby rovno vracal DTO, alebo prispôsobiť MemberDto poliám Membershipu.
                return mapper.readValue(body, new TypeReference<List<MemberDto>>() {});
            } else {
                throw new RuntimeException("Failed to load members: " + status + " " + body);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load members", e);
        } finally {
            if (conn != null) conn.disconnect();
        }
    }
}
