package com.markovic.javazadanie.controller;

import com.markovic.javazadanie.model.Comment;
import com.markovic.javazadanie.service.CommentService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    // CREATE
    @PostMapping
    public ResponseEntity<Comment> create(@RequestBody CreateCommentRequest req) {
        Comment c = commentService.create(req.getTaskId(), req.getAuthorId(), req.getContent());
        return ResponseEntity.ok(c);
    }

    // ALL
    @GetMapping
    public ResponseEntity<List<Comment>> getAll() {
        return ResponseEntity.ok(commentService.getAll());
    }

    // BY ID
    @GetMapping("/{id}")
    public ResponseEntity<Comment> getOne(@PathVariable Long id) {
        return commentService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // BY TASK
    @GetMapping("/task/{taskId}")
    public ResponseEntity<List<Comment>> getByTask(@PathVariable Long taskId) {
        return ResponseEntity.ok(commentService.getByTask(taskId));
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return commentService.delete(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    @Data
    public static class CreateCommentRequest {
        private Long taskId;
        private Long authorId;
        private String content;
    }
}
