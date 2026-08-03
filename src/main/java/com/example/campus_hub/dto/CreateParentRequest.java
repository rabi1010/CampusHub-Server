package com.example.campus_hub.dto;

import jakarta.validation.constraints.*;
import java.util.List;

public class CreateParentRequest {

    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Enter a valid email")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    private String phone;
    private String relationship;

    // List of student IDs to link
    @NotNull(message = "At least one child is required")
    private List<String> studentIds;

    public String       getFullName()    { return fullName; }
    public String       getEmail()       { return email; }
    public String       getPassword()    { return password; }
    public String       getPhone()       { return phone; }
    public String       getRelationship(){ return relationship; }
    public List<String> getStudentIds()  { return studentIds; }

    public void setFullName(String v)          { this.fullName = v; }
    public void setEmail(String v)             { this.email = v; }
    public void setPassword(String v)          { this.password = v; }
    public void setPhone(String v)             { this.phone = v; }
    public void setRelationship(String v)      { this.relationship = v; }
    public void setStudentIds(List<String> v)  { this.studentIds = v; }
}
