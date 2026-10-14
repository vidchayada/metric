/*
 * Copyright (c) 2026 Vidchayada
 * Licensed under the MIT License.
 */

package metric.backend.service;

import lombok.RequiredArgsConstructor;
import metric.backend.dto.AuthResponse;
import metric.backend.dto.LoginRequest;
import metric.backend.entity.Role;
import metric.backend.entity.User;
import metric.backend.repository.UserRepository;
import metric.backend.security.JwtTokenProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Invalid username or password"));

        boolean isMatched = passwordEncoder.matches(request.getPassword(), user.getPasswordHash())
                || request.getPassword().equals(user.getPasswordHash());

        if (!isMatched) {
            throw new IllegalArgumentException("Invalid username or password");
        }

        String token = jwtTokenProvider.createToken(
                user.getUsername(),
                user.getRole().name(),
                user.getUserId()
        );

        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(user.getUserId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }

    @Transactional
    public AuthResponse register(String username, String email, String password) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }

        User newUser = User.builder()
                .username(username)
                .fullName(username)
                .email(email)
                .passwordHash(passwordEncoder.encode(password))
                .role(Role.user)
                .build();

        User savedUser = userRepository.save(newUser);

        String token = jwtTokenProvider.createToken(
                savedUser.getUsername(),
                savedUser.getRole().name(),
                savedUser.getUserId()
        );

        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(savedUser.getUserId())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .role(savedUser.getRole().name())
                .build();
    }
}