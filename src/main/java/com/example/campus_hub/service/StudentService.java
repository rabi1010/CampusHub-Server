package com.example.campus_hub.service;

import com.example.campus_hub.dto.CreateStudentRequest;
import com.example.campus_hub.dto.UpdateStudentRequest;
import com.example.campus_hub.dto.StudentResponse;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.campus_hub.entity.Student;
import com.example.campus_hub.entity.User;
import com.example.campus_hub.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Service
public class StudentService {

    private final StudentRepository    studentRepository;
    private final UserRepository       userRepository;
    private final DepartmentRepository departmentRepository;
    private final BatchRepository      batchRepository;
    private final MarkRepository       markRepository;
    private final AttendanceRepository attendanceRepository;
    private final PasswordEncoder      passwordEncoder;
    private final EmailService emailService;
    private final Cloudinary cloudinary;

    // Constructor injection — no Lombok @RequiredArgsConstructor
    public StudentService(
            StudentRepository    studentRepository,
            UserRepository       userRepository,
            DepartmentRepository departmentRepository,
            BatchRepository      batchRepository,
            PasswordEncoder      passwordEncoder,
            EmailService         emailService,
            MarkRepository       markRepository,
            AttendanceRepository attendanceRepository,
            Cloudinary            cloudinary
    ) {
        this.studentRepository    = studentRepository;
        this.userRepository       = userRepository;
        this.departmentRepository = departmentRepository;
        this.batchRepository      = batchRepository;
        this.passwordEncoder      = passwordEncoder;
        this.emailService         = emailService;
        this.markRepository       = markRepository;
        this.attendanceRepository = attendanceRepository;
        this.cloudinary            = cloudinary;
    }

    // ════════════════════════════════════════════════════
    // GET ALL with search and pagination
    // ════════════════════════════════════════════════════
    @Transactional(readOnly = true)
    public Page<StudentResponse> getAll(
            int    page,
            int    size,
            String search,
            String departmentId
    ) {
        // PageRequest creates a Pageable from page number and size
        // page - 1 because Spring pages are 0-indexed
        // (page 1 from client = page 0 in Spring)
        Pageable pageable = PageRequest.of(
                page - 1,
                size,
                Sort.by(Sort.Direction.DESC, "admissionDate")
        );

        return studentRepository.searchStudents(
                search,
                departmentId,
                pageable
        ).map(StudentResponse::from);
    }

    // ════════════════════════════════════════════════════
    // GET ONE
    // ════════════════════════════════════════════════════
    public Student getById(String id) {
        return studentRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("STUDENT_NOT_FOUND")
                );
    }

    @Transactional(readOnly = true)
    public Student getByUserEmail(String email) {
        return studentRepository.findByUserId(
                        userRepository.findByEmail(email)
                                .orElseThrow(() -> new RuntimeException("STUDENT_NOT_FOUND"))
                                .getId())
                .orElseThrow(() -> new RuntimeException("STUDENT_NOT_FOUND"));
    }

    // ════════════════════════════════════════════════════
    // CREATE
    // @Transactional is the Java equivalent of Prisma's $transaction
    // If ANY exception is thrown inside this method,
    // ALL database changes are rolled back automatically
    // ════════════════════════════════════════════════════
    @Transactional
    public Student create(CreateStudentRequest request) {

        // Check email uniqueness
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("EMAIL_TAKEN");
        }

        // Check roll number uniqueness
        if (studentRepository.existsByRollNo(request.getRollNo())) {
            throw new RuntimeException("ROLL_NO_TAKEN");
        }

        // Find department (must exist)
        var department = departmentRepository
                .findById(request.getDepartmentId())
                .orElseThrow(() -> new RuntimeException("DEPARTMENT_NOT_FOUND"));

        // Find batch (must exist)
        var batch = batchRepository
                .findById(request.getBatchId())
                .orElseThrow(() -> new RuntimeException("BATCH_NOT_FOUND"));

        // Step 1: Create User
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());
        user.setRole(User.Role.STUDENT);
        user.setStatus(User.Status.ACTIVE);
        User savedUser = userRepository.save(user);
        emailService.sendStudentCreatedEmail(
                savedUser.getEmail(),
                savedUser.getFullName(),
                request.getRollNo()
        );

        // Step 2: Create Student linked to User
        // Because @Transactional wraps this whole method,
        // if this save fails, the userRepository.save() above
        // is also rolled back automatically
        Student student = new Student();
        student.setUser(savedUser);
        student.setRollNo(request.getRollNo());
        student.setDepartment(department);
        student.setBatch(batch);
        student.setAddress(request.getAddress());

        return studentRepository.save(student);
    }

    // ════════════════════════════════════════════════════
    // UPDATE
    // ════════════════════════════════════════════════════
    @Transactional
    public Student update(String id, UpdateStudentRequest request) {

        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("STUDENT_NOT_FOUND"));

        User user = student.getUser();

        // Only update fields that are NOT null
        // This is the partial update / PATCH pattern
        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        userRepository.save(user);

        if (request.getDepartmentId() != null) {
            var dept = departmentRepository
                    .findById(request.getDepartmentId())
                    .orElseThrow(() -> new RuntimeException("DEPARTMENT_NOT_FOUND"));
            student.setDepartment(dept);
        }

        if (request.getBatchId() != null) {
            var batch = batchRepository
                    .findById(request.getBatchId())
                    .orElseThrow(() -> new RuntimeException("BATCH_NOT_FOUND"));
            student.setBatch(batch);
        }

        if (request.getAddress() != null) {
            student.setAddress(request.getAddress());
        }

        return studentRepository.save(student);
    }

    // ════════════════════════════════════════════════════
    // DELETE
    // ════════════════════════════════════════════════════
    @Transactional
    public void delete(String id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("STUDENT_NOT_FOUND"));

        studentRepository.deleteParentLinks(id);
        markRepository.deleteByStudentId(id);
        attendanceRepository.deleteByStudentId(id);
        studentRepository.delete(student);
    }

    // ════════════════════════════════════════════════════
    // UPLOAD IMAGE — store as BLOB in database
    // MultipartFile is Spring's equivalent of multer's req.file
    // ════════════════════════════════════════════════════
    @Transactional
    public void uploadImage(String id, MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new RuntimeException("IMAGE_UPLOAD_FAILED");
        }

        // Validate file type
        List<String> allowed = Arrays.asList(
                "image/jpeg", "image/png", "image/webp"
        );

        if (!allowed.contains(file.getContentType())) {
            throw new RuntimeException("INVALID_IMAGE_TYPE");
        }

        // Validate file size (max 2MB)
        if (file.getSize() > 2 * 1024 * 1024) {
            throw new RuntimeException("IMAGE_TOO_LARGE");
        }

        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("STUDENT_NOT_FOUND"));

        try {
            var result = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", "campus-hub/profile-images",
                            "public_id", student.getUser().getId(),
                            "overwrite", true,
                            "resource_type", "image"
                    )
            );
            student.getUser().setProfileImageUrl((String) result.get("secure_url"));
            student.getUser().setProfileImage(null);
            student.getUser().setProfileImageContentType(file.getContentType());
            userRepository.save(student.getUser());
        } catch (Exception e) {
            System.err.println("Student image upload failed: " + e.getMessage());
            throw new RuntimeException("IMAGE_UPLOAD_FAILED");
        }
    }

    // ════════════════════════════════════════════════════
    // GET IMAGE — retrieve BLOB from database
    // ════════════════════════════════════════════════════
    public byte[] getImage(String id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("STUDENT_NOT_FOUND"));

        byte[] image = student.getUser().getProfileImage();
        if (image == null) {
            throw new RuntimeException("IMAGE_NOT_FOUND");
        }

        return image;
    }

    public String getImageUrl(String id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("STUDENT_NOT_FOUND"));
        return student.getUser().getProfileImageUrl();
    }

    public String getImageContentType(String id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("STUDENT_NOT_FOUND"));
        if (student.getUser().getProfileImage() == null) {
            throw new RuntimeException("IMAGE_NOT_FOUND");
        }
        return student.getUser().getProfileImageContentType();
    }


}
