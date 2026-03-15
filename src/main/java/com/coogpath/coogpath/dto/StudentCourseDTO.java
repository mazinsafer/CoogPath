package com.coogpath.coogpath.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StudentCourseDTO 
{
    private String courseCode;// Combines subject and number (e.g., "COSC 3360")
    private String title;
    private String term;
    private Integer credits;
    private String status;

}
