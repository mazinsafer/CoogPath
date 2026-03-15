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
    // A nested DTO just for the courses inside a generated plan

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PlannedCourseDTO {
        private String courseCode;
        private String title;
        private int credits;
        private String prereqString; // For the hover tooltip in the UI
        private String reason;       // The AI's explanation for putting it here
    }
}
