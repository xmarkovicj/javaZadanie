package com.markovic.javazadanie.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class GroupStatsDto {
    private Long groupId;
    private String groupName;

    private long totalTasks;
    private long openTasks;
    private long submittedTasks;
    private long closedTasks;

    private double completionRate; // 0–100 %

    // userEmail -> pocet submissions
    private Map<String, Long> memberSubmissions;

    // napr. day -> count (na graf aktivity)
    private Map<String, Long> activityPerDay;
}
