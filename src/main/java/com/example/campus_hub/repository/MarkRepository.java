package com.example.campus_hub.repository;

import com.example.campus_hub.entity.Mark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MarkRepository
        extends JpaRepository<Mark, String> {

    // Get all marks for a student
    List<Mark> findByStudentId(String studentId);

    // Get all marks for a student in a specific course
    List<Mark> findByStudentIdAndCourseId(
            String studentId, String courseId
    );

    // Get all marks for a course
    List<Mark> findByCourseId(String courseId);

    // Check if mark already exists for this combination
    Optional<Mark> findByStudentIdAndCourseIdAndExamType(
            String studentId,
            String courseId,
            Mark.ExamType examType
    );

    // GPA calculation — average percentage across all courses
    @Query("""
        SELECT AVG(m.marksObtained / m.totalMarks * 100)
        FROM Mark m
        WHERE m.student.id = :studentId
        """)
    Double calculateAveragePercentage(
            @Param("studentId") String studentId
    );
}