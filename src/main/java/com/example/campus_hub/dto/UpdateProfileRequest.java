package com.example.campus_hub.dto;

public class UpdateProfileRequest {
    private String fullName;
    private String email;
    private String phone;

    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public void setFullName(String value) { this.fullName = value; }
    public void setEmail(String value) { this.email = value; }
    public void setPhone(String value) { this.phone = value; }
}
