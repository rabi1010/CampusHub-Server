package com.example.campus_hub.controller;

import com.example.campus_hub.dto.ApiResponse;
import com.example.campus_hub.dto.CreateTeacherRequest;
import com.example.campus_hub.dto.UpdateTeacherRequest;
import com.example.campus_hub.entity.Teacher;
import com.example.campus_hub.service.TeacherService;
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
@RequestMapping("/api/teachers")
@CrossOrigin(
        origins = "${app.frontend.url}",
        allowCredentials = "true"
)
public class TeacherController {

    private final TeacherService teacherService;

    public TeacherController(TeacherService teacherService) {
        this.teacherService = teacherService;
    }

    // GET /api/teachers
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<ApiResponse<Page<Teacher>>> getAll(
            @RequestParam(defaultValue = "1")  int    page,
            @RequestParam(defaultValue = "10") int    size,
            @RequestParam(defaultValue = "")   String search,
            @RequestParam(required = false)    String departmentId
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Teachers fetched",
                        teacherService.getAll(page, size, search, departmentId)
                )
        );
    }

    // GET /api/teachers/me — teacher views own profile
    @GetMapping("/me")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<ApiResponse<Teacher>> getMe(Authentication auth) {
        try {
            return ResponseEntity.ok(
                    ApiResponse.success(
                            "Profile fetched",
                            teacherService.getByUserEmail(auth.getName())
                    )
            );
        } catch (RuntimeException e) {
            if ("TEACHER_NOT_FOUND".equals(e.getMessage())) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Teacher profile not found"));
            }
            throw e;
        }
    }

    // GET /api/teachers/:id
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<ApiResponse<Teacher>> getById(@PathVariable String id) {
        try {
            return ResponseEntity.ok(ApiResponse.success("Teacher fetched", teacherService.getById(id)));
        } catch (RuntimeException e) {
            if ("TEACHER_NOT_FOUND".equals(e.getMessage())) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Teacher not found"));
            }
            throw e;
        }
    }

    @PostMapping(value = "/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<ApiResponse<Object>> uploadImage(
            @PathVariable String id, @RequestParam("image") MultipartFile file
    ) {
        try {
            teacherService.uploadImage(id, file);
            return ResponseEntity.ok(ApiResponse.success("Image uploaded", null));
        } catch (RuntimeException e) {
            return switch (e.getMessage()) {
                case "TEACHER_NOT_FOUND" -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("Teacher not found"));
                case "INVALID_IMAGE_TYPE" -> ResponseEntity.badRequest().body(ApiResponse.error("Only JPEG, PNG, WebP allowed"));
                case "IMAGE_TOO_LARGE" -> ResponseEntity.badRequest().body(ApiResponse.error("Image must be under 2MB"));
                case "IMAGE_UPLOAD_FAILED" -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("Upload failed"));
                default -> throw e;
            };
        }
    }

    @GetMapping(value = "/{id}/image", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<?> getImage(@PathVariable String id) {
        try {
            String imageUrl = teacherService.getImageUrl(id);
            return imageUrl == null || imageUrl.isBlank()
                    ? ResponseEntity.notFound().build()
                    : ResponseEntity.ok(ApiResponse.success("Teacher image URL fetched", imageUrl));
        } catch (RuntimeException e) {
            return "TEACHER_NOT_FOUND".equals(e.getMessage())
                    ? ResponseEntity.notFound().build()
                    : ResponseEntity.internalServerError().build();
        }
    }

    // POST /api/teachers
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Teacher>> create(
            @Valid @RequestBody CreateTeacherRequest request
    ) {
        try {
            Teacher teacher = teacherService.create(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(
                            "Teacher created", teacher
                    ));
        } catch (RuntimeException e) {
            return switch (e.getMessage()) {
                case "EMAIL_TAKEN" ->
                        ResponseEntity.status(HttpStatus.CONFLICT)
                                .body(ApiResponse.error(
                                        "Email already registered"
                                ));
                case "EMPLOYEE_ID_TAKEN" ->
                        ResponseEntity.status(HttpStatus.CONFLICT)
                                .body(ApiResponse.error(
                                        "Employee ID already exists"
                                ));
                case "DEPARTMENT_NOT_FOUND" ->
                        ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body(ApiResponse.error(
                                        "Department not found"
                                ));
                default -> throw e;
            };
        }
    }

    // PUT /api/teachers/:id
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Teacher>> update(
            @PathVariable String id,
            @RequestBody  UpdateTeacherRequest request
    ) {
        try {
            return ResponseEntity.ok(
                    ApiResponse.success(
                            "Teacher updated",
                            teacherService.update(id, request)
                    )
            );
        } catch (RuntimeException e) {
            if ("TEACHER_NOT_FOUND".equals(e.getMessage())) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Teacher not found"));
            }
            throw e;
        }
    }

    // DELETE /api/teachers/:id
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Object>> delete(
            @PathVariable String id
    ) {
        try {
            teacherService.delete(id);
            return ResponseEntity.ok(
                    ApiResponse.success("Teacher deleted", null)
            );
        } catch (RuntimeException e) {
            if ("TEACHER_NOT_FOUND".equals(e.getMessage())) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Teacher not found"));
            }
            throw e;
        }
    }
}
