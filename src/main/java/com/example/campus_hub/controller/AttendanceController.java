package com.example.campus_hub.controller;

import com.example.campus_hub.dto.ApiResponse;
import com.example.campus_hub.dto.AttendanceResponse;
import com.example.campus_hub.dto.MarkAttendanceRequest;
import com.example.campus_hub.entity.Attendance;
import com.example.campus_hub.service.AttendanceService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/attendance")
@CrossOrigin(
        origins = {"${app.frontend.url}", "https://campushub-n6bn.onrender.com"},
        allowCredentials = "true"
)
public class AttendanceController {

    private final AttendanceService attendanceService;

    public AttendanceController(
            AttendanceService attendanceService
    ) {
        this.attendanceService = attendanceService;
    }

    // POST /api/attendance — teacher marks attendance
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<ApiResponse<List<AttendanceResponse>>> mark(
            @Valid @RequestBody MarkAttendanceRequest request,
            Authentication auth
    ) {
        try {
            List<AttendanceResponse> result = attendanceService
                    .markAttendance(request, auth.getName()).stream()
                    .map(AttendanceResponse::from).toList();
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(
                            "Attendance marked successfully", result
                    ));
        } catch (RuntimeException e) {
            if ("COURSE_NOT_FOUND".equals(e.getMessage())) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Course not found"));
            }
            if (e.getMessage() != null &&
                    e.getMessage().startsWith("STUDENT_NOT_FOUND")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error(
                                "Student not found: " +
                                        e.getMessage().split(":")[1]
                        ));
            }
            if ("INVALID_STATUS".equals(e.getMessage())) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error(
                                "Status must be PRESENT, ABSENT, or LATE"
                        ));
            }
            throw e;
        }
    }

    // GET /api/attendance/course/:id
    @GetMapping("/course/{courseId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<ApiResponse<Page<AttendanceResponse>>> getByCourse(
            @PathVariable String courseId,
            @RequestParam(defaultValue = "1")  int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Attendance fetched",
                        attendanceService.getByCourse(courseId, page, size)
                                .map(AttendanceResponse::from)
                )
        );
    }

    // GET /api/attendance/student/:id
    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT', 'PARENT')")
    public ResponseEntity<ApiResponse<Page<AttendanceResponse>>> getByStudent(
            @PathVariable String studentId,
            @RequestParam(defaultValue = "1")  int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Attendance fetched",
                        attendanceService.getByStudent(studentId, page, size)
                                .map(AttendanceResponse::from)
                )
        );
    }

    // GET /api/attendance/summary — authenticated student views own summary
    @GetMapping("/summary")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<AttendanceService.AttendanceSummary>>
    getMySummary(Authentication auth) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Summary fetched",
                        attendanceService.getSummaryForUser(auth.getName())
                )
        );
    }

    // GET /api/attendance/student/:id/summary
    @GetMapping("/student/{studentId}/summary")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT', 'PARENT')")
    public ResponseEntity<ApiResponse<AttendanceService.AttendanceSummary>>
    getSummary(@PathVariable String studentId) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Summary fetched",
                        attendanceService.getSummary(studentId)
                )
        );
    }
}
