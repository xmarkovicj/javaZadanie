package com.markovic.javazadanie.controller;

import com.markovic.javazadanie.model.ActivityAction;
import com.markovic.javazadanie.model.User;
import com.markovic.javazadanie.repository.UserRepository;
import com.markovic.javazadanie.security.JwtUtil;
import com.markovic.javazadanie.service.ActivityLogService;
import com.markovic.javazadanie.service.UserService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.AuthenticationException;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;




@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserService userService;
    private final ActivityLogService activityLogService;
    private final UserRepository userRepository;

    @PostMapping("/register")
    public ResponseEntity<User> register(@Valid @RequestBody RegisterRequest req) {
        User u = new User();
        u.setName(req.getName());
        u.setEmail(req.getEmail());
        u.setPassword(req.getPassword()); // UserService.register ho zahashuje

        User saved = userService.register(u);

        // 🔔 LOG: registrácia
        activityLogService.log(
                saved.getId(),
                ActivityAction.USER_REGISTERED,
                "User registered with email " + saved.getEmail()
        );

        return ResponseEntity.ok(saved);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req) {
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword())
            );

            String jwt = jwtUtil.generateToken(((UserDetails) auth.getPrincipal()).getUsername());

            // nájdeme usera kvôli logu
            User user = userRepository.findByEmail(req.getEmail());
            if (user != null) {
                activityLogService.log(
                        user.getId(),
                        ActivityAction.USER_LOGIN_SUCCESS,
                        "User logged in successfully"
                );
            }

            return ResponseEntity.ok(new AuthResponse(jwt));

        } catch (AuthenticationException ex) {
            // ak existuje user s týmto emailom, zalogujeme neúspešný pokus
            User user = userRepository.findByEmail(req.getEmail());
            if (user != null) {
                activityLogService.log(
                        user.getId(),
                        ActivityAction.USER_LOGIN_FAILED,
                        "Failed login attempt"
                );
            }
            return ResponseEntity.status(401).body("Invalid credentials");
        }
    }

    @Data
    public static class RegisterRequest {
        @NotBlank
        private String name;
        @NotBlank
        @Email
        private String email;
        @NotBlank
        @Size(min = 6, message = "Password at least 6 chars")
        private String password;
    }

    @Data
    public static class LoginRequest {
        @NotBlank
        @Email
        private String email;

        @NotBlank
        private String password;
    }

    @Data
    public static class AuthResponse {
        private final String token;
    }
}
