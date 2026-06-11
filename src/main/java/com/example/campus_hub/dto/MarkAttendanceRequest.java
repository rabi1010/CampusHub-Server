package com.example.campus_hub.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

public class MarkAttendanceRequest {

    @NotBlank(message = "Course ID is required")
    private String courseId;

    @NotNull(message = "Date is required")
    private LocalDate date;

    @NotNull(message = "Records are required")
    private List<AttendanceRecord> records;

    public String              getCourseId() { return courseId; }
    public LocalDate           getDate()     { return date; }
    public List<AttendanceRecord> getRecords(){ return records; }

    public void setCourseId(String v)                  { this.courseId = v; }
    public void setDate(LocalDate v)                   { this.date = v; }
    public void setRecords(List<AttendanceRecord> v)   { this.records = v; }

    // Inner class — one record per student
    public static class AttendanceRecord {
        private String studentId;
        private String status; // PRESENT, ABSENT, LATE

        public String getStudentId() { return studentId; }
        public String getStatus()    { return status; }

        public void setStudentId(String v) { this.studentId = v; }
        public void setStatus(String v)    { this.status = v; }
    }
}