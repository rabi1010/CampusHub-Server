package com.example.campus_hub.controller;

import com.example.campus_hub.dto.ApiResponse;
import com.example.campus_hub.dto.MarkResponse;
import com.example.campus_hub.dto.UploadMarksRequest;
import com.example.campus_hub.entity.Mark;
import com.example.campus_hub.service.MarkService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/marks")
@CrossOrigin(
        origins = "${app.frontend.url}",
        allowCredentials = "true"
)
public class MarkController {

    private final MarkService markService;

    public MarkController(MarkService markService) {
        this.markService = markService;
    }

    // POST /api/marks — teacher uploads marks
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<ApiResponse<List<MarkResponse>>> upload(
            @Valid @RequestBody UploadMarksRequest request,
            Authentication auth
    ) {
        try {
            List<MarkResponse> marks = markService.uploadMarks(request, auth.getName())
                    .stream().map(MarkResponse::from).toList();
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(
                            "Marks uploaded successfully", marks
                    ));
        } catch (RuntimeException e) {
            if ("COURSE_NOT_FOUND".equals(e.getMessage())) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Course not found"));
            }
            if ("INVALID_EXAM_TYPE".equals(e.getMessage())) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error(
                                "Exam type must be INTERNAL, MIDTERM, or FINAL"
                        ));
            }
            if (e.getMessage() != null &&
                    e.getMessage().startsWith("STUDENT_NOT_FOUND")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error(
                                "Student not found: " +
                                        e.getMessage().split(":")[1]
                        ));
            }
            throw e;
        }
    }

    // GET /api/marks/student/:id
    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT', 'PARENT')")
    public ResponseEntity<ApiResponse<List<MarkResponse>>> getByStudent(
            @PathVariable String studentId
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Marks fetched",
                        markService.getByStudent(studentId).stream()
                                .map(MarkResponse::from).toList()
                )
        );
    }

    // GET /api/marks/gpa — authenticated student views own GPA
    @GetMapping("/gpa")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<Double>> getMyGpa(Authentication auth) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "GPA fetched",
                        markService.getGpaForUser(auth.getName())
                )
        );
    }

    // GET /api/marks/course/:id
    @GetMapping("/course/{courseId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<ApiResponse<List<MarkResponse>>> getByCourse(
            @PathVariable String courseId
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Marks fetched",
                        markService.getByCourse(courseId).stream()
                                .map(MarkResponse::from).toList()
                )
        );
    }

    // GET /api/marks/student/:id/course/:courseId
    @GetMapping("/student/{studentId}/course/{courseId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT', 'PARENT')")
    public ResponseEntity<ApiResponse<List<MarkResponse>>> getByStudentAndCourse(
            @PathVariable String studentId,
            @PathVariable String courseId
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Marks fetched",
                        markService.getByStudentAndCourse(studentId, courseId)
                                .stream().map(MarkResponse::from).toList()
                )
        );
    }

    // GET /api/marks/student/:id/gpa
    @GetMapping("/student/{studentId}/gpa")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT', 'PARENT')")
    public ResponseEntity<ApiResponse<Double>> getGpa(
            @PathVariable String studentId
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "GPA fetched",
                        markService.getGpa(studentId)
                )
        );
    }
}
