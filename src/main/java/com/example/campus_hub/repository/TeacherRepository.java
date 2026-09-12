package com.example.campus_hub.repository;

import com.example.campus_hub.entity.Teacher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TeacherRepository
        extends JpaRepository<Teacher, String> {

    boolean existsByEmployeeId(String employeeId);

    Optional<Teacher> findByUserId(String userId);

    @Query("""
        SELECT t FROM Teacher t
        JOIN t.user u
        WHERE (
            :search IS NULL OR :search = '' OR
            LOWER(u.fullName)    LIKE LOWER(CONCAT('%', :search, '%')) OR
            LOWER(u.email)       LIKE LOWER(CONCAT('%', :search, '%')) OR
            LOWER(t.employeeId)  LIKE LOWER(CONCAT('%', :search, '%'))
        )
        AND (:departmentId IS NULL OR t.department.id = :departmentId)
        """)
    Page<Teacher> searchTeachers(
            @Param("search")       String search,
            @Param("departmentId") String departmentId,
            Pageable pageable
    );
}