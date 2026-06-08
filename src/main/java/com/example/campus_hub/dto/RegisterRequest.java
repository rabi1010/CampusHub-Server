package com.example.campus_hub.dto;

import jakarta.validation.constraints.*;
import java.util.List;

public class RegisterRequest {

    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Enter a valid email")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    private String phone;

    @NotBlank(message = "Role is required")
    @Pattern(regexp = "TEACHER|STUDENT|PARENT",
            message = "Role must be TEACHER, STUDENT, or PARENT")
    private String role;

    // Optional — only sent when role = PARENT
    // Contains roll numbers of their children
    private List<String> childRollNumbers;

    public String       getFullName()          { return fullName; }
    public String       getEmail()             { return email; }
    public String       getPassword()          { return password; }
    public String       getPhone()             { return phone; }
    public String       getRole()              { return role; }
    public List<String> getChildRollNumbers()  { return childRollNumbers; }

    public void setFullName(String v)               { this.fullName = v; }
    public void setEmail(String v)                  { this.email = v; }
    public void setPassword(String v)               { this.password = v; }
    public void setPhone(String v)                  { this.phone = v; }
    public void setRole(String v)                   { this.role = v; }
    public void setChildRollNumbers(List<String> v) { this.childRollNumbers = v; }
}