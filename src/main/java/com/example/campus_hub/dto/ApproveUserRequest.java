package com.example.campus_hub.dto;

import java.util.List;

public class ApproveUserRequest {

    private String       role;
    private String       rollNo;         // STUDENT approval
    private String       employeeId;     // TEACHER approval
    private String       departmentId;
    private String       batchId;
    private String       address;
    private String       qualification;  // TEACHER approval
    private List<String> studentIds;     // PARENT approval
    private String       relationship;   // PARENT approval

    public String       getRole()         { return role; }
    public String       getRollNo()       { return rollNo; }
    public String       getEmployeeId()   { return employeeId; }
    public String       getDepartmentId() { return departmentId; }
    public String       getBatchId()      { return batchId; }
    public String       getAddress()      { return address; }
    public String       getQualification(){ return qualification; }
    public List<String> getStudentIds()   { return studentIds; }
    public String       getRelationship() { return relationship; }

    public void setRole(String v)              { this.role = v; }
    public void setRollNo(String v)            { this.rollNo = v; }
    public void setEmployeeId(String v)        { this.employeeId = v; }
    public void setDepartmentId(String v)      { this.departmentId = v; }
    public void setBatchId(String v)           { this.batchId = v; }
    public void setAddress(String v)           { this.address = v; }
    public void setQualification(String v)     { this.qualification = v; }
    public void setStudentIds(List<String> v)  { this.studentIds = v; }
    public void setRelationship(String v)      { this.relationship = v; }
}