package com.coogpath.coogpath.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlannedTerm 
{
    private String termLabel;
    private String season;
    private int year;
    private int totalCredits = 0;

    private List<PlannedCourseDTO> courses = new ArrayList<>();

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PlannedCourseDTO {
        private String courseCode;
        private String title;
        private int credits;
        private String prereqString;
        private String reason;       // Why the planner placed the course in this term
    }
}
