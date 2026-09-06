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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

    @PostMapping(value = "/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'PARENT')")
    public ResponseEntity<ApiResponse<Object>> uploadImage(
        @PathVariable String id, @RequestParam("image") MultipartFile file
    ) {
        try {
            parentService.uploadImage(id, file);
            return ResponseEntity.ok(ApiResponse.success("Image uploaded", null));
        } catch (RuntimeException e) {
            return switch (e.getMessage()) {
                case "PARENT_NOT_FOUND" -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("Parent not found"));
                case "INVALID_IMAGE_TYPE" -> ResponseEntity.badRequest().body(ApiResponse.error("Only JPEG, PNG, WebP allowed"));
                case "IMAGE_TOO_LARGE" -> ResponseEntity.badRequest().body(ApiResponse.error("Image must be under 2MB"));
                case "IMAGE_UPLOAD_FAILED" -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("Upload failed"));
                default -> throw e;
            };
        }
    }

    @GetMapping(value = "/{id}/image", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'PARENT')")
    public ResponseEntity<?> getImage(@PathVariable String id) {
        try {
            String imageUrl = parentService.getImageUrl(id);
            return imageUrl == null || imageUrl.isBlank()
                ? ResponseEntity.notFound().build()
                : ResponseEntity.ok(ApiResponse.success("Parent image URL fetched", imageUrl));
        } catch (RuntimeException e) {
            return "PARENT_NOT_FOUND".equals(e.getMessage())
                ? ResponseEntity.notFound().build()
                : ResponseEntity.internalServerError().build();
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
