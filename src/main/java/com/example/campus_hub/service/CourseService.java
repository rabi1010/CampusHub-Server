package com.example.campus_hub.service;

import com.example.campus_hub.dto.CreateCourseRequest;
import com.example.campus_hub.dto.UpdateCourseRequest;
import com.example.campus_hub.entity.Course;
import com.example.campus_hub.repository.CourseRepository;
import com.example.campus_hub.repository.DepartmentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CourseService {

    private final CourseRepository     courseRepository;
    private final DepartmentRepository departmentRepository;

    public CourseService(
            CourseRepository     courseRepository,
            DepartmentRepository departmentRepository
    ) {
        this.courseRepository     = courseRepository;
        this.departmentRepository = departmentRepository;
    }

    // ════════════════════════════════════════════════════
    // GET ALL
    // ════════════════════════════════════════════════════
    public Page<Course> getAll(
            int page, int size,
            String search,
            String departmentId,
            int semester) {

        Pageable pageable = PageRequest.of(
                page - 1, size,
                Sort.by(Sort.Direction.ASC, "semester")
        );
        return courseRepository.searchCourses(
                search, departmentId, semester, pageable
        );
    }

    // ════════════════════════════════════════════════════
    // GET ONE
    // ════════════════════════════════════════════════════
    public Course getById(String id) {
        return courseRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("COURSE_NOT_FOUND")
                );
    }

    // ════════════════════════════════════════════════════
    // CREATE
    // ════════════════════════════════════════════════════
    @Transactional
    public Course create(CreateCourseRequest request) {

        if (courseRepository.existsByCode(request.getCode())) {
            throw new RuntimeException("COURSE_CODE_TAKEN");
        }

        var department = departmentRepository
                .findById(request.getDepartmentId())
                .orElseThrow(() ->
                        new RuntimeException("DEPARTMENT_NOT_FOUND")
                );

        Course course = new Course();
        course.setName(request.getName());
        course.setCode(request.getCode());
        course.setDepartment(department);
        course.setCredits(request.getCredits());
        course.setSemester(request.getSemester());
        course.setDescription(request.getDescription());

        return courseRepository.save(course);
    }

    // ════════════════════════════════════════════════════
    // UPDATE
    // ════════════════════════════════════════════════════
    @Transactional
    public Course update(String id, UpdateCourseRequest request) {

        Course course = courseRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("COURSE_NOT_FOUND")
                );

        if (request.getName() != null) {
            course.setName(request.getName());
        }
        if (request.getDepartmentId() != null) {
            var dept = departmentRepository
                    .findById(request.getDepartmentId())
                    .orElseThrow(() ->
                            new RuntimeException("DEPARTMENT_NOT_FOUND")
                    );
            course.setDepartment(dept);
        }
        if (request.getCredits() != null) {
            course.setCredits(request.getCredits());
        }
        if (request.getSemester() != null) {
            course.setSemester(request.getSemester());
        }
        if (request.getDescription() != null) {
            course.setDescription(request.getDescription());
        }

        return courseRepository.save(course);
    }

    // ════════════════════════════════════════════════════
    // DELETE
    // ════════════════════════════════════════════════════
    @Transactional
    public void delete(String id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("COURSE_NOT_FOUND")
                );
        courseRepository.delete(course);
    }
}