package com.coogpath.coogpath.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.coogpath.coogpath.dto.PlanResult;
import com.coogpath.coogpath.model.Course;
import com.coogpath.coogpath.model.Student;
import com.coogpath.coogpath.model.Term;
import com.coogpath.coogpath.repository.DegreeProgramRepository;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;

@SpringBootTest
class PlanGeneratorServiceDistributionTest {
    @Autowired private PlanGeneratorService plans;
    @Autowired private DegreeProgramRepository programs;
    @Autowired private CatalogCache catalog;

    @Test
    void majorCoursesReachTheFinalTermWithoutBreakingPrerequisites() {
        for (long programId : List.of(1L, 2L)) {
            List<String> options = programId == 1L
                    ? List.of("SENIOR_SE", "SENIOR_DS", "MATH_MINOR")
                    : List.of("STANDARD", "RE", "ECTC");
            for (String option : options) {
                for (boolean fastest : List.of(false, true)) {
                    Student student = new Student();
                    student.setDegreeProgram(programs.findById(programId).orElseThrow());
                    student.setCapstoneChoice(option);
                    student.setFinanceTrack(option);
                    PlanResult plan = ReflectionTestUtils.invokeMethod(plans, "buildPlan", student, Map.of(), fastest,
                            Term.Season.SPRING, 2027, false);
                    String major = programId == 1L ? "COSC " : "FINA ";
                    assertTrue(plan.getTerms().getLast().getCourses().stream()
                            .anyMatch(course -> course.getCourseCode().startsWith(major)), option + " final term");
                    verifyCourses(student, plan, fastest ? 18 : 16);
                }
            }
        }
    }

    private void verifyCourses(Student student, PlanResult plan, int cap) {
        List<Course> required = ReflectionTestUtils.invokeMethod(plans, "getRemainingCourses", student, Map.of());
        Map<String, Course> byCode = new HashMap<>();
        for (Course course : required) byCode.put(course.getSubject() + " " + course.getNumber(), course);
        PrerequisiteGraph graph = catalog.prerequisiteGraph();
        Set<Long> completed = new HashSet<>();
        for (var term : plan.getTerms()) {
            assertTrue(term.getTotalCredits() <= cap, term.getTermLabel() + " exceeds credit cap");
            int sum = 0;
            Set<Long> thisTerm = new HashSet<>();
            for (var item : term.getCourses()) {
                Course course = byCode.remove(item.getCourseCode());
                assertNotNull(course, item.getCourseCode() + " appears twice or is not required");
                assertTrue(graph.isUnlocked(course.getCourseId(), completed), item.getCourseCode() + " is before its prerequisites");
                sum += course.getCredits();
                thisTerm.add(course.getCourseId());
            }
            assertEquals(sum, term.getTotalCredits());
            completed.addAll(thisTerm);
        }
        assertTrue(byCode.isEmpty(), "Required courses were omitted");
        assertTrue(plan.getBlockers().isEmpty(), "Plan contains blocked courses");
    }
}
