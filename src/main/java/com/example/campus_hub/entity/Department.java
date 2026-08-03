package com.example.campus_hub.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "departments")
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(unique = true, nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String code;

    private String description;

    public Department() {}

    public String getId()          { return id; }
    public String getName()        { return name; }
    public String getCode()        { return code; }
    public String getDescription() { return description; }

    public void setId(String id)                   { this.id = id; }
    public void setName(String name)               { this.name = name; }
    public void setCode(String code)               { this.code = code; }
    public void setDescription(String description) { this.description = description; }
}