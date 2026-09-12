package com.example.campus_hub.dto;

import com.example.campus_hub.entity.Parent;

import java.time.LocalDateTime;
import java.util.List;

public record ParentResponse(
        String id,
        StudentResponse.UserSummary user,
        List<StudentResponse> children,
        String relationship,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ParentResponse from(Parent parent) {
        return new ParentResponse(
                parent.getId(),
                StudentResponse.UserSummary.from(parent.getUser()),
                parent.getChildren().stream().map(StudentResponse::from).toList(),
                parent.getRelationship(),
                parent.getCreatedAt(),
                parent.getUpdatedAt()
        );
    }
}
