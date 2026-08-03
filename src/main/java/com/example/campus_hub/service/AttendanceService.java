package com.example.campus_hub.service;

import com.example.campus_hub.dto.MarkAttendanceRequest;
import com.example.campus_hub.entity.*;
import com.example.campus_hub.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final StudentRepository    studentRepository;
    private final CourseRepository     courseRepository;
    private final UserRepository       userRepository;
    private final ParentRepository     parentRepository;
    private final EmailService         emailService;

    public AttendanceService(
            AttendanceRepository attendanceRepository,
            StudentRepository    studentRepository,
            CourseRepository     courseRepository,
            UserRepository       userRepository,
            ParentRepository     parentRepository,
            EmailService         emailService
    ) {
        this.attendanceRepository = attendanceRepository;
        this.studentRepository    = studentRepository;
        this.courseRepository     = courseRepository;
        this.userRepository       = userRepository;
        this.parentRepository     = parentRepository;
        this.emailService         = emailService;
    }

    // ════════════════════════════════════════════════════
    // MARK ATTENDANCE
    // Teacher submits attendance for a whole class at once
    // ════════════════════════════════════════════════════
    @Transactional
    public List<Attendance> markAttendance(
            MarkAttendanceRequest request,
            String teacherEmail) {

        // Find the teacher marking attendance
        User teacher = userRepository
                .findByEmail(teacherEmail)
                .orElseThrow(() ->
                        new RuntimeException("USER_NOT_FOUND")
                );

        // Find the course
        Course course = courseRepository
                .findById(request.getCourseId())
                .orElseThrow(() ->
                        new RuntimeException("COURSE_NOT_FOUND")
                );

        List<Attendance> saved = new ArrayList<>();

        for (MarkAttendanceRequest.AttendanceRecord record
                : request.getRecords()) {

            Student student = studentRepository
                    .findById(record.getStudentId())
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "STUDENT_NOT_FOUND:" + record.getStudentId()
                            )
                    );

            // Parse status
            Attendance.Status status;
            try {
                status = Attendance.Status.valueOf(
                        record.getStatus().toUpperCase()
                );
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("INVALID_STATUS");
            }

            // Skip if already marked for this date
            if (attendanceRepository
                    .existsByStudentIdAndCourseIdAndDate(
                            student.getId(),
                            course.getId(),
                            request.getDate()
                    )) {
                continue;
            }

            // Save attendance record
            Attendance attendance = new Attendance();
            attendance.setStudent(student);
            attendance.setCourse(course);
            attendance.setMarkedBy(teacher);
            attendance.setStatus(status);
            attendance.setDate(request.getDate());
            saved.add(attendanceRepository.save(attendance));

            // If ABSENT → notify all linked parents
            if (status == Attendance.Status.ABSENT) {
                notifyParents(student, course, request.getDate());
            }
        }

        return saved;
    }

    // ════════════════════════════════════════════════════
    // NOTIFY PARENTS — called when student is marked ABSENT
    // ════════════════════════════════════════════════════
    private void notifyParents(
            Student student,
            Course course,
            java.time.LocalDate date) {

        // Find all parents linked to this student
        List<Parent> parents = parentRepository
                .findByChildId(student.getId());

        if (parents.isEmpty()) return;

        for (Parent parent : parents) {
            emailService.sendAbsenceNotificationEmail(
                    parent.getUser().getEmail(),
                    parent.getUser().getFullName(),
                    student.getUser().getFullName(),
                    course.getName(),
                    date.toString()
            );
        }
    }

    // ════════════════════════════════════════════════════
    // GET BY COURSE — teacher views class attendance
    // ════════════════════════════════════════════════════
    public Page<Attendance> getByCourse(
            String courseId, int page, int size) {

        Pageable pageable = PageRequest.of(
                page - 1, size,
                Sort.by(Sort.Direction.DESC, "date")
        );
        return attendanceRepository.findByCourseId(
                courseId, pageable
        );
    }

    // ════════════════════════════════════════════════════
    // GET BY STUDENT — student/parent views attendance
    // ════════════════════════════════════════════════════
    public Page<Attendance> getByStudent(
            String studentId, int page, int size) {

        Pageable pageable = PageRequest.of(
                page - 1, size,
                Sort.by(Sort.Direction.DESC, "date")
        );
        return attendanceRepository.findByStudentId(
                studentId, pageable
        );
    }

    // ════════════════════════════════════════════════════
    // GET SUMMARY — attendance counts for a student
    // ════════════════════════════════════════════════════
    public AttendanceSummary getSummary(String studentId) {
        long present = attendanceRepository
                .countByStudentIdAndStatus(
                        studentId, Attendance.Status.PRESENT
                );
        long absent = attendanceRepository
                .countByStudentIdAndStatus(
                        studentId, Attendance.Status.ABSENT
                );
        long late = attendanceRepository
                .countByStudentIdAndStatus(
                        studentId, Attendance.Status.LATE
                );

        long total = present + absent + late;
        double percentage = total > 0
                ? Math.round((present * 100.0 / total) * 10.0) / 10.0
                : 0.0;

        return new AttendanceSummary(
                present, absent, late, total, percentage
        );
    }

    // Simple summary record
    public record AttendanceSummary(
            long present,
            long absent,
            long late,
            long total,
            double attendancePercentage
    ) {}
}