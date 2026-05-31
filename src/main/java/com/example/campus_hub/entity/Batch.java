package com.example.campus_hub.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "batches")
public class Batch {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String name;

    @ManyToOne
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    private int startYear;
    private int endYear;

    public Batch() {}

    public String     getId()         { return id; }
    public String     getName()       { return name; }
    public Department getDepartment() { return department; }
    public int        getStartYear()  { return startYear; }
    public int        getEndYear()    { return endYear; }

    public void setId(String id)               { this.id = id; }
    public void setName(String name)           { this.name = name; }
    public void setDepartment(Department dept) { this.department = dept; }
    public void setStartYear(int startYear)    { this.startYear = startYear; }
    public void setEndYear(int endYear)        { this.endYear = endYear; }
}