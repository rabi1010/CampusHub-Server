package com.example.campus_hub.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "courses")
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String code;

    @ManyToOne
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    private int credits;
    private int semester;
    private String description;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public Course() {}

    public String     getId()          { return id; }
    public String     getName()        { return name; }
    public String     getCode()        { return code; }
    public Department getDepartment()  { return department; }
    public int        getCredits()     { return credits; }
    public int        getSemester()    { return semester; }
    public String     getDescription() { return description; }
    public LocalDateTime getCreatedAt(){ return createdAt; }
    public LocalDateTime getUpdatedAt(){ return updatedAt; }

    public void setId(String id)               { this.id = id; }
    public void setName(String name)           { this.name = name; }
    public void setCode(String code)           { this.code = code; }
    public void setDepartment(Department dept) { this.department = dept; }
    public void setCredits(int credits)        { this.credits = credits; }
    public void setSemester(int semester)      { this.semester = semester; }
    public void setDescription(String desc)    { this.description = desc; }
}