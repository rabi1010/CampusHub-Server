package com.example.campus_hub.dto;

public class ChangePasswordRequest {
    private String currentPassword;
    private String newPassword;

    public String getCurrentPassword() { return currentPassword; }
    public String getNewPassword() { return newPassword; }
    public void setCurrentPassword(String value) { this.currentPassword = value; }
    public void setNewPassword(String value) { this.newPassword = value; }
}
