package com.example.campus_hub.dto;

import jakarta.validation.constraints.*;

public class CreateStudentRequest {

    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 60, message = "Name must be 2-60 characters")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Enter a valid email")
    private String email;

    private String phone;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    @NotBlank(message = "Roll number is required")
    private String rollNo;

    @NotBlank(message = "Department is required")
    private String departmentId;

    @NotBlank(message = "Batch is required")
    private String batchId;

    private String address;

    public String getFullName()     { return fullName; }
    public String getEmail()        { return email; }
    public String getPhone()        { return phone; }
    public String getPassword()     { return password; }
    public String getRollNo()       { return rollNo; }
    public String getDepartmentId() { return departmentId; }
    public String getBatchId()      { return batchId; }
    public String getAddress()      { return address; }

    public void setFullName(String v)     { this.fullName = v; }
    public void setEmail(String v)        { this.email = v; }
    public void setPhone(String v)        { this.phone = v; }
    public void setPassword(String v)     { this.password = v; }
    public void setRollNo(String v)       { this.rollNo = v; }
    public void setDepartmentId(String v) { this.departmentId = v; }
    public void setBatchId(String v)      { this.batchId = v; }
    public void setAddress(String v)      { this.address = v; }
}