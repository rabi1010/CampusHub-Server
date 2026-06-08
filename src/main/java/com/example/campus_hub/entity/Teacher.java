package com.example.campus_hub.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "teachers")
public class Teacher {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @Column(unique = true, nullable = false)
    private String employeeId;

    private String qualification;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public Teacher() {}

    public String       getId()            { return id; }
    public User         getUser()          { return user; }
    public Department   getDepartment()    { return department; }
    public String       getEmployeeId()    { return employeeId; }
    public String       getQualification() { return qualification; }
    public LocalDateTime getCreatedAt()   { return createdAt; }
    public LocalDateTime getUpdatedAt()   { return updatedAt; }

    public void setId(String id)                   { this.id = id; }
    public void setUser(User user)                 { this.user = user; }
    public void setDepartment(Department dept)     { this.department = dept; }
    public void setEmployeeId(String employeeId)   { this.employeeId = employeeId; }
    public void setQualification(String q)         { this.qualification = q; }
}