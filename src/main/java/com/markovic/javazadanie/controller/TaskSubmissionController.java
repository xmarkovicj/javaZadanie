package com.markovic.javazadanie.controller;

import com.markovic.javazadanie.model.TaskSubmission;
import com.markovic.javazadanie.service.TaskSubmissionService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/submissions")
@RequiredArgsConstructor
public class TaskSubmissionController {

    private final TaskSubmissionService submissionService;

    // CREATE (odovzdanie úlohy)
    @PostMapping
    public ResponseEntity<TaskSubmission> create(@RequestBody CreateSubmissionRequest req) {
        TaskSubmission s = submissionService.create(
                req.getTaskId(),
                req.getUserId(),
                req.getContent(),
                req.getAttachmentUrl()
        );
        return ResponseEntity.ok(s);
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
    public ResponseEntity<TaskSubmission> grade(@PathVariable Long id,
                                                @RequestBody GradeRequest req) {
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

    // DTOs
    @Data
    public static class CreateSubmissionRequest {
        private Long taskId;
        private Long userId;
        private String content;
        private String attachmentUrl;
    }

    @Data
    public static class GradeRequest {
        private Double grade;
        private String feedback;
    }
}
