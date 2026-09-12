package com.example.campus_hub.dto;

import jakarta.validation.constraints.*;

public class CreateTeacherRequest {

    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Enter a valid email")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    private String phone;

    @NotBlank(message = "Employee ID is required")
    private String employeeId;

    @NotBlank(message = "Department is required")
    private String departmentId;

    private String qualification;

    public String getFullName()     { return fullName; }
    public String getEmail()        { return email; }
    public String getPassword()     { return password; }
    public String getPhone()        { return phone; }
    public String getEmployeeId()   { return employeeId; }
    public String getDepartmentId() { return departmentId; }
    public String getQualification(){ return qualification; }

    public void setFullName(String v)      { this.fullName = v; }
    public void setEmail(String v)         { this.email = v; }
    public void setPassword(String v)      { this.password = v; }
    public void setPhone(String v)         { this.phone = v; }
    public void setEmployeeId(String v)    { this.employeeId = v; }
    public void setDepartmentId(String v)  { this.departmentId = v; }
    public void setQualification(String v) { this.qualification = v; }
}