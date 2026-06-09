package com.example.campus_hub.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "notices")
public class Notice {

    public enum ForRole { ALL, STUDENT, TEACHER, PARENT }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @ManyToOne
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ForRole forRole = ForRole.ALL;

    private boolean urgent = false;

    @CreationTimestamp
    private LocalDateTime createdAt;

    public Notice() {}

    public String      getId()        { return id; }
    public String      getTitle()     { return title; }
    public String      getContent()   { return content; }
    public User        getCreatedBy() { return createdBy; }
    public ForRole     getForRole()   { return forRole; }
    public boolean     isUrgent()     { return urgent; }
    public LocalDateTime getCreatedAt(){ return createdAt; }

    public void setId(String id)            { this.id = id; }
    public void setTitle(String title)      { this.title = title; }
    public void setContent(String content)  { this.content = content; }
    public void setCreatedBy(User user)     { this.createdBy = user; }
    public void setForRole(ForRole forRole) { this.forRole = forRole; }
    public void setUrgent(boolean urgent)   { this.urgent = urgent; }
}