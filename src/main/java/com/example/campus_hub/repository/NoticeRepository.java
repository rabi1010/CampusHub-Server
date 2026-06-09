package com.example.campus_hub.repository;

import com.example.campus_hub.entity.Notice;
import com.example.campus_hub.entity.Notice.ForRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NoticeRepository
        extends JpaRepository<Notice, String> {

    // Returns notices for ALL + notices targeted at specific role
    // So a STUDENT sees ALL notices + STUDENT notices
    // A PARENT sees ALL notices + PARENT notices
    @Query("""
        SELECT n FROM Notice n
        WHERE n.forRole = 'ALL'
        OR n.forRole = :role
        ORDER BY n.createdAt DESC
        """)
    Page<Notice> findByRole(
            @Param("role") ForRole role,
            Pageable pageable
    );

    // Admin sees everything
    @Query("""
        SELECT n FROM Notice n
        WHERE (
            :search IS NULL OR :search = '' OR
            LOWER(n.title)   LIKE LOWER(CONCAT('%', :search, '%')) OR
            LOWER(n.content) LIKE LOWER(CONCAT('%', :search, '%'))
        )
        ORDER BY n.createdAt DESC
        """)
    Page<Notice> findAllWithSearch(
            @Param("search") String search,
            Pageable pageable
    );
}