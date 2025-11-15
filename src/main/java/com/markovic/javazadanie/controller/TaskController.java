package com.markovic.javazadanie.controller;

import com.markovic.javazadanie.model.Task;
import com.markovic.javazadanie.service.TaskService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {
    private final TaskService taskService;

    //CREATE
    @PostMapping
    public ResponseEntity<Task> create(@RequestBody CreateTaskRequest req){
        Task t = taskService.create(
                req.getGroupId(),
                req.getCreatedBy(),
                req.getTitle(),
                req.getDescription(),
                req.getStatus(),
                req.getDeadline()
        );
        return ResponseEntity.ok(t);
    }

    //READ ALL
    @GetMapping
    public ResponseEntity<List<Task>> getAll(){
        return ResponseEntity.ok(taskService.getAll());
    }

    //READ ONE
    @GetMapping("/{id}")
    public ResponseEntity<Task> getOne(@PathVariable Long id){
        return taskService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    //READ BY GROUP
    @GetMapping("/group/{groupId}")
    public ResponseEntity<List<Task>> getByGroup(@PathVariable Long groupId){
        return ResponseEntity.ok(taskService.getByGroup(groupId));
    }

    // UPDATE (title/description/status/deadline)
    @PutMapping("/{id}")
    public ResponseEntity<Task> update(@PathVariable Long id, @RequestBody UpdateTaskRequest req) {
        return taskService.update(id, req.getTitle(), req.getDescription(), req.getStatus(), req.getDeadline())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return taskService.delete(id) ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    @Data
    public static class CreateTaskRequest {
        private Long groupId;         // FK -> groups.group_id
        private Long createdBy;       // FK -> users.id
        private String title;
        private String description;
        private String status;        // napr. "OPEN" / "DONE"
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        private LocalDateTime deadline;
    }

    @Data
    public static class UpdateTaskRequest {
        private String title;
        private String description;
        private String status;
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        private LocalDateTime deadline;
    }
}

