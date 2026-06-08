package com.example.campus_hub.repository;

import com.example.campus_hub.entity.Parent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ParentRepository
        extends JpaRepository<Parent, String> {

    Optional<Parent> findByUserId(String userId);

    // Search by parent name or email
    @Query("""
        SELECT p FROM Parent p
        JOIN p.user u
        WHERE (
            :search IS NULL OR :search = '' OR
            LOWER(u.fullName) LIKE LOWER(CONCAT('%', :search, '%')) OR
            LOWER(u.email)    LIKE LOWER(CONCAT('%', :search, '%'))
        )
        """)
    Page<Parent> searchParents(
        @Param("search") String search,
        Pageable pageable
    );

    // Find parent by their child's student ID
    @Query("""
        SELECT p FROM Parent p
        JOIN p.children c
        WHERE c.id = :studentId
        """)
    java.util.List<Parent> findByChildId(
        @Param("studentId") String studentId
    );

    boolean existsByUserId(String userId);
}
