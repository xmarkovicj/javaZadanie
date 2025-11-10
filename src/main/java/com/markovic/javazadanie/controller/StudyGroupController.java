package com.markovic.javazadanie.controller;

import com.markovic.javazadanie.model.StudyGroup;
import com.markovic.javazadanie.service.StudyGroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
public class StudyGroupController {
    private final StudyGroupService groupService;

    //Create
    @PostMapping
    public ResponseEntity<StudyGroup> createGroup(@RequestBody StudyGroup group){
        return ResponseEntity.ok(groupService.create(group));
    }

    //READ ALL
    @GetMapping
    public ResponseEntity<List<StudyGroup>> getAll(){
        return ResponseEntity.ok(groupService.getAll());
    }

    //READ ONE
    @GetMapping("/{id}")
    public ResponseEntity<StudyGroup> getOne(@PathVariable Long id){
        return groupService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    //UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<StudyGroup> update(@PathVariable Long id, @RequestBody StudyGroup group){
        return groupService.update(id, group)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    //DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id){
        boolean deleted = groupService.delete(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }


}
