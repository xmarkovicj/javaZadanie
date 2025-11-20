package com.markovic.javazadanie.controller;

import com.markovic.javazadanie.model.Membership;
import com.markovic.javazadanie.service.MembershipService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@RestController
@RequestMapping("/api/memberships")
@RequiredArgsConstructor
public class MembershipController {

    private final MembershipService membershipService;

    // CREATE – pridať používateľa do skupiny
    @PostMapping
    public ResponseEntity<Membership> addMember(@Valid @RequestBody AddMemberRequest req) {
        Membership m = membershipService.addMember(
                req.getGroupId(),
                req.getUserId(),
                req.getRole()
        );
        return ResponseEntity.ok(m);
    }

    // READ ALL
    @GetMapping
    public ResponseEntity<List<Membership>> getAll() {
        return ResponseEntity.ok(membershipService.getAll());
    }

    // READ ONE
    @GetMapping("/{id}")
    public ResponseEntity<Membership> getOne(@PathVariable Long id) {
        return membershipService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // BY GROUP
    @GetMapping("/group/{groupId}")
    public ResponseEntity<List<Membership>> getByGroup(@PathVariable Long groupId) {
        return ResponseEntity.ok(membershipService.getByGroup(groupId));
    }

    // BY USER
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Membership>> getByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(membershipService.getByUser(userId));
    }

    // UPDATE ROLE
    @PutMapping("/{id}/role")
    public ResponseEntity<Membership> updateRole(@PathVariable Long id,
                                                 @Valid @RequestBody UpdateRoleRequest req) {
        return membershipService.updateRole(id, req.getRole())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // DELETE – odstrániť membera zo skupiny
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return membershipService.remove(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    // DTOs

    @Data
    public static class AddMemberRequest {
        @NotNull
        private Long groupId;

        @NotNull
        private Long userId;

        @NotBlank
        private String role;
    }

    @Data
    public static class UpdateRoleRequest {
        @NotBlank
        private String role;
    }
}
