package com.example.campus_hub.service;

import com.example.campus_hub.entity.Attendance;
import com.example.campus_hub.repository.*;
import org.springframework.stereotype.Service;

@Service
public class StatsService {

    private final UserRepository       userRepository;
    private final StudentRepository    studentRepository;
    private final TeacherRepository    teacherRepository;
    private final ParentRepository     parentRepository;
    private final CourseRepository     courseRepository;
    private final NoticeRepository     noticeRepository;
    private final AttendanceRepository attendanceRepository;

    public StatsService(
            UserRepository       userRepository,
            StudentRepository    studentRepository,
            TeacherRepository    teacherRepository,
            ParentRepository     parentRepository,
            CourseRepository     courseRepository,
            NoticeRepository     noticeRepository,
            AttendanceRepository attendanceRepository
    ) {
        this.userRepository       = userRepository;
        this.studentRepository    = studentRepository;
        this.teacherRepository    = teacherRepository;
        this.parentRepository     = parentRepository;
        this.courseRepository     = courseRepository;
        this.noticeRepository     = noticeRepository;
        this.attendanceRepository = attendanceRepository;
    }

    // Admin dashboard stats
    public AdminStats getAdminStats() {
        long totalStudents   = studentRepository.count();
        long totalTeachers   = teacherRepository.count();
        long totalParents    = parentRepository.count();
        long totalCourses    = courseRepository.count();
        long totalNotices    = noticeRepository.count();
        long pendingApprovals = userRepository
                .findByStatus(
                        com.example.campus_hub.entity.User.Status.PENDING
                ).size();
        long totalAbsent = attendanceRepository
                .countByStatus(Attendance.Status.ABSENT);
        long totalPresent = attendanceRepository
                .countByStatus(Attendance.Status.PRESENT);

        return new AdminStats(
                totalStudents,
                totalTeachers,
                totalParents,
                totalCourses,
                totalNotices,
                pendingApprovals,
                totalPresent,
                totalAbsent
        );
    }

    public record AdminStats(
            long totalStudents,
            long totalTeachers,
            long totalParents,
            long totalCourses,
            long totalNotices,
            long pendingApprovals,
            long totalPresentRecords,
            long totalAbsentRecords
    ) {}
}