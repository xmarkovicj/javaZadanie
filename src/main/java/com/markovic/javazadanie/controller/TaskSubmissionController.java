package com.markovic.javazadanie.controller;

import com.markovic.javazadanie.model.TaskSubmission;
import com.markovic.javazadanie.model.User;
import com.markovic.javazadanie.service.TaskSubmissionService;
import com.markovic.javazadanie.service.UserService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
import java.util.HashMap;



@RestController
@RequestMapping("/api/submissions")
@RequiredArgsConstructor
public class TaskSubmissionController {

    private final TaskSubmissionService submissionService;
    private final UserService userService;

    // CREATE (odovzdanie úlohy)
    @PostMapping
    public ResponseEntity<TaskSubmission> create(@Valid @RequestBody CreateSubmissionRequest req,
                                                 @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        TaskSubmission submission = submissionService.create(
                req.getTaskId(),
                user.getId(),               // sem dáme id prihláseného usera
                req.getContent(),
                "PENDING"
        );

        return ResponseEntity.ok(submission);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleIllegalState(IllegalStateException ex) {
        Map<String, String> body = new HashMap<>();
        body.put("error", ex.getMessage());
        return ResponseEntity.status(409).body(body);
    }


    // READ ALL
    @GetMapping
    public ResponseEntity<List<TaskSubmission>> getAll() {
        return ResponseEntity.ok(submissionService.getAll());
    }

    // READ ONE
    @GetMapping("/{id}")
    public ResponseEntity<TaskSubmission> getOne(@PathVariable Long id) {
        return submissionService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // BY TASK
    @GetMapping("/task/{taskId}")
    public ResponseEntity<List<TaskSubmission>> getByTask(@PathVariable Long taskId) {
        return ResponseEntity.ok(submissionService.getByTask(taskId));
    }

    // BY USER
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<TaskSubmission>> getByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(submissionService.getByUser(userId));
    }

    // GRADE
    @PutMapping("/{id}/grade")
    public ResponseEntity<TaskSubmission> grade(@Valid @PathVariable Long id,
                                               @Valid @RequestBody GradeRequest req) {
        return submissionService.grade(id, req.getGrade(), req.getFeedback())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return submissionService.delete(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
    @GetMapping("/my")
    public ResponseEntity<TaskSubmission> getMySubmission(
            @RequestParam Long taskId,
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = userService.findByEmail(userDetails.getUsername()).orElseThrow(() -> new RuntimeException("User not found"));

        return submissionService.findByTaskAndUser(taskId, user.getId())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }


    // DTOs
    @Data
    public static class CreateSubmissionRequest {
        @NotNull
        private Long taskId;


        @NotBlank
        private String content;

        private String attachmentUrl;
    }

    @Data
    public static class GradeRequest {
        private Double grade;
        private String feedback;
    }
}
