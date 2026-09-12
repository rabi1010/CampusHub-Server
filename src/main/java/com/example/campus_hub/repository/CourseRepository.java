package com.example.campus_hub.repository;

import com.example.campus_hub.entity.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CourseRepository
        extends JpaRepository<Course, String> {

    boolean existsByCode(String code);

    @Query("""
        SELECT c FROM Course c
        WHERE (
            :search IS NULL OR :search = '' OR
            LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%')) OR
            LOWER(c.code) LIKE LOWER(CONCAT('%', :search, '%'))
        )
        AND (:departmentId IS NULL OR c.department.id = :departmentId)
        AND (:semester = 0 OR c.semester = :semester)
        """)
    Page<Course> searchCourses(
            @Param("search")       String search,
            @Param("departmentId") String departmentId,
            @Param("semester")     int semester,
            Pageable pageable
    );
}