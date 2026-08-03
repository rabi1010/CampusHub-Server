package com.example.campus_hub.controller;

import com.example.campus_hub.dto.*;
import com.example.campus_hub.entity.User;
import com.example.campus_hub.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(
        origins = "${app.frontend.url}",
        allowCredentials = "true"
)
public class AuthController {

    private final AuthService authService;

    // POST /api/auth/register
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Object>> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        try {
            User user = authService.register(request);
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(ApiResponse.success(
                            "Account created. Please wait for admin approval.",
                            user
                    ));
        } catch (RuntimeException e) {
            if ("EMAIL_TAKEN".equals(e.getMessage())) {
                return ResponseEntity
                        .status(HttpStatus.CONFLICT)
                        .body(ApiResponse.error(
                                "This email is already registered"
                        ));
            }
            throw e;
        }
    }

    // POST /api/auth/login
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request
    ) {
        try {
            AuthResponse response = authService.login(request);
            return ResponseEntity.ok(
                    ApiResponse.success("Login successful", response)
            );
        } catch (RuntimeException e) {
            return switch (e.getMessage()) {
                case "INVALID_CREDENTIALS" ->
                        ResponseEntity
                                .status(HttpStatus.UNAUTHORIZED)
                                .body(ApiResponse.error(
                                        "Invalid email or password"
                                ));
                case "ACCOUNT_PENDING" ->
                        ResponseEntity
                                .status(HttpStatus.FORBIDDEN)
                                .body(ApiResponse.error(
                                        "Account pending approval. " +
                                                "Please wait for admin."
                                ));
                case "ACCOUNT_SUSPENDED" ->
                        ResponseEntity
                                .status(HttpStatus.FORBIDDEN)
                                .body(ApiResponse.error(
                                        "Account suspended. Contact admin."
                                ));
                default -> throw e;
            };
        }
    }

    // POST /api/auth/logout
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Object>> logout() {
        return ResponseEntity.ok(
                ApiResponse.success("Logged out successfully", null)
        );
    }

    // GET /api/auth/me
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<User>> me(
            Authentication auth
    ) {
        User user = authService.me(auth.getName());
        return ResponseEntity.ok(
                ApiResponse.success("User fetched", user)
        );
    }
}