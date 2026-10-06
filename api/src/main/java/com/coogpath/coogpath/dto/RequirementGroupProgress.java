package com.coogpath.coogpath.dto;

import java.util.List;

public record RequirementGroupProgress(
        String name,
        int totalCredits,
        int completedCredits,
        List<RequirementCourse> courses) {

    /** courseCode is "SUBJ NUM", or "A or B" when the requirement is a choice from a course set. */
    public record RequirementCourse(
            String courseCode,
            String title,
            int credits,
            boolean completed) {
    }
}
