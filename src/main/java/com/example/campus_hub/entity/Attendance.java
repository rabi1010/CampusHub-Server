package com.example.campus_hub.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "attendance")
public class Attendance {

    public enum Status { PRESENT, ABSENT, LATE }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne
    @JoinColumn(name = "marked_by", nullable = false)
    private User markedBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Column(nullable = false)
    private LocalDate date;

    @CreationTimestamp
    private LocalDateTime createdAt;

    public Attendance() {}

    public String      getId()        { return id; }
    public Student     getStudent()   { return student; }
    public Course      getCourse()    { return course; }
    public User        getMarkedBy()  { return markedBy; }
    public Status      getStatus()    { return status; }
    public LocalDate   getDate()      { return date; }
    public LocalDateTime getCreatedAt(){ return createdAt; }

    public void setId(String id)           { this.id = id; }
    public void setStudent(Student s)      { this.student = s; }
    public void setCourse(Course c)        { this.course = c; }
    public void setMarkedBy(User u)        { this.markedBy = u; }
    public void setStatus(Status status)   { this.status = status; }
    public void setDate(LocalDate date)    { this.date = date; }
}