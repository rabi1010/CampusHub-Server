package com.example.campus_hub.dto;

import java.util.List;

public class UpdateParentRequest {

    private String       fullName;
    private String       phone;
    private String       relationship;
    private List<String> studentIds; // replace all children

    public String       getFullName()    { return fullName; }
    public String       getPhone()       { return phone; }
    public String       getRelationship(){ return relationship; }
    public List<String> getStudentIds()  { return studentIds; }

    public void setFullName(String v)         { this.fullName = v; }
    public void setPhone(String v)            { this.phone = v; }
    public void setRelationship(String v)     { this.relationship = v; }
    public void setStudentIds(List<String> v) { this.studentIds = v; }
}
