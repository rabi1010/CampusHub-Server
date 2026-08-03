package com.example.campus_hub.repository;

import com.example.campus_hub.entity.Attendance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface AttendanceRepository
        extends JpaRepository<Attendance, String> {

    // Get all attendance for a course
    Page<Attendance> findByCourseId(
            String courseId, Pageable pageable
    );

    // Get all attendance for a student
    Page<Attendance> findByStudentId(
            String studentId, Pageable pageable
    );

    // Get attendance for a student in a specific course
    List<Attendance> findByStudentIdAndCourseId(
            String studentId, String courseId
    );

    // Check if attendance already marked for this date + course + student
    boolean existsByStudentIdAndCourseIdAndDate(
            String studentId, String courseId, LocalDate date
    );

    // Summary counts for a student
    @Query("""
        SELECT COUNT(a) FROM Attendance a
        WHERE a.student.id = :studentId
        AND a.status = :status
        """)
    long countByStudentIdAndStatus(
            @Param("studentId") String studentId,
            @Param("status")    Attendance.Status status
    );
    long countByStatus(Attendance.Status status);
}