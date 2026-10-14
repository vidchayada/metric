/*
 * Copyright (c) 2026 Vidchayada
 * Licensed under the MIT License.
 */

package metric.backend.controller;

import jakarta.validation.Valid;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import metric.backend.dto.AuthResponse;
import metric.backend.dto.LoginRequest;
import metric.backend.entity.User;
import metric.backend.repository.UserRepository;
import metric.backend.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final String MESSAGE = "message"; // fix for S1192 (duplicated 3x)
    private static final String ERROR = "error"; // fix for S1192 (duplicated 2x)
    private static final String USERNAME = "username";

    private final AuthService authService;
    private final UserRepository userRepository;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody Map<String, String> body) {
        String username = body.get(USERNAME);
        String email = body.get("email");
        String password = body.get("password");

        if (username == null || username.trim().isEmpty() ||
                email == null || email.trim().isEmpty() ||
                password == null || password.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(MESSAGE, "All fields are required."));
        }

        try {
            AuthResponse res = authService.register(username.trim(), email.trim(), password);
            return ResponseEntity.ok(Map.of(
                    MESSAGE, "User registered successfully",
                    "token", res.getToken(),
                    USERNAME, res.getUsername()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(MESSAGE, e.getMessage()));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser(
            @AuthenticationPrincipal Object principal,
            Principal authPrincipal) {

        String username = null;
        if (principal instanceof User u) {
            username = u.getUsername();
        } else if (authPrincipal != null) {
            username = authPrincipal.getName();
        }

        if (username == null) {
            return ResponseEntity.status(401).body(Map.of(ERROR, "Unauthorized"));
        }

        return userRepository.findByUsername(username)
                .map(u -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("id", u.getUserId());
                    response.put("userId", u.getUserId());
                    response.put(USERNAME, u.getUsername());
                    response.put("email", u.getEmail());
                    response.put("role", u.getRole());
                    return ResponseEntity.ok(response);
                })
                .orElseGet(() -> ResponseEntity.status(401).body(Map.of(ERROR, "User not found")));
    }
}