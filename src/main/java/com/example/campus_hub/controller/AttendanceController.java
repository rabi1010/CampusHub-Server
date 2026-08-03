package com.example.campus_hub.controller;

import com.example.campus_hub.dto.ApiResponse;
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
        origins = "${app.frontend.url}",
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
    public ResponseEntity<ApiResponse<List<Attendance>>> mark(
            @Valid @RequestBody MarkAttendanceRequest request,
            Authentication auth
    ) {
        try {
            List<Attendance> result =
                    attendanceService.markAttendance(
                            request, auth.getName()
                    );
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
    public ResponseEntity<ApiResponse<Page<Attendance>>> getByCourse(
            @PathVariable String courseId,
            @RequestParam(defaultValue = "1")  int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Attendance fetched",
                        attendanceService.getByCourse(courseId, page, size)
                )
        );
    }

    // GET /api/attendance/student/:id
    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT', 'PARENT')")
    public ResponseEntity<ApiResponse<Page<Attendance>>> getByStudent(
            @PathVariable String studentId,
            @RequestParam(defaultValue = "1")  int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Attendance fetched",
                        attendanceService.getByStudent(
                                studentId, page, size
                        )
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