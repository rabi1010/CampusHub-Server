package com.example.campus_hub.config;

import com.example.campus_hub.entity.Batch;
import com.example.campus_hub.entity.Department;
import com.example.campus_hub.entity.User;
import com.example.campus_hub.repository.BatchRepository;
import com.example.campus_hub.repository.DepartmentRepository;
import com.example.campus_hub.repository.UserRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

// ApplicationRunner runs automatically after Spring Boot starts
// This is equivalent to prisma/seed.ts in Node.js
// It checks if data exists before inserting — safe to run multiple times

@Component
public class DataSeeder implements ApplicationRunner {

    private final UserRepository       userRepository;
    private final DepartmentRepository departmentRepository;
    private final BatchRepository      batchRepository;
    private final PasswordEncoder      passwordEncoder;

    public DataSeeder(
            UserRepository       userRepository,
            DepartmentRepository departmentRepository,
            BatchRepository      batchRepository,
            PasswordEncoder      passwordEncoder
    ) {
        this.userRepository       = userRepository;
        this.departmentRepository = departmentRepository;
        this.batchRepository      = batchRepository;
        this.passwordEncoder      = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        seedDepartments();
        seedBatches();
        seedAdminUser();
    }

    // ── Departments ───────────────────────────────────────
    private void seedDepartments() {
        // Only seed if table is empty
        if (departmentRepository.count() > 0) {
            System.out.println("✅ Departments already seeded");
            return;
        }

        List<Department> departments = List.of(
                createDept("Computer Science",       "CS",  "CS Department"),
                createDept("Information Technology", "IT",  "IT Department"),
                createDept("Electronics",            "EC",  "Electronics Department"),
                createDept("Civil Engineering",      "CE",  "Civil Department"),
                createDept("Mechanical Engineering", "ME",  "Mechanical Department")
        );

        departmentRepository.saveAll(departments);
        System.out.println("✅ Departments seeded: " + departments.size());
    }

    private Department createDept(String name, String code, String desc) {
        Department d = new Department();
        d.setName(name);
        d.setCode(code);
        d.setDescription(desc);
        return d;
    }

    // ── Batches ───────────────────────────────────────────
    private void seedBatches() {
        if (batchRepository.count() > 0) {
            System.out.println("✅ Batches already seeded");
            return;
        }

        // Get departments that were just created
        Department cs = departmentRepository.findByCode("CS");
        Department it = departmentRepository.findByCode("IT");
        Department ec = departmentRepository.findByCode("EC");
        Department ce = departmentRepository.findByCode("CE");
        Department me = departmentRepository.findByCode("ME");

        List<Batch> batches = List.of(
                createBatch("2021-2024", cs, 2021, 2024),
                createBatch("2022-2025", cs, 2022, 2025),
                createBatch("2023-2026", cs, 2023, 2026),
                createBatch("2023-2026", it, 2023, 2026),
                createBatch("2023-2026", ec, 2023, 2026),
                createBatch("2023-2026", ce, 2023, 2026),
                createBatch("2023-2026", me, 2023, 2026)
        );

        batchRepository.saveAll(batches);
        System.out.println("✅ Batches seeded: " + batches.size());
    }

    private Batch createBatch(
            String name, Department dept, int start, int end
    ) {
        Batch b = new Batch();
        b.setName(name);
        b.setDepartment(dept);
        b.setStartYear(start);
        b.setEndYear(end);
        return b;
    }

    // ── Admin user ────────────────────────────────────────
    private void seedAdminUser() {
        if (userRepository.existsByEmail("admin@campushub.edu")) {
            System.out.println("✅ Admin already exists");
            return;
        }

        User admin = new User();
        admin.setEmail("admin@campushub.edu");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setFullName("System Admin");
        admin.setRole(User.Role.ADMIN);
        admin.setStatus(User.Status.ACTIVE);

        userRepository.save(admin);
        System.out.println("✅ Admin user seeded");
        System.out.println("   Email:    admin@campushub.edu");
        System.out.println("   Password: admin123");
    }
}