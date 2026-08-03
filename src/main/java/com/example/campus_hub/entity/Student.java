package com.example.campus_hub.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "students")
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @ManyToOne
    @JoinColumn(name = "batch_id", nullable = false)
    private Batch batch;

    @Column(unique = true, nullable = false)
    private String rollNo;

    private String address;

    @CreationTimestamp
    private LocalDateTime admissionDate;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public Student() {}

    public String        getId()            { return id; }
    public User          getUser()          { return user; }
    public Department    getDepartment()    { return department; }
    public Batch         getBatch()         { return batch; }
    public String        getRollNo()        { return rollNo; }
    public String        getAddress()       { return address; }
    public LocalDateTime getAdmissionDate() { return admissionDate; }
    public LocalDateTime getUpdatedAt()     { return updatedAt; }

    public void setId(String id)                   { this.id = id; }
    public void setUser(User user)                 { this.user = user; }
    public void setDepartment(Department dept)     { this.department = dept; }
    public void setBatch(Batch batch)              { this.batch = batch; }
    public void setRollNo(String rollNo)           { this.rollNo = rollNo; }
    public void setAddress(String address)         { this.address = address; }
    public void setAdmissionDate(LocalDateTime dt) { this.admissionDate = dt; }
}