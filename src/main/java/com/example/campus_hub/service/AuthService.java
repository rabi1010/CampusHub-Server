package com.example.campus_hub.service;

import com.example.campus_hub.dto.*;
import com.example.campus_hub.entity.Parent;
import com.example.campus_hub.entity.Student;
import com.example.campus_hub.entity.Teacher;
import com.example.campus_hub.repository.TeacherRepository;
import com.example.campus_hub.entity.User;
import com.example.campus_hub.repository.*;
import com.example.campus_hub.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final ParentRepository parentRepository;
    private final DepartmentRepository departmentRepository;
    private final BatchRepository batchRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;

    public AuthService(
            UserRepository       userRepository,
            StudentRepository    studentRepository,
            TeacherRepository    teacherRepository,
            ParentRepository     parentRepository,
            DepartmentRepository departmentRepository,
            BatchRepository      batchRepository,
            PasswordEncoder      passwordEncoder,
            JwtUtil              jwtUtil,
            EmailService         emailService
    ) {
        this.userRepository       = userRepository;
        this.studentRepository    = studentRepository;
        this.teacherRepository    = teacherRepository;
        this.parentRepository     = parentRepository;
        this.departmentRepository = departmentRepository;
        this.batchRepository      = batchRepository;
        this.passwordEncoder      = passwordEncoder;
        this.jwtUtil              = jwtUtil;
        this.emailService         = emailService;
    }

    // ── Register ─────────────────────────────────────────
    public User register(RegisterRequest request) {
        System.out.println("=== REGISTER CALLED ===");
        System.out.println("Email: " + request.getEmail());
        System.out.println("Role: " + request.getRole());
        System.out.println("ChildRollNumbers: " + request.getChildRollNumbers());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("EMAIL_TAKEN");
        }

        // Validate child roll numbers exist if registering as parent
        if (request.getChildRollNumbers() != null
                && !request.getChildRollNumbers().isEmpty()) {
            for (String rollNo : request.getChildRollNumbers()) {
                if (!studentRepository.existsByRollNo(rollNo)) {
                    throw new RuntimeException("STUDENT_NOT_FOUND:" + rollNo);
                }
            }
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .role(User.Role.PENDING)
                .status(User.Status.PENDING)
                .build();

        User saved = userRepository.save(user);

        emailService.sendRegistrationEmail(
                saved.getEmail(),
                saved.getFullName()
        );

        return saved;
    }

    // ── Login ─────────────────────────────────────────────
    public AuthResponse login(LoginRequest request) {

        User user = userRepository
                .findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new RuntimeException("INVALID_CREDENTIALS")
                );

        if (!passwordEncoder.matches(
                request.getPassword(), user.getPassword()
        )) {
            throw new RuntimeException("INVALID_CREDENTIALS");
        }

        if (user.getStatus() == User.Status.PENDING) {
            throw new RuntimeException("ACCOUNT_PENDING");
        }

        if (user.getStatus() == User.Status.SUSPENDED) {
            throw new RuntimeException("ACCOUNT_SUSPENDED");
        }

        String token = jwtUtil.generateToken(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getRole().name()
        );

        return AuthResponse.builder()
                .token(token)
                .user(AuthResponse.UserDTO.builder()
                        .id(user.getId())
                        .email(user.getEmail())
                        .fullName(user.getFullName())
                        .role(user.getRole().name())
                        .build())
                .build();
    }

    // ── Get current user ──────────────────────────────────
    public User me(String email) {
        return userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("USER_NOT_FOUND")
                );
    }

    // ── List users awaiting admin approval ────────────────
    public List<User> getPendingUsers() {
        return userRepository.findByStatus(User.Status.PENDING);
    }

    @Transactional
    public User approveUser(String userId, ApproveUserRequest request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("USER_NOT_FOUND"));

        if (user.getStatus() == User.Status.ACTIVE) {
            throw new RuntimeException("ALREADY_APPROVED");
        }

        user.setStatus(User.Status.ACTIVE);
        user.setRole(User.Role.valueOf(request.getRole()));
        User saved = userRepository.save(user);

        // ── STUDENT ───────────────────────────────────────
        if ("STUDENT".equals(request.getRole())) {
            if (request.getRollNo() == null
                    || request.getDepartmentId() == null
                    || request.getBatchId() == null) {
                throw new RuntimeException("STUDENT_DETAILS_REQUIRED");
            }

            if (studentRepository.existsByRollNo(request.getRollNo())) {
                throw new RuntimeException("ROLL_NO_TAKEN");
            }

            var dept = departmentRepository
                    .findById(request.getDepartmentId())
                    .orElseThrow(() -> new RuntimeException("DEPARTMENT_NOT_FOUND"));

            var batch = batchRepository
                    .findById(request.getBatchId())
                    .orElseThrow(() -> new RuntimeException("BATCH_NOT_FOUND"));

            Student student = new Student();
            student.setUser(saved);
            student.setRollNo(request.getRollNo());
            student.setDepartment(dept);
            student.setBatch(batch);
            student.setAddress(request.getAddress());
            studentRepository.save(student);
        }
        if ("TEACHER".equals(request.getRole())) {
            if (request.getEmployeeId() == null
                    || request.getDepartmentId() == null) {
                throw new RuntimeException("TEACHER_DETAILS_REQUIRED");
            }

            if (teacherRepository.existsByEmployeeId(
                    request.getEmployeeId())) {
                throw new RuntimeException("EMPLOYEE_ID_TAKEN");
            }

            var dept = departmentRepository
                    .findById(request.getDepartmentId())
                    .orElseThrow(() ->
                            new RuntimeException("DEPARTMENT_NOT_FOUND")
                    );

            Teacher teacher = new Teacher();
            teacher.setUser(saved);
            teacher.setEmployeeId(request.getEmployeeId());
            teacher.setDepartment(dept);
            teacher.setQualification(request.getQualification());
            teacherRepository.save(teacher);
        }

        // ── PARENT ────────────────────────────────────────
        if ("PARENT".equals(request.getRole())) {
            if (request.getStudentIds() == null
                    || request.getStudentIds().isEmpty()) {
                throw new RuntimeException("PARENT_DETAILS_REQUIRED");
            }

            List<Student> children = new ArrayList<>();
            for (String studentId : request.getStudentIds()) {
                Student student = studentRepository
                        .findById(studentId)
                        .orElseThrow(() ->
                                new RuntimeException("STUDENT_NOT_FOUND")
                        );
                children.add(student);
            }

            Parent parent = new Parent();
            parent.setUser(saved);
            parent.setChildren(children);
            parent.setRelationship(request.getRelationship());
            parentRepository.save(parent);
        }

        // Send approval email
        emailService.sendApprovalEmail(
                saved.getEmail(),
                saved.getFullName(),
                saved.getRole().name()
        );
        return saved;

    }
}

