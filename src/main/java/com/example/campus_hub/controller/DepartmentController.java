package com.example.campus_hub.controller;

import com.example.campus_hub.dto.ApiResponse;
import com.example.campus_hub.entity.Batch;
import com.example.campus_hub.entity.Department;
import com.example.campus_hub.repository.BatchRepository;
import com.example.campus_hub.repository.DepartmentRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = {"${app.frontend.url}", "https://campushub-n6bn.onrender.com"}, allowCredentials = "true")
public class DepartmentController {

    private final DepartmentRepository deptRepo;
    private final BatchRepository      batchRepo;

    public DepartmentController(
            DepartmentRepository deptRepo,
            BatchRepository batchRepo
    ) {
        this.deptRepo  = deptRepo;
        this.batchRepo = batchRepo;
    }

    @GetMapping("/departments")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<Department>>> getDepartments() {
        return ResponseEntity.ok(
                ApiResponse.success("Departments fetched", deptRepo.findAll())
        );
    }

    @GetMapping("/batches")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<Batch>>> getBatches() {
        return ResponseEntity.ok(
                ApiResponse.success("Batches fetched", batchRepo.findAll())
        );
    }
}