package com.example.campus_hub.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "marks")
public class Mark {

    public enum ExamType { INTERNAL, MIDTERM, FINAL }

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
    @JoinColumn(name = "uploaded_by", nullable = false)
    private User uploadedBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExamType examType;

    @Column(nullable = false)
    private float marksObtained;

    @Column(nullable = false)
    private float totalMarks;

    private int semester;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public Mark() {}

    public String    getId()            { return id; }
    public Student   getStudent()       { return student; }
    public Course    getCourse()        { return course; }
    public User      getUploadedBy()    { return uploadedBy; }
    public ExamType  getExamType()      { return examType; }
    public float     getMarksObtained() { return marksObtained; }
    public float     getTotalMarks()    { return totalMarks; }
    public int       getSemester()      { return semester; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public void setId(String id)                  { this.id = id; }
    public void setStudent(Student s)             { this.student = s; }
    public void setCourse(Course c)               { this.course = c; }
    public void setUploadedBy(User u)             { this.uploadedBy = u; }
    public void setExamType(ExamType examType)    { this.examType = examType; }
    public void setMarksObtained(float marks)     { this.marksObtained = marks; }
    public void setTotalMarks(float total)        { this.totalMarks = total; }
    public void setSemester(int semester)         { this.semester = semester; }
}