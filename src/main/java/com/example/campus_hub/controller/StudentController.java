package com.example.campus_hub.controller;

import com.example.campus_hub.dto.ApiResponse;
import com.example.campus_hub.dto.CreateStudentRequest;
import com.example.campus_hub.dto.UpdateStudentRequest;
import com.example.campus_hub.dto.StudentResponse;
import com.example.campus_hub.entity.Student;
import com.example.campus_hub.service.StudentService;
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
@RequestMapping("/api/students")
@CrossOrigin(origins = "${app.frontend.url}", allowCredentials = "true")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    // GET /api/students?page=1&size=10&search=john&departmentId=...
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<ApiResponse<Page<StudentResponse>>> getAll(
            @RequestParam(defaultValue = "1")  int    page,
            @RequestParam(defaultValue = "10") int    size,
            @RequestParam(defaultValue = "")   String search,
            @RequestParam(required = false)    String departmentId
    ) {
        Page<StudentResponse> students =
                studentService.getAll(page, size, search, departmentId);
        return ResponseEntity.ok(
                ApiResponse.success("Students fetched", students)
        );
    }

    // GET /api/students/:id
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<ApiResponse<Student>> getById(
            @PathVariable String id
    ) {
        try {
            Student student = studentService.getById(id);
            return ResponseEntity.ok(
                    ApiResponse.success("Student fetched", student)
            );
        } catch (RuntimeException e) {
            if ("STUDENT_NOT_FOUND".equals(e.getMessage())) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Student not found"));
            }
            throw e;
        }
    }

    // GET /api/students/me - resolve the student record linked to the session user
    @GetMapping("/me")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<Student>> getMe(Authentication authentication) {
        try {
            Student student = studentService.getByUserEmail(authentication.getName());
            return ResponseEntity.ok(ApiResponse.success("Student profile fetched", student));
        } catch (RuntimeException e) {
            if ("STUDENT_NOT_FOUND".equals(e.getMessage())) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Student profile not found"));
            }
            throw e;
        }
    }

    // POST /api/students
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Student>> create(
            @Valid @RequestBody CreateStudentRequest request
    ) {
        try {
            Student student = studentService.create(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Student created", student));
        } catch (RuntimeException e) {
            return switch (e.getMessage()) {
                case "EMAIL_TAKEN" ->
                        ResponseEntity.status(HttpStatus.CONFLICT)
                                .body(ApiResponse.error("Email already registered"));
                case "ROLL_NO_TAKEN" ->
                        ResponseEntity.status(HttpStatus.CONFLICT)
                                .body(ApiResponse.error("Roll number already taken"));
                case "DEPARTMENT_NOT_FOUND" ->
                        ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body(ApiResponse.error("Department not found"));
                case "BATCH_NOT_FOUND" ->
                        ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body(ApiResponse.error("Batch not found"));
                default -> throw e;
            };
        }
    }

    // PUT /api/students/:id
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Student>> update(
            @PathVariable  String               id,
            @RequestBody   UpdateStudentRequest request
    ) {
        try {
            Student student = studentService.update(id, request);
            return ResponseEntity.ok(
                    ApiResponse.success("Student updated", student)
            );
        } catch (RuntimeException e) {
            if ("STUDENT_NOT_FOUND".equals(e.getMessage())) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Student not found"));
            }
            throw e;
        }
    }

    // DELETE /api/students/:id
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Object>> delete(
            @PathVariable String id
    ) {
        try {
            studentService.delete(id);
            return ResponseEntity.ok(
                    ApiResponse.success("Student deleted", null)
            );
        } catch (RuntimeException e) {
            if ("STUDENT_NOT_FOUND".equals(e.getMessage())) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Student not found"));
            }
            throw e;
        }
    }

    // POST /api/students/:id/image
    // consumes = MULTIPART_FORM_DATA — tells Spring to expect a file upload
    @PostMapping(
            value    = "/{id}/image",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT')")
    public ResponseEntity<ApiResponse<Object>> uploadImage(
            @PathVariable                      String      id,
            @RequestParam("image") MultipartFile file  // "image" = form field name
    ) {
        try {
            studentService.uploadImage(id, file);
            return ResponseEntity.ok(
                    ApiResponse.success("Image uploaded", null)
            );
        } catch (RuntimeException e) {
            return switch (e.getMessage()) {
                case "STUDENT_NOT_FOUND"   ->
                        ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body(ApiResponse.error("Student not found"));
                case "INVALID_IMAGE_TYPE"  ->
                        ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(ApiResponse.error("Only JPEG, PNG, WebP allowed"));
                case "IMAGE_TOO_LARGE"     ->
                        ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(ApiResponse.error("Image must be under 2MB"));
                case "IMAGE_UPLOAD_FAILED" ->
                        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(ApiResponse.error("Upload failed"));
                default -> throw e;
            };
        }
    }

    // GET /api/students/:id/image
    // produces = IMAGE_JPEG_VALUE — tells Spring the response is binary image
    @GetMapping(
            value    = "/{id}/image",
            produces = MediaType.IMAGE_JPEG_VALUE
    )
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<byte[]> getImage(@PathVariable String id) {
        try {
            byte[] image = studentService.getImage(id);
            String contentType = studentService.getImageContentType(id);
            MediaType mediaType = contentType == null
                    ? MediaType.IMAGE_JPEG
                    : MediaType.parseMediaType(contentType);
            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .body(image);
        } catch (RuntimeException e) {
            return ResponseEntity.status(
                    "IMAGE_NOT_FOUND".equals(e.getMessage())
                            ? HttpStatus.NOT_FOUND
                            : HttpStatus.INTERNAL_SERVER_ERROR
            ).build();
        }
    }
}
