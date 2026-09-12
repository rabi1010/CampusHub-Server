package com.example.campus_hub.service;

import com.example.campus_hub.dto.UploadMarksRequest;
import com.example.campus_hub.entity.Mark;
import com.example.campus_hub.entity.User;
import com.example.campus_hub.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class MarkService {

    private final MarkRepository    markRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository  courseRepository;
    private final UserRepository    userRepository;

    public MarkService(
            MarkRepository    markRepository,
            StudentRepository studentRepository,
            CourseRepository  courseRepository,
            UserRepository    userRepository
    ) {
        this.markRepository    = markRepository;
        this.studentRepository = studentRepository;
        this.courseRepository  = courseRepository;
        this.userRepository    = userRepository;
    }

    // ════════════════════════════════════════════════════
    // UPLOAD MARKS — teacher uploads for whole class
    // If mark already exists for same exam type → update it
    // ════════════════════════════════════════════════════
    @Transactional
    public List<Mark> uploadMarks(
            UploadMarksRequest request,
            String teacherEmail) {

        User teacher = userRepository
                .findByEmail(teacherEmail)
                .orElseThrow(() ->
                        new RuntimeException("USER_NOT_FOUND")
                );

        var course = courseRepository
                .findById(request.getCourseId())
                .orElseThrow(() ->
                        new RuntimeException("COURSE_NOT_FOUND")
                );

        Mark.ExamType examType;
        try {
            examType = Mark.ExamType.valueOf(
                    request.getExamType().toUpperCase()
            );
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("INVALID_EXAM_TYPE");
        }

        List<Mark> saved = new ArrayList<>();

        for (UploadMarksRequest.MarkRecord record
                : request.getRecords()) {

            var student = studentRepository
                    .findById(record.getStudentId())
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "STUDENT_NOT_FOUND:" + record.getStudentId()
                            )
                    );

            // Check if mark already exists for this combination
            // If yes → update it (upsert pattern)
            Optional<Mark> existing =
                    markRepository.findByStudentIdAndCourseIdAndExamType(
                            student.getId(),
                            course.getId(),
                            examType
                    );

            Mark mark = existing.orElse(new Mark());
            mark.setStudent(student);
            mark.setCourse(course);
            mark.setUploadedBy(teacher);
            mark.setExamType(examType);
            mark.setMarksObtained(record.getMarksObtained());
            mark.setTotalMarks(record.getTotalMarks());
            mark.setSemester(request.getSemester());

            saved.add(markRepository.save(mark));
        }

        return saved;
    }

    // ════════════════════════════════════════════════════
    // GET BY STUDENT — student views own marks
    // ════════════════════════════════════════════════════
    @Transactional(readOnly = true)
    public List<Mark> getByStudent(String studentId) {
        return markRepository.findByStudentId(studentId);
    }

    // ════════════════════════════════════════════════════
    // GET BY COURSE — teacher views all marks for a course
    // ════════════════════════════════════════════════════
    @Transactional(readOnly = true)
    public List<Mark> getByCourse(String courseId) {
        return markRepository.findByCourseId(courseId);
    }

    // ════════════════════════════════════════════════════
    // GET BY STUDENT + COURSE
    // ════════════════════════════════════════════════════
    @Transactional(readOnly = true)
    public List<Mark> getByStudentAndCourse(
            String studentId, String courseId) {
        return markRepository.findByStudentIdAndCourseId(
                studentId, courseId
        );
    }

    // ════════════════════════════════════════════════════
    // GET GPA — average percentage across all marks
    // ════════════════════════════════════════════════════
    public double getGpa(String studentId) {
        Double avg = markRepository
                .calculateAveragePercentage(studentId);
        return avg != null
                ? Math.round(avg * 10.0) / 10.0
                : 0.0;
    }

    @Transactional(readOnly = true)
    public double getGpaForUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("USER_NOT_FOUND"));
        String studentId = studentRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("STUDENT_NOT_FOUND"))
                .getId();
        return getGpa(studentId);
    }
}
