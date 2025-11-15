package com.markovic.javazadanie.controller;

import com.markovic.javazadanie.model.Resource;
import com.markovic.javazadanie.service.ResourceService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/resources")
@RequiredArgsConstructor
public class ResourceController {

    private final ResourceService resourceService;

    // CREATE
    @PostMapping
    public ResponseEntity<Resource> create(@RequestBody CreateResourceRequest req) {
        Resource r = resourceService.create(
                req.getGroupId(),
                req.getUploadedBy(),
                req.getTitle(),
                req.getType(),
                req.getPathOrUrl()
        );
        return ResponseEntity.ok(r);
    }

    // READ ALL
    @GetMapping
    public ResponseEntity<List<Resource>> getAll() {
        return ResponseEntity.ok(resourceService.getAll());
    }

    // READ ONE
    @GetMapping("/{id}")
    public ResponseEntity<Resource> getOne(@PathVariable Long id) {
        return resourceService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // BY GROUP
    @GetMapping("/group/{groupId}")
    public ResponseEntity<List<Resource>> getByGroup(@PathVariable Long groupId) {
        return ResponseEntity.ok(resourceService.getByGroup(groupId));
    }

    // UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<Resource> update(@PathVariable Long id,
                                           @RequestBody UpdateResourceRequest req) {
        return resourceService.update(id, req.getTitle(), req.getType(), req.getPathOrUrl())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return resourceService.delete(id) ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    // DTOs
    @Data
    public static class CreateResourceRequest {
        private Long groupId;
        private Long uploadedBy;
        private String title;
        private String type;
        private String pathOrUrl;
    }

    @Data
    public static class UpdateResourceRequest {
        private String title;
        private String type;
        private String pathOrUrl;
    }
}
