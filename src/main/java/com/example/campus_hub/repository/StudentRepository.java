package com.example.campus_hub.repository;

import com.example.campus_hub.entity.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface StudentRepository
        extends JpaRepository<Student, String> {

    // Check if roll number already exists
    boolean existsByRollNo(String rollNo);

    // Find student by their linked User ID
    Optional<Student> findByUserId(String userId);

    Optional<Student> findByRollNo(String rollNo);

    // Search across multiple fields with pagination
    // This is a custom JPQL query
    // JPQL uses entity class names (Student, not "students" table)
    // and field names from Java classes (not column names)
    //
    // LOWER() makes it case-insensitive
    // :search is a named parameter — injected safely (no SQL injection)
    // %:search% is a LIKE pattern — matches anywhere in the string
    @Query("""
        SELECT s FROM Student s
        JOIN s.user u
        WHERE (
            :search IS NULL OR :search = '' OR
            LOWER(u.fullName) LIKE LOWER(CONCAT('%', :search, '%')) OR
            LOWER(u.email)    LIKE LOWER(CONCAT('%', :search, '%')) OR
            LOWER(s.rollNo)   LIKE LOWER(CONCAT('%', :search, '%'))
        )
        AND (:departmentId IS NULL OR s.department.id = :departmentId)
        """)
    Page<Student> searchStudents(
            @Param("search")       String search,
            @Param("departmentId") String departmentId,
            Pageable pageable
    );
}