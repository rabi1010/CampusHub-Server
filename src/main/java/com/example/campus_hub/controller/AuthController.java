package com.example.campus_hub.controller;

import com.example.campus_hub.dto.*;
import com.example.campus_hub.entity.User;
import com.example.campus_hub.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(
        origins = "${app.frontend.url}",
        allowCredentials = "true"
)
public class AuthController {

    private final AuthService authService;

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
                        .body(ApiResponse.error("This email is already registered"));
            }
            if (e.getMessage() != null &&
                    e.getMessage().startsWith("STUDENT_NOT_FOUND")) {
                String rollNo = e.getMessage().contains(":")
                        ? e.getMessage().split(":")[1]
                        : "unknown";
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error(
                                "No student found with roll number: " + rollNo
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
    // GET /api/users/pending
    @GetMapping("/users/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<User>>> getPendingUsers() {
        List<User> users = authService.getPendingUsers();
        return ResponseEntity.ok(
                ApiResponse.success("Pending users fetched", users)
        );
    }

    @PatchMapping("/users/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<User>> approveUser(
            @PathVariable String id,
            @RequestBody ApproveUserRequest request
    ) {
        if (request.getRole() == null ||
                (!request.getRole().equals("STUDENT") &&
                        !request.getRole().equals("TEACHER") &&
                        !request.getRole().equals("PARENT"))) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Role must be STUDENT, TEACHER, or PARENT"));
        }

        try {
            User user = authService.approveUser(id, request);
            return ResponseEntity.ok(
                    ApiResponse.success("User approved", user)
            );
        } catch (RuntimeException e) {
            return switch (e.getMessage()) {
                case "USER_NOT_FOUND" ->
                        ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body(ApiResponse.error("User not found"));
                case "ALREADY_APPROVED" ->
                        ResponseEntity.status(HttpStatus.CONFLICT)
                                .body(ApiResponse.error("User already approved"));
                case "STUDENT_DETAILS_REQUIRED" ->
                        ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(ApiResponse.error(
                                        "rollNo, departmentId, batchId required for STUDENT"
                                ));
                case "ROLL_NO_TAKEN" ->
                        ResponseEntity.status(HttpStatus.CONFLICT)
                                .body(ApiResponse.error("Roll number already taken"));
                case "TEACHER_DETAILS_REQUIRED" ->                          // ← add
                        ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(ApiResponse.error(
                                        "employeeId and departmentId required for TEACHER"
                                ));
                case "EMPLOYEE_ID_TAKEN" ->                                 // ← add
                        ResponseEntity.status(HttpStatus.CONFLICT)
                                .body(ApiResponse.error("Employee ID already exists"));
                case "PARENT_DETAILS_REQUIRED" ->                          // ← add this too
                        ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(ApiResponse.error(
                                        "studentIds required for PARENT"
                                ));
                default -> throw e;
            };
        }
    }


}