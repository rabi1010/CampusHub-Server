package com.example.campus_hub.dto;

import com.example.campus_hub.entity.Mark;

import java.time.LocalDateTime;

public record MarkResponse(
        String id,
        StudentInfo student,
        CourseInfo course,
        UserInfo uploadedBy,
        Mark.ExamType examType,
        float marksObtained,
        float totalMarks,
        int semester,
        LocalDateTime createdAt
) {
    public static MarkResponse from(Mark mark) {
        return new MarkResponse(
                mark.getId(),
                new StudentInfo(
                        mark.getStudent().getId(),
                        mark.getStudent().getRollNo(),
                        new UserInfo(
                                mark.getStudent().getUser().getFullName(),
                                mark.getStudent().getUser().getEmail()
                        )
                ),
                new CourseInfo(
                        mark.getCourse().getId(),
                        mark.getCourse().getName(),
                        mark.getCourse().getCode()
                ),
                new UserInfo(
                        mark.getUploadedBy().getFullName(),
                        mark.getUploadedBy().getEmail()
                ),
                mark.getExamType(),
                mark.getMarksObtained(),
                mark.getTotalMarks(),
                mark.getSemester(),
                mark.getCreatedAt()
        );
    }

    public record StudentInfo(String id, String rollNo, UserInfo user) {}
    public record CourseInfo(String id, String name, String code) {}
    public record UserInfo(String fullName, String email) {}
}
