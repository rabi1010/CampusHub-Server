package com.example.campus_hub.service;

import com.example.campus_hub.dto.CreateParentRequest;
import com.example.campus_hub.dto.UpdateParentRequest;
import com.example.campus_hub.dto.ParentResponse;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
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
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
public class ParentService {

    private final ParentRepository  parentRepository;
    private final UserRepository    userRepository;
    private final StudentRepository studentRepository;
    private final PasswordEncoder   passwordEncoder;
    private final EmailService      emailService;
    private final Cloudinary         cloudinary;

    public ParentService(
        ParentRepository  parentRepository,
        UserRepository    userRepository,
        StudentRepository studentRepository,
        PasswordEncoder   passwordEncoder,
        EmailService      emailService,
        Cloudinary        cloudinary
    ) {
        this.parentRepository  = parentRepository;
        this.userRepository    = userRepository;
        this.studentRepository = studentRepository;
        this.passwordEncoder   = passwordEncoder;
        this.emailService      = emailService;
        this.cloudinary        = cloudinary;
    }

    // ════════════════════════════════════════════════════
    // GET ALL
    // ════════════════════════════════════════════════════
    @Transactional(readOnly = true)
    public Page<ParentResponse> getAll(int page, int size, String search) {
        Pageable pageable = PageRequest.of(
            page - 1,
            size,
            Sort.by(Sort.Direction.DESC, "createdAt")
        );
        return parentRepository.searchParents(search, pageable)
            .map(ParentResponse::from);
    }

    // ════════════════════════════════════════════════════
    // GET ONE
    // ════════════════════════════════════════════════════
    @Transactional(readOnly = true)
    public ParentResponse getById(String id) {
        Parent parent = parentRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("PARENT_NOT_FOUND"));
        return ParentResponse.from(parent);
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
    @Transactional(readOnly = true)
    public ParentResponse getByUserEmail(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("USER_NOT_FOUND"));
        Parent parent = parentRepository.findByUserId(user.getId())
            .orElseThrow(() -> new RuntimeException("PARENT_NOT_FOUND"));
        return ParentResponse.from(parent);
    }

    @Transactional
    public void uploadImage(String id, MultipartFile file) {
        validateImage(file);
        Parent parent = parentRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("PARENT_NOT_FOUND"));
        try {
            var result = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                "folder", "campus-hub/profile-images",
                "public_id", parent.getUser().getId(),
                "overwrite", true,
                "resource_type", "image"
            ));
            parent.getUser().setProfileImageUrl((String) result.get("secure_url"));
            parent.getUser().setProfileImage(null);
            parent.getUser().setProfileImageContentType(file.getContentType());
            userRepository.save(parent.getUser());
        } catch (Exception e) {
            throw new RuntimeException("IMAGE_UPLOAD_FAILED");
        }
    }

    public String getImageUrl(String id) {
        Parent parent = parentRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("PARENT_NOT_FOUND"));
        return parent.getUser().getProfileImageUrl();
    }

    private void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) throw new RuntimeException("IMAGE_UPLOAD_FAILED");
        if (!List.of("image/jpeg", "image/png", "image/webp").contains(file.getContentType())) {
            throw new RuntimeException("INVALID_IMAGE_TYPE");
        }
        if (file.getSize() > 2 * 1024 * 1024) throw new RuntimeException("IMAGE_TOO_LARGE");
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
