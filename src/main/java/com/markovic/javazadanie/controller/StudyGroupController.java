package com.markovic.javazadanie.controller;

import com.markovic.javazadanie.model.StudyGroup;
import com.markovic.javazadanie.model.User;
import com.markovic.javazadanie.repository.UserRepository;
import com.markovic.javazadanie.service.StudyGroupService;
import com.markovic.javazadanie.model.Membership;
import com.markovic.javazadanie.service.MembershipService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import javax.validation.Valid;
import java.util.List;


@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
public class StudyGroupController {
    private final StudyGroupService groupService;
    private final MembershipService membershipService;
    private final  UserRepository userRepository;

    //Create
    @PostMapping
    public ResponseEntity<StudyGroup> createGroup(@Valid @RequestBody StudyGroup group){
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
    public ResponseEntity<StudyGroup> update(@Valid @PathVariable Long id,@Valid @RequestBody StudyGroup group){
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


    @PostMapping("/{groupId}/members/{userId}")
    public ResponseEntity<Membership> addMember(@PathVariable Long groupId, @PathVariable Long userId, @RequestBody(required = false) String role){
        Membership m = membershipService.addMember(userId, groupId, role);
        return ResponseEntity.ok(m);
    }

    @GetMapping("/{groupId}/members")
    public ResponseEntity<List<Membership>> getMembers(@PathVariable Long groupId){
        return ResponseEntity.ok(membershipService.getByGroup(groupId));
    }
    @DeleteMapping("/{groupId}/members/{userId}")
    public ResponseEntity<Void> removeMember(@PathVariable Long groupId,
                                             @PathVariable Long userId) {
        boolean removed = membershipService.removeMember(userId, groupId);
        return removed ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    @PostMapping("/{groupId}/join")
    public ResponseEntity<Membership> joinGroupForCurrentUser(
            @PathVariable Long groupId,
            Authentication authentication
    ) {
        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));

        Membership m = membershipService.addMember(groupId, user.getId(), "MEMBER");
        return ResponseEntity.ok(m);
    }

    @PostMapping("/{groupId}/leave")
    public ResponseEntity<Void> leaveGroupForCurrentUser(
            @PathVariable Long groupId,
            Authentication authentication
    ) {
        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));

        membershipService.removeMember(groupId,user.getId());
        return ResponseEntity.noContent().build();
    }



}
