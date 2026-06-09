package com.example.campus_hub.dto;

import jakarta.validation.constraints.NotBlank;

public class CreateNoticeRequest {

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Content is required")
    private String content;

    // ALL, STUDENT, TEACHER, PARENT
    private String forRole = "ALL";

    private boolean urgent = false;

    public String  getTitle()   { return title; }
    public String  getContent() { return content; }
    public String  getForRole() { return forRole; }
    public boolean isUrgent()   { return urgent; }

    public void setTitle(String v)    { this.title = v; }
    public void setContent(String v)  { this.content = v; }
    public void setForRole(String v)  { this.forRole = v; }
    public void setUrgent(boolean v)  { this.urgent = v; }
}