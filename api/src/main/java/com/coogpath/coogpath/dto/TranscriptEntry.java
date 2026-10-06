package com.coogpath.coogpath.dto;

import com.coogpath.coogpath.model.StudentCourse;

public record TranscriptEntry(
        Long courseId,
        String courseCode,
        String title,
        int credits,
        String status,
        String grade) {

    public static TranscriptEntry from(StudentCourse record) {
        return new TranscriptEntry(
                record.getCourse().getCourseId(),
                record.getCourse().getSubject() + " " + record.getCourse().getNumber(),
                record.getCourse().getTitle(),
                record.getCourse().getCredits(),
                record.getStatus().name(),
                record.getGrade());
    }
}
