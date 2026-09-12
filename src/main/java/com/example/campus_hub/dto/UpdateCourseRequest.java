package com.example.campus_hub.dto;

public class UpdateCourseRequest {

    private String name;
    private String departmentId;
    private Integer credits;
    private Integer semester;
    private String description;

    public String  getName()        { return name; }
    public String  getDepartmentId(){ return departmentId; }
    public Integer getCredits()     { return credits; }
    public Integer getSemester()    { return semester; }
    public String  getDescription() { return description; }

    public void setName(String v)         { this.name = v; }
    public void setDepartmentId(String v) { this.departmentId = v; }
    public void setCredits(Integer v)     { this.credits = v; }
    public void setSemester(Integer v)    { this.semester = v; }
    public void setDescription(String v)  { this.description = v; }
}