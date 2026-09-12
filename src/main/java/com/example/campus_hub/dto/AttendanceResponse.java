package com.example.campus_hub.dto;

import com.example.campus_hub.entity.Attendance;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record AttendanceResponse(
        String id,
        StudentInfo student,
        CourseInfo course,
        UserInfo markedBy,
        Attendance.Status status,
        LocalDate date,
        LocalDateTime createdAt
) {
    public static AttendanceResponse from(Attendance attendance) {
        return new AttendanceResponse(
                attendance.getId(),
                new StudentInfo(
                        attendance.getStudent().getId(),
                        attendance.getStudent().getRollNo(),
                        new UserInfo(
                                attendance.getStudent().getUser().getFullName(),
                                attendance.getStudent().getUser().getEmail()
                        )
                ),
                new CourseInfo(
                        attendance.getCourse().getId(),
                        attendance.getCourse().getName(),
                        attendance.getCourse().getCode()
                ),
                new UserInfo(
                        attendance.getMarkedBy().getFullName(),
                        attendance.getMarkedBy().getEmail()
                ),
                attendance.getStatus(),
                attendance.getDate(),
                attendance.getCreatedAt()
        );
    }

    public record StudentInfo(String id, String rollNo, UserInfo user) {}
    public record CourseInfo(String id, String name, String code) {}
    public record UserInfo(String fullName, String email) {}
}
