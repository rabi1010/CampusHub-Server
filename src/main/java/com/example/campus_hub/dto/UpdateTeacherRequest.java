package com.example.campus_hub.dto;

public class UpdateTeacherRequest {

    private String fullName;
    private String phone;
    private String password;
    private String departmentId;
    private String qualification;

    public String getFullName()     { return fullName; }
    public String getPhone()        { return phone; }
    public String getPassword()     { return password; }
    public String getDepartmentId() { return departmentId; }
    public String getQualification(){ return qualification; }

    public void setFullName(String v)      { this.fullName = v; }
    public void setPhone(String v)         { this.phone = v; }
    public void setPassword(String v)      { this.password = v; }
    public void setDepartmentId(String v)  { this.departmentId = v; }
    public void setQualification(String v) { this.qualification = v; }
}