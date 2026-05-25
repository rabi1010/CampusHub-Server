package com.example.campus_hub.service;

import com.example.campus_hub.dto.*;
import com.example.campus_hub.entity.User;
import com.example.campus_hub.repository.UserRepository;
import com.example.campus_hub.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository  userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil         jwtUtil;
    private final EmailService    emailService;

    // ── Register ─────────────────────────────────────────
    public User register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("EMAIL_TAKEN");
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .role(User.Role.PENDING)
                .status(User.Status.PENDING)
                .build();

        User saved = userRepository.save(user);

        // Send email in background — doesn't block response
        emailService.sendRegistrationEmail(
                saved.getEmail(),
                saved.getFullName()
        );

        return saved;
    }

    // ── Login ─────────────────────────────────────────────
    public AuthResponse login(LoginRequest request) {

        User user = userRepository
                .findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new RuntimeException("INVALID_CREDENTIALS")
                );

        if (!passwordEncoder.matches(
                request.getPassword(), user.getPassword()
        )) {
            throw new RuntimeException("INVALID_CREDENTIALS");
        }

        if (user.getStatus() == User.Status.PENDING) {
            throw new RuntimeException("ACCOUNT_PENDING");
        }

        if (user.getStatus() == User.Status.SUSPENDED) {
            throw new RuntimeException("ACCOUNT_SUSPENDED");
        }

        String token = jwtUtil.generateToken(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getRole().name()
        );

        return AuthResponse.builder()
                .token(token)
                .user(AuthResponse.UserDTO.builder()
                        .id(user.getId())
                        .email(user.getEmail())
                        .fullName(user.getFullName())
                        .role(user.getRole().name())
                        .build())
                .build();
    }

    // ── Get current user ──────────────────────────────────
    public User me(String email) {
        return userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("USER_NOT_FOUND")
                );
    }
}