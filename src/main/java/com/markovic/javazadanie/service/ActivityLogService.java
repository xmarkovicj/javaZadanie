package com.markovic.javazadanie.service;

import com.markovic.javazadanie.model.ActivityAction;
import com.markovic.javazadanie.model.ActivityLog;
import com.markovic.javazadanie.model.User;
import com.markovic.javazadanie.repository.ActivityLogRepository;
import com.markovic.javazadanie.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ActivityLogService {

    private final ActivityLogRepository activityLogRepository;
    private final UserRepository userRepository;

    public ActivityLog log(Long userId, String action, String details) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        ActivityLog log = ActivityLog.builder()
                .user(user)
                .action(action)
                .details(details)
                .timestamp(LocalDateTime.now())
                .build();

        return activityLogRepository.save(log);
    }

    public ActivityLog log(Long userId, ActivityAction action, String details) {
        return log(userId, action.name(), details);
    }

    public List<ActivityLog> getAll() {
        return activityLogRepository.findAll();
    }

    public List<ActivityLog> getByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        return activityLogRepository.findByUser(user);
    }
}
