package com.example.campus_hub.dto;

import jakarta.validation.constraints.*;
import java.util.List;

public class UploadMarksRequest {

    @NotBlank(message = "Course ID is required")
    private String courseId;

    @NotBlank(message = "Exam type is required")
    private String examType; // INTERNAL, MIDTERM, FINAL

    @Min(value = 1, message = "Semester must be at least 1")
    @Max(value = 8, message = "Semester cannot exceed 8")
    private int semester;

    @NotNull(message = "Marks records are required")
    private List<MarkRecord> records;

    public String           getCourseId() { return courseId; }
    public String           getExamType() { return examType; }
    public int              getSemester() { return semester; }
    public List<MarkRecord> getRecords()  { return records; }

    public void setCourseId(String v)           { this.courseId = v; }
    public void setExamType(String v)           { this.examType = v; }
    public void setSemester(int v)              { this.semester = v; }
    public void setRecords(List<MarkRecord> v)  { this.records = v; }

    public static class MarkRecord {
        private String studentId;
        private float  marksObtained;
        private float  totalMarks;

        public String getStudentId()    { return studentId; }
        public float  getMarksObtained(){ return marksObtained; }
        public float  getTotalMarks()   { return totalMarks; }

        public void setStudentId(String v)    { this.studentId = v; }
        public void setMarksObtained(float v) { this.marksObtained = v; }
        public void setTotalMarks(float v)    { this.totalMarks = v; }
    }
}