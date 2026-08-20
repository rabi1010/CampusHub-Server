package com.example.campus_hub.dto;

import com.example.campus_hub.entity.Batch;
import com.example.campus_hub.entity.Department;
import com.example.campus_hub.entity.Student;
import com.example.campus_hub.entity.User;

import java.time.LocalDateTime;

public record StudentResponse(
        String id,
        UserSummary user,
        DepartmentSummary department,
        BatchSummary batch,
        String rollNo,
        String address,
        LocalDateTime admissionDate,
        LocalDateTime updatedAt
) {
    public static StudentResponse from(Student student) {
        return new StudentResponse(
                student.getId(),
                UserSummary.from(student.getUser()),
                DepartmentSummary.from(student.getDepartment()),
                BatchSummary.from(student.getBatch()),
                student.getRollNo(),
                student.getAddress(),
                student.getAdmissionDate(),
                student.getUpdatedAt()
        );
    }

    public record UserSummary(
            String id,
            String email,
            String fullName,
            String phone,
            User.Role role,
            User.Status status,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        static UserSummary from(User user) {
            return new UserSummary(
                    user.getId(), user.getEmail(), user.getFullName(), user.getPhone(),
                    user.getRole(), user.getStatus(), user.getCreatedAt(), user.getUpdatedAt()
            );
        }
    }

    public record DepartmentSummary(String id, String name, String code) {
        static DepartmentSummary from(Department department) {
            return new DepartmentSummary(department.getId(), department.getName(), department.getCode());
        }
    }

    public record BatchSummary(String id, String name) {
        static BatchSummary from(Batch batch) {
            return new BatchSummary(batch.getId(), batch.getName());
        }
    }
}
