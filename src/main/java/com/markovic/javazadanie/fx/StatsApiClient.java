package com.markovic.javazadanie.fx;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

public class StatsApiClient {

    private static final String BASE_URL = "http://localhost:8080/api";
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public GroupStats loadGroupStats(String token, Long groupId) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/stats/groups/" + groupId))
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();

        HttpResponse<String> response =
                httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Failed to load stats: " +
                    response.statusCode() + " " + response.body());
        }

        JsonNode root = objectMapper.readTree(response.body());

        GroupStats stats = new GroupStats();
        stats.groupId = root.get("groupId").asLong();
        stats.groupName = root.get("groupName").asText();
        stats.totalTasks = root.get("totalTasks").asLong();
        stats.openTasks = root.get("openTasks").asLong();
        stats.submittedTasks = root.get("submittedTasks").asLong();
        stats.closedTasks = root.get("closedTasks").asLong();
        stats.completionRate = root.get("completionRate").asDouble();

        Map<String, Long> memberSubs = new HashMap<>();
        JsonNode ms = root.get("memberSubmissions");
        if (ms != null && ms.isObject()) {
            ms.fields().forEachRemaining(e ->
                    memberSubs.put(e.getKey(), e.getValue().asLong())
            );
        }
        stats.memberSubmissions = memberSubs;

        Map<String, Long> activity = new HashMap<>();
        JsonNode act = root.get("activityPerDay");
        if (act != null && act.isObject()) {
            act.fields().forEachRemaining(e ->
                    activity.put(e.getKey(), e.getValue().asLong())
            );
        }
        stats.activityPerDay = activity;

        return stats;
    }

    public static class GroupStats {
        public Long groupId;
        public String groupName;

        public long totalTasks;
        public long openTasks;
        public long submittedTasks;
        public long closedTasks;
        public double completionRate;

        public Map<String, Long> memberSubmissions;
        public Map<String, Long> activityPerDay;
    }
}
