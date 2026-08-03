package com.example.campus_hub.dto;

// For updates, all fields are optional
// Only fields that are NOT null will be updated
// This is the PATCH pattern — only update what is sent

public class UpdateStudentRequest {

    private String fullName;
    private String phone;
    private String password;
    private String departmentId;
    private String batchId;
    private String address;

    public String getFullName()     { return fullName; }
    public String getPhone()        { return phone; }
    public String getPassword()     { return password; }
    public String getDepartmentId() { return departmentId; }
    public String getBatchId()      { return batchId; }
    public String getAddress()      { return address; }

    public void setFullName(String fullName)         { this.fullName = fullName; }
    public void setPhone(String phone)               { this.phone = phone; }
    public void setPassword(String password)         { this.password = password; }
    public void setDepartmentId(String departmentId) { this.departmentId = departmentId; }
    public void setBatchId(String batchId)           { this.batchId = batchId; }
    public void setAddress(String address)           { this.address = address; }
}