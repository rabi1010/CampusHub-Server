package com.example.campus_hub.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    public enum Role {
        ADMIN, TEACHER, STUDENT, PARENT, PENDING
    }

    public enum Status {
        ACTIVE, PENDING, SUSPENDED
    }
    // Add profileImage field
    @Lob          // @Lob tells JPA this is a Large Object (BLOB)
    @Column(name = "profile_image")
    private byte[] profileImage;

    // Add getter and setter
    public byte[] getProfileImage()              { return profileImage; }
    public void   setProfileImage(byte[] image)  { this.profileImage = image; }
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(unique = true, nullable = false)
    private String email;

    @JsonIgnore
    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String fullName;

    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Role role = Role.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Status status = Status.PENDING;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;


}