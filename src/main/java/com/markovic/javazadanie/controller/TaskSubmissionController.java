package com.markovic.javazadanie.controller;

import com.markovic.javazadanie.model.TaskSubmission;
import com.markovic.javazadanie.model.User;
import com.markovic.javazadanie.service.TaskSubmissionService;
import com.markovic.javazadanie.service.UserService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskSubmissionController {

    private final TaskSubmissionService submissionService;
    private final UserService userService;

    // POST /api/tasks/{taskId}/submissions  – odoslanie riešenia
    @PostMapping("/{taskId}/submissions")
    public ResponseEntity<TaskSubmission> submit(
            @PathVariable Long taskId,
            @RequestBody SubmitRequest req,
            Authentication authentication
    ) {
        // z Authentication si vytiahneme aktuálneho používateľa
        String email = authentication.getName();
        User user = userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));

        TaskSubmission saved = submissionService.create(
                taskId,
                user.getId(),
                req.getContent(),
                req.getAttachmentUrl()
        );

        return ResponseEntity.status(201).body(saved);
    }

    // GET /api/tasks/{taskId}/submissions/my – moje riešenie pre danú úlohu
    @GetMapping("/{taskId}/submissions/my")
    public ResponseEntity<TaskSubmission> getMySubmission(
            @PathVariable Long taskId,
            Authentication authentication
    ) {
        String email = authentication.getName();
        User user = userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));

        return submissionService.findByTaskAndUser(taskId, user.getId())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Voliteľné: GET /api/tasks/{taskId}/submissions – všetky odovzdania pre úlohu
    @GetMapping("/{taskId}/submissions")
    public ResponseEntity<List<TaskSubmission>> getByTask(@PathVariable Long taskId) {
        return ResponseEntity.ok(submissionService.getByTask(taskId));
    }

    @Data
    public static class SubmitRequest {
        @NotBlank
        private String content;

        private String attachmentUrl; // môže byť null
    }
}
