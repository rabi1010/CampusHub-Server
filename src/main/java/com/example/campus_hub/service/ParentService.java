package com.example.campus_hub.service;

import com.example.campus_hub.dto.CreateParentRequest;
import com.example.campus_hub.dto.UpdateParentRequest;
import com.example.campus_hub.entity.Parent;
import com.example.campus_hub.entity.Student;
import com.example.campus_hub.entity.User;
import com.example.campus_hub.repository.ParentRepository;
import com.example.campus_hub.repository.StudentRepository;
import com.example.campus_hub.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class ParentService {

    private final ParentRepository  parentRepository;
    private final UserRepository    userRepository;
    private final StudentRepository studentRepository;
    private final PasswordEncoder   passwordEncoder;
    private final EmailService      emailService;

    public ParentService(
        ParentRepository  parentRepository,
        UserRepository    userRepository,
        StudentRepository studentRepository,
        PasswordEncoder   passwordEncoder,
        EmailService      emailService
    ) {
        this.parentRepository  = parentRepository;
        this.userRepository    = userRepository;
        this.studentRepository = studentRepository;
        this.passwordEncoder   = passwordEncoder;
        this.emailService      = emailService;
    }

    // ════════════════════════════════════════════════════
    // GET ALL
    // ════════════════════════════════════════════════════
    public Page<Parent> getAll(int page, int size, String search) {
        Pageable pageable = PageRequest.of(
            page - 1,
            size,
            Sort.by(Sort.Direction.DESC, "createdAt")
        );
        return parentRepository.searchParents(search, pageable);
    }

    // ════════════════════════════════════════════════════
    // GET ONE
    // ════════════════════════════════════════════════════
    public Parent getById(String id) {
        return parentRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("PARENT_NOT_FOUND"));
    }

    // ════════════════════════════════════════════════════
    // GET BY USER ID (for parent viewing own data)
    // ════════════════════════════════════════════════════
    public Parent getByUserId(String userId) {
        return parentRepository.findByUserId(userId)
            .orElseThrow(() -> new RuntimeException("PARENT_NOT_FOUND"));
    }

    // ════════════════════════════════════════════════════
    // GET BY USER EMAIL
    // ════════════════════════════════════════════════════
    public Parent getByUserEmail(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("USER_NOT_FOUND"));
        return parentRepository.findByUserId(user.getId())
            .orElseThrow(() -> new RuntimeException("PARENT_NOT_FOUND"));
    }

    // ════════════════════════════════════════════════════
    // CREATE — admin creates parent and links to students
    // ════════════════════════════════════════════════════
    @Transactional
    public Parent create(CreateParentRequest request) {

        // Check email uniqueness
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("EMAIL_TAKEN");
        }

        // Validate all student IDs exist
        List<Student> children = new ArrayList<>();
        for (String studentId : request.getStudentIds()) {
            Student student = studentRepository.findById(studentId)
                .orElseThrow(() ->
                    new RuntimeException("STUDENT_NOT_FOUND:" + studentId)
                );
            children.add(student);
        }

        // Create User
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());
        user.setRole(User.Role.PARENT);
        user.setStatus(User.Status.ACTIVE);
        User savedUser = userRepository.save(user);

        // Create Parent linked to User and children
        Parent parent = new Parent();
        parent.setUser(savedUser);
        parent.setChildren(children);
        parent.setRelationship(request.getRelationship());
        Parent saved = parentRepository.save(parent);

        // Send welcome email
        emailService.sendParentCreatedEmail(
            savedUser.getEmail(),
            savedUser.getFullName(),
            children.stream()
                .map(s -> s.getUser().getFullName())
                .toList()
        );

        return saved;
    }

    // ════════════════════════════════════════════════════
    // UPDATE
    // ════════════════════════════════════════════════════
    @Transactional
    public Parent update(String id, UpdateParentRequest request) {

        Parent parent = parentRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("PARENT_NOT_FOUND"));

        User user = parent.getUser();

        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        userRepository.save(user);

        if (request.getRelationship() != null) {
            parent.setRelationship(request.getRelationship());
        }

        // Replace children list if new studentIds provided
        if (request.getStudentIds() != null
                && !request.getStudentIds().isEmpty()) {
            List<Student> children = new ArrayList<>();
            for (String studentId : request.getStudentIds()) {
                Student student = studentRepository
                    .findById(studentId)
                    .orElseThrow(() ->
                        new RuntimeException("STUDENT_NOT_FOUND")
                    );
                children.add(student);
            }
            parent.setChildren(children);
        }

        return parentRepository.save(parent);
    }

    // ════════════════════════════════════════════════════
    // DELETE
    // ════════════════════════════════════════════════════
    @Transactional
    public void delete(String id) {
        Parent parent = parentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("PARENT_NOT_FOUND"));

        // Clear junction table first
        parent.getChildren().clear();
        parentRepository.saveAndFlush(parent);

        // Delete parent record directly
        parentRepository.delete(parent);

        // Then delete the user
        userRepository.delete(parent.getUser());
    }

    // ════════════════════════════════════════════════════
    // FIND PARENTS OF A STUDENT
    // Used by attendance to notify parents
    // ════════════════════════════════════════════════════
    public List<Parent> getParentsByStudentId(String studentId) {
        return parentRepository.findByChildId(studentId);
    }
}
