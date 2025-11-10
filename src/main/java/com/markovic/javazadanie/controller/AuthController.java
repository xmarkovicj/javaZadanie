package com.markovic.javazadanie.controller;

import com.markovic.javazadanie.model.User;
import com.markovic.javazadanie.service.UserService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;



@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody RegisterRequest request) {
        User u = new User();
        u.setName(request.getName());
        u.setEmail(request.getEmail());
        u.setPassword(request.getPassword());
        User saved = userService.register(u);
        return ResponseEntity.ok(saved);
    }

    @PostMapping("/login")
    public ResponseEntity<User> login(@RequestBody LoginRequest request)
    {
        User user = userService.login(request.getEmail(), request.getPassword());
        return ResponseEntity.ok(user);
    }

    @Data
    static class RegisterRequest {
        private String name;
        private String email;
        private String password;
    }

    @Data
    static class LoginRequest {
        private String email;
        private String password;
    }
}
