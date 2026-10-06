package com.coogpath.coogpath.dto;

import com.coogpath.coogpath.model.Student;
import com.coogpath.coogpath.service.ProgramRules;

public record StudentProfile(
        Long studentId,
        String name,
        String email,
        Long programId,
        String programName,
        String capstoneChoice,
        String financeTrack,
        boolean mathMinor,
        int freeElectiveCredits,
        boolean includeSummer) {

    public static StudentProfile from(Student student) {
        return new StudentProfile(
                student.getStudentId(),
                student.getName(),
                student.getEmail(),
                student.getDegreeProgram() != null ? student.getDegreeProgram().getProgramId() : null,
                student.getDegreeProgram() != null ? student.getDegreeProgram().getName() : null,
                student.getCapstoneChoice() != null ? student.getCapstoneChoice() : ProgramRules.DEFAULT_CAPSTONE,
                student.getFinanceTrack() != null ? student.getFinanceTrack() : ProgramRules.DEFAULT_FINANCE_TRACK,
                student.isMathMinor(),
                student.getFreeElectiveCredits() != null ? student.getFreeElectiveCredits() : 0,
                student.isIncludeSummer());
    }
}
