package com.example.campus_hub.dto;

import jakarta.validation.constraints.*;

public class CreateCourseRequest {

    @NotBlank(message = "Course name is required")
    private String name;

    @NotBlank(message = "Course code is required")
    private String code;

    @NotBlank(message = "Department is required")
    private String departmentId;

    @Min(value = 1, message = "Credits must be at least 1")
    private int credits;

    @Min(value = 1, message = "Semester must be at least 1")
    @Max(value = 8, message = "Semester cannot exceed 8")
    private int semester;

    private String description;

    public String getName()        { return name; }
    public String getCode()        { return code; }
    public String getDepartmentId(){ return departmentId; }
    public int    getCredits()     { return credits; }
    public int    getSemester()    { return semester; }
    public String getDescription() { return description; }

    public void setName(String v)         { this.name = v; }
    public void setCode(String v)         { this.code = v; }
    public void setDepartmentId(String v) { this.departmentId = v; }
    public void setCredits(int v)         { this.credits = v; }
    public void setSemester(int v)        { this.semester = v; }
    public void setDescription(String v)  { this.description = v; }
}