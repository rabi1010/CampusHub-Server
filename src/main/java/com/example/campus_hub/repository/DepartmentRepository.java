package com.example.campus_hub.repository;

import com.example.campus_hub.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepartmentRepository
        extends JpaRepository<Department, String> {
    boolean existsByName(String name);
    boolean existsByCode(String code);
    Department findByCode(String code);  // ← add this
}