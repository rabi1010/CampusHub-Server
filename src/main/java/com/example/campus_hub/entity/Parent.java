package com.example.campus_hub.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "parents")
public class Parent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    // One parent has one User account (login credentials)
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    private User user;

    // One parent can have MULTIPLE children
    // This is the Many-to-Many through a join table
    // parent_students table:
    //   parent_id → parents.id
    //   student_id → students.id
    @ManyToMany
    @JoinTable(
        name               = "parent_students",
        joinColumns        = @JoinColumn(name = "parent_id"),
        inverseJoinColumns = @JoinColumn(name = "student_id")
    )
    private List<Student> children = new ArrayList<>();

    private String relationship; // Father, Mother, Guardian

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public Parent() {}

    public String              getId()           { return id; }
    public User                getUser()         { return user; }
    public List<Student>       getChildren()     { return children; }
    public String              getRelationship() { return relationship; }
    public LocalDateTime       getCreatedAt()    { return createdAt; }
    public LocalDateTime       getUpdatedAt()    { return updatedAt; }

    public void setId(String id)                       { this.id = id; }
    public void setUser(User user)                     { this.user = user; }
    public void setChildren(List<Student> children)    { this.children = children; }
    public void setRelationship(String relationship)   { this.relationship = relationship; }
}
