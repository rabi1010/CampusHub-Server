package com.example.campus_hub.service;

import com.example.campus_hub.dto.CreateTeacherRequest;
import com.example.campus_hub.dto.UpdateTeacherRequest;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.campus_hub.entity.Teacher;
import com.example.campus_hub.entity.User;
import com.example.campus_hub.repository.DepartmentRepository;
import com.example.campus_hub.repository.AttendanceRepository;
import com.example.campus_hub.repository.MarkRepository;
import com.example.campus_hub.repository.NoticeRepository;
import com.example.campus_hub.repository.TeacherRepository;
import com.example.campus_hub.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class TeacherService {

    private final TeacherRepository    teacherRepository;
    private final UserRepository       userRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder      passwordEncoder;
    private final EmailService         emailService;
    private final MarkRepository       markRepository;
    private final AttendanceRepository attendanceRepository;
    private final NoticeRepository     noticeRepository;
    private final Cloudinary           cloudinary;

    public TeacherService(
            TeacherRepository    teacherRepository,
            UserRepository       userRepository,
            DepartmentRepository departmentRepository,
            PasswordEncoder      passwordEncoder,
            EmailService         emailService,
            MarkRepository       markRepository,
            AttendanceRepository attendanceRepository,
            NoticeRepository     noticeRepository,
            Cloudinary           cloudinary
    ) {
        this.teacherRepository    = teacherRepository;
        this.userRepository       = userRepository;
        this.departmentRepository = departmentRepository;
        this.passwordEncoder      = passwordEncoder;
        this.emailService         = emailService;
        this.markRepository       = markRepository;
        this.attendanceRepository = attendanceRepository;
        this.noticeRepository     = noticeRepository;
        this.cloudinary           = cloudinary;
    }

    // ════════════════════════════════════════════════════
    // GET ALL
    // ════════════════════════════════════════════════════
    public Page<Teacher> getAll(
            int page, int size,
            String search, String departmentId) {

        Pageable pageable = PageRequest.of(
                page - 1, size,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );
        return teacherRepository.searchTeachers(
                search, departmentId, pageable
        );
    }

    // ════════════════════════════════════════════════════
    // GET ONE
    // ════════════════════════════════════════════════════
    @Transactional(readOnly = true)
    public Teacher getById(String id) {
        return teacherRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("TEACHER_NOT_FOUND")
                );
    }

    // ════════════════════════════════════════════════════
    // CREATE
    // ════════════════════════════════════════════════════
    @Transactional
    public Teacher create(CreateTeacherRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("EMAIL_TAKEN");
        }

        if (teacherRepository.existsByEmployeeId(
                request.getEmployeeId())) {
            throw new RuntimeException("EMPLOYEE_ID_TAKEN");
        }

        var department = departmentRepository
                .findById(request.getDepartmentId())
                .orElseThrow(() ->
                        new RuntimeException("DEPARTMENT_NOT_FOUND")
                );

        // Create User
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(
                passwordEncoder.encode(request.getPassword())
        );
        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());
        user.setRole(User.Role.TEACHER);
        user.setStatus(User.Status.ACTIVE);
        User savedUser = userRepository.save(user);

        // Create Teacher
        Teacher teacher = new Teacher();
        teacher.setUser(savedUser);
        teacher.setDepartment(department);
        teacher.setEmployeeId(request.getEmployeeId());
        teacher.setQualification(request.getQualification());
        Teacher saved = teacherRepository.save(teacher);

        // Send welcome email
        emailService.sendTeacherCreatedEmail(
                savedUser.getEmail(),
                savedUser.getFullName(),
                request.getEmployeeId()
        );

        return saved;
    }

    // ════════════════════════════════════════════════════
    // UPDATE
    // ════════════════════════════════════════════════════
    @Transactional
    public Teacher update(String id, UpdateTeacherRequest request) {

        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("TEACHER_NOT_FOUND")
                );

        User user = teacher.getUser();

        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getPassword() != null
                && !request.getPassword().isEmpty()) {
            user.setPassword(
                    passwordEncoder.encode(request.getPassword())
            );
        }
        userRepository.save(user);

        if (request.getDepartmentId() != null) {
            var dept = departmentRepository
                    .findById(request.getDepartmentId())
                    .orElseThrow(() ->
                            new RuntimeException("DEPARTMENT_NOT_FOUND")
                    );
            teacher.setDepartment(dept);
        }

        if (request.getQualification() != null) {
            teacher.setQualification(request.getQualification());
        }

        return teacherRepository.save(teacher);
    }

    @Transactional(readOnly = true)
    public Teacher getByUserEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("USER_NOT_FOUND"));
        return teacherRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("TEACHER_NOT_FOUND"));
    }

    @Transactional
    public void uploadImage(String id, MultipartFile file) {
        validateImage(file);
        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("TEACHER_NOT_FOUND"));
        try {
            var result = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                    "folder", "campus-hub/profile-images",
                    "public_id", teacher.getUser().getId(),
                    "overwrite", true,
                    "resource_type", "image"
            ));
            teacher.getUser().setProfileImageUrl((String) result.get("secure_url"));
            teacher.getUser().setProfileImage(null);
            teacher.getUser().setProfileImageContentType(file.getContentType());
            userRepository.save(teacher.getUser());
        } catch (Exception e) {
            throw new RuntimeException("IMAGE_UPLOAD_FAILED");
        }
    }

    public String getImageUrl(String id) {
        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("TEACHER_NOT_FOUND"));
        return teacher.getUser().getProfileImageUrl();
    }

    private void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) throw new RuntimeException("IMAGE_UPLOAD_FAILED");
        if (!List.of("image/jpeg", "image/png", "image/webp").contains(file.getContentType())) {
            throw new RuntimeException("INVALID_IMAGE_TYPE");
        }
        if (file.getSize() > 2 * 1024 * 1024) throw new RuntimeException("IMAGE_TOO_LARGE");
    }

    // ════════════════════════════════════════════════════
    // DELETE
    // ════════════════════════════════════════════════════
    @Transactional
    public void delete(String id) {
        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("TEACHER_NOT_FOUND")
                );

        String userId = teacher.getUser().getId();
        markRepository.deleteByUploadedById(userId);
        attendanceRepository.deleteByMarkedById(userId);
        noticeRepository.deleteByCreatedById(userId);
        teacherRepository.delete(teacher);
    }
}
