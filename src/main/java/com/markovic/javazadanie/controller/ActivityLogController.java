package com.markovic.javazadanie.controller;

import com.markovic.javazadanie.model.ActivityLog;
import com.markovic.javazadanie.service.ActivityLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/activity-log")
@RequiredArgsConstructor
public class ActivityLogController {

    private final ActivityLogService activityLogService;

    @GetMapping
    public ResponseEntity<List<ActivityLog>> getAll() {
        return ResponseEntity.ok(activityLogService.getAll());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ActivityLog>> getByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(activityLogService.getByUser(userId));
    }
}
