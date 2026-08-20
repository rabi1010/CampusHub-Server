package com.example.campus_hub.controller;

import com.example.campus_hub.dto.ApiResponse;
import com.example.campus_hub.dto.CreateParentRequest;
import com.example.campus_hub.dto.UpdateParentRequest;
import com.example.campus_hub.dto.ParentResponse;
import com.example.campus_hub.entity.Parent;
import com.example.campus_hub.service.ParentService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/parents")
@CrossOrigin(origins = "${app.frontend.url}", allowCredentials = "true")
public class ParentController {

    private final ParentService parentService;

    public ParentController(ParentService parentService) {
        this.parentService = parentService;
    }

    // GET /api/parents
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<ParentResponse>>> getAll(
        @RequestParam(defaultValue = "1")  int    page,
        @RequestParam(defaultValue = "10") int    size,
        @RequestParam(defaultValue = "")   String search
    ) {
        return ResponseEntity.ok(
            ApiResponse.success(
                "Parents fetched",
                parentService.getAll(page, size, search)
            )
        );
    }

    // GET /api/parents/:id
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PARENT')")
    public ResponseEntity<ApiResponse<ParentResponse>> getById(
        @PathVariable String id
    ) {
        try {
            return ResponseEntity.ok(
                ApiResponse.success("Parent fetched", parentService.getById(id))
            );
        } catch (RuntimeException e) {
            if ("PARENT_NOT_FOUND".equals(e.getMessage())) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Parent not found"));
            }
            throw e;
        }
    }

    // GET /api/parents/me — parent views own profile
    @GetMapping("/me")
    @PreAuthorize("hasRole('PARENT')")
    public ResponseEntity<ApiResponse<ParentResponse>> getMe(
        Authentication auth
    ) {
        try {
            // auth.getName() returns email (set in JwtAuthFilter)
            ParentResponse parent = parentService.getByUserEmail(auth.getName());
            return ResponseEntity.ok(
                ApiResponse.success("Profile fetched", parent)
            );
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("Parent profile not found"));
        }
    }

    // POST /api/parents
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ParentResponse>> create(
        @Valid @RequestBody CreateParentRequest request
    ) {
        try {
            Parent parent = parentService.create(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Parent created", ParentResponse.from(parent)));
        } catch (RuntimeException e) {
            return switch (e.getMessage()) {
                case "EMAIL_TAKEN" ->
                    ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(ApiResponse.error("Email already registered"));
                default -> {
                    if (e.getMessage().startsWith("STUDENT_NOT_FOUND")) {
                        yield ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(ApiResponse.error("One or more students not found"));
                    }
                    throw e;
                }
            };
        }
    }

    // PUT /api/parents/:id
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ParentResponse>> update(
        @PathVariable String id,
        @RequestBody  UpdateParentRequest request
    ) {
        try {
            return ResponseEntity.ok(
                ApiResponse.success(
                    "Parent updated",
                    ParentResponse.from(parentService.update(id, request))
                )
            );
        } catch (RuntimeException e) {
            if ("PARENT_NOT_FOUND".equals(e.getMessage())) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Parent not found"));
            }
            throw e;
        }
    }

    // DELETE /api/parents/:id
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Object>> delete(
        @PathVariable String id
    ) {
        try {
            parentService.delete(id);
            return ResponseEntity.ok(
                ApiResponse.success("Parent deleted", null)
            );
        } catch (RuntimeException e) {
            if ("PARENT_NOT_FOUND".equals(e.getMessage())) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Parent not found"));
            }
            throw e;
        }
    }
}
