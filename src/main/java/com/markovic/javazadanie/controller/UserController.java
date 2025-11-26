package com.markovic.javazadanie.controller;

import com.markovic.javazadanie.model.User;
import com.markovic.javazadanie.repository.UserRepository;
import com.markovic.javazadanie.service.UserService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final UserService userService;

    // CREATE – admin vytvorí užívateľa
    // CREATE
    @PostMapping
    public User createUser(@Valid @RequestBody User user) {
        return userService.createUser(user);
    }
    // READ ALL
    @GetMapping
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // READ ONE podľa ID
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return userService.getUserById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // GET /api/users/me – aktuálny prihlásený používateľ
    @GetMapping("/me")
    public ResponseEntity<User> getMe(Authentication authentication) {
        String currentEmail = authentication.getName();

        User current = userService.findByEmail(currentEmail)
                .orElseThrow(() -> new RuntimeException("User not found: " + currentEmail));

        return ResponseEntity.ok(current);
    }

    // UPDATE podľa ID (admin upravuje hocikoho)
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id,
                                           @Valid @RequestBody UpdateProfileRequest req) {

        // poskladáme "partial" User, ktorý pôjde do service
        User u = new User();
        u.setName(req.getName());
        u.setEmail(req.getEmail());
        // heslo môže byť null/empty = nemeníme ho
        if (req.getPassword() != null && !req.getPassword().isBlank()) {
            u.setPassword(req.getPassword());
        }

        return userService.updateUser(id, u)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // UPDATE "me" – edit profilu prihláseného usera
    @PutMapping("/me")
    public ResponseEntity<User> updateMe(
            @Valid @RequestBody UpdateProfileRequest req,
            Authentication authentication
    ) {
        String currentEmail = authentication.getName();

        User current = userService.findByEmail(currentEmail)
                .orElseThrow(() -> new RuntimeException("User not found: " + currentEmail));

        User u = new User();
        u.setName(req.getName());
        u.setEmail(req.getEmail());
        if (req.getPassword() != null && !req.getPassword().isBlank()) {
            u.setPassword(req.getPassword());
        }

        return userService.updateUser(current.getId(), u)
                .map(ResponseEntity::ok)
                .orElseThrow(() ->
                        new RuntimeException("User not found by id: " + current.getId()));
    }

    // DELETE podľa ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        boolean deleted = userService.deleteUser(id);
        return deleted ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    @Data
    public static class UpdateProfileRequest {
        @NotBlank
        private String name;

        @NotBlank
        @Email
        private String email;

        // nepovinné – keď je null/empty, heslo sa nemení
        private String password;
    }
}
