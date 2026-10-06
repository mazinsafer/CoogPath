package com.coogpath.coogpath.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.coogpath.coogpath.model.Course;
import com.coogpath.coogpath.model.DegreeProgram;
import com.coogpath.coogpath.model.RequirementGroup;
import com.coogpath.coogpath.model.Student;

class ProgramRulesTest {

    private static Student studentIn(long programId) {
        DegreeProgram program = new DegreeProgram();
        program.setProgramId(programId);
        Student student = new Student();
        student.setDegreeProgram(program);
        return student;
    }

    private static RequirementGroup group(String name) {
        RequirementGroup group = new RequirementGroup();
        group.setName(name);
        return group;
    }

    private static Course course(String subject) {
        Course course = new Course();
        course.setSubject(subject);
        return course;
    }

    @Test
    void cs_software_engineering_choice_excludes_data_science_and_math_minor() {
        Student student = studentIn(ProgramRules.COMPUTER_SCIENCE_PROGRAM_ID);
        student.setCapstoneChoice("SENIOR_SE");

        assertTrue(ProgramRules.appliesTo(student, group("CS Capstone (Software Engineering)")));
        assertFalse(ProgramRules.appliesTo(student, group("CS Capstone (Data Science)")));
        assertFalse(ProgramRules.appliesTo(student, group("Math Minor")));
        assertTrue(ProgramRules.appliesTo(student, group("CS Core")));
    }

    @Test
    void cs_math_minor_choice_excludes_both_capstones() {
        Student student = studentIn(ProgramRules.COMPUTER_SCIENCE_PROGRAM_ID);
        student.setCapstoneChoice("MATH_MINOR");

        assertTrue(ProgramRules.appliesTo(student, group("Math Minor")));
        assertFalse(ProgramRules.appliesTo(student, group("CS Capstone (Software Engineering)")));
        assertFalse(ProgramRules.appliesTo(student, group("CS Capstone (Data Science)")));
    }

    @Test
    void finance_student_only_gets_groups_for_their_track() {
        Student student = studentIn(ProgramRules.FINANCE_PROGRAM_ID);
        student.setFinanceTrack("RE");

        assertTrue(ProgramRules.appliesTo(student, group("Finance Track: Real Estate")));
        assertTrue(ProgramRules.appliesTo(student, group("Finance Bauer Electives: Real Estate")));
        assertFalse(ProgramRules.appliesTo(student, group("Finance Track: Standard")));
        assertFalse(ProgramRules.appliesTo(student, group("Finance Bauer Electives: Standard")));
        assertTrue(ProgramRules.appliesTo(student, group("Finance Core")));
    }

    @Test
    void finance_math_minor_is_opt_in() {
        Student student = studentIn(ProgramRules.FINANCE_PROGRAM_ID);
        assertFalse(ProgramRules.appliesTo(student, group("Finance Math Minor")));

        student.setMathMinor(true);
        assertTrue(ProgramRules.appliesTo(student, group("Finance Math Minor")));
    }

    @Test
    void finance_math_minor_covers_elec_placeholders_in_bauer_electives_only() {
        Student student = studentIn(ProgramRules.FINANCE_PROGRAM_ID);
        RequirementGroup electives = group("Finance Bauer Electives: Standard");
        Course elec = course("ELEC");
        Course busi = course("BUSI");

        assertTrue(ProgramRules.countsSeparately(student, electives, elec));

        student.setMathMinor(true);
        assertFalse(ProgramRules.countsSeparately(student, electives, elec));
        assertTrue(ProgramRules.countsSeparately(student, electives, busi));
        assertTrue(ProgramRules.countsSeparately(student, group("Finance Core"), elec));
    }

    @Test
    void core_subject_follows_the_program() {
        assertEquals("COSC", ProgramRules.coreSubject(studentIn(ProgramRules.COMPUTER_SCIENCE_PROGRAM_ID)));
        assertEquals("FINA", ProgramRules.coreSubject(studentIn(ProgramRules.FINANCE_PROGRAM_ID)));
    }

    @Test
    void unknown_finance_track_falls_back_to_standard() {
        assertEquals("Standard", ProgramRules.financeTrackDisplayName("NOPE"));
        assertFalse(ProgramRules.isValidFinanceTrack("NOPE"));
        assertTrue(ProgramRules.isValidFinanceTrack("GEM"));
    }
}
