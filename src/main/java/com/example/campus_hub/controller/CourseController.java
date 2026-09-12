package com.example.campus_hub.controller;

import com.example.campus_hub.dto.ApiResponse;
import com.example.campus_hub.dto.CreateCourseRequest;
import com.example.campus_hub.dto.UpdateCourseRequest;
import com.example.campus_hub.entity.Course;
import com.example.campus_hub.service.CourseService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/courses")
@CrossOrigin(
        origins = {"${app.frontend.url}", "https://campushub-n6bn.onrender.com"},
        allowCredentials = "true"
)
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    // GET /api/courses
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Page<Course>>> getAll(
            @RequestParam(defaultValue = "1")  int    page,
            @RequestParam(defaultValue = "10") int    size,
            @RequestParam(defaultValue = "")   String search,
            @RequestParam(required = false)    String departmentId,
            @RequestParam(defaultValue = "0")  int    semester
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Courses fetched",
                        courseService.getAll(
                                page, size, search, departmentId, semester
                        )
                )
        );
    }

    // GET /api/courses/:id
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Course>> getById(
            @PathVariable String id
    ) {
        try {
            return ResponseEntity.ok(
                    ApiResponse.success(
                            "Course fetched",
                            courseService.getById(id)
                    )
            );
        } catch (RuntimeException e) {
            if ("COURSE_NOT_FOUND".equals(e.getMessage())) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Course not found"));
            }
            throw e;
        }
    }

    // POST /api/courses
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Course>> create(
            @Valid @RequestBody CreateCourseRequest request
    ) {
        try {
            Course course = courseService.create(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Course created", course));
        } catch (RuntimeException e) {
            return switch (e.getMessage()) {
                case "COURSE_CODE_TAKEN" ->
                        ResponseEntity.status(HttpStatus.CONFLICT)
                                .body(ApiResponse.error(
                                        "Course code already exists"
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

    // PUT /api/courses/:id
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Course>> update(
            @PathVariable String id,
            @RequestBody  UpdateCourseRequest request
    ) {
        try {
            return ResponseEntity.ok(
                    ApiResponse.success(
                            "Course updated",
                            courseService.update(id, request)
                    )
            );
        } catch (RuntimeException e) {
            if ("COURSE_NOT_FOUND".equals(e.getMessage())) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Course not found"));
            }
            throw e;
        }
    }

    // DELETE /api/courses/:id
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Object>> delete(
            @PathVariable String id
    ) {
        try {
            courseService.delete(id);
            return ResponseEntity.ok(
                    ApiResponse.success("Course deleted", null)
            );
        } catch (RuntimeException e) {
            if ("COURSE_NOT_FOUND".equals(e.getMessage())) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Course not found"));
            }
            throw e;
        }
    }
}