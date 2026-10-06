package com.coogpath.coogpath.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.coogpath.coogpath.dto.RequirementGroupProgress;
import com.coogpath.coogpath.dto.RequirementGroupProgress.RequirementCourse;
import com.coogpath.coogpath.exception.ResourceNotFoundException;
import com.coogpath.coogpath.model.Course;
import com.coogpath.coogpath.model.CourseSetCourse;
import com.coogpath.coogpath.model.RequirementGroup;
import com.coogpath.coogpath.model.RequirementItem;
import com.coogpath.coogpath.model.Student;
import com.coogpath.coogpath.model.StudentCourse;
import com.coogpath.coogpath.repository.CourseSetCourseRepository;
import com.coogpath.coogpath.repository.RequirementGroupRepository;
import com.coogpath.coogpath.repository.RequirementItemRepository;
import com.coogpath.coogpath.repository.StudentCourseRepository;
import com.coogpath.coogpath.repository.StudentRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RequirementService {

    private final StudentRepository studentRepository;
    private final RequirementGroupRepository requirementGroupRepository;
    private final RequirementItemRepository requirementItemRepository;
    private final StudentCourseRepository studentCourseRepository;
    private final CourseSetCourseRepository courseSetCourseRepository;

    /** Progress toward every requirement group that applies to the student's program and options. */
    public List<RequirementGroupProgress> getProgress(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        Set<Long> completedIds = studentCourseRepository.findByStudentStudentId(studentId).stream()
                .filter(sc -> sc.getStatus() == StudentCourse.Status.TAKEN
                        || sc.getStatus() == StudentCourse.Status.TRANSFER)
                .map(sc -> sc.getCourse().getCourseId())
                .collect(Collectors.toSet());

        List<RequirementGroup> groups = requirementGroupRepository.findByDegreeProgramProgramId(
                student.getDegreeProgram().getProgramId());

        List<RequirementGroupProgress> result = new ArrayList<>();
        for (RequirementGroup group : groups) {
            if (!ProgramRules.appliesTo(student, group)) continue;
            result.add(progressFor(student, group, completedIds));
        }
        return result;
    }

    private RequirementGroupProgress progressFor(Student student, RequirementGroup group, Set<Long> completedIds) {
        int totalCredits = 0;
        int completedCredits = 0;
        List<RequirementCourse> courses = new ArrayList<>();

        for (RequirementItem item : requirementItemRepository.findByRequirementGroupGroupId(group.getGroupId())) {
            if (item.getCourse() != null) {
                Course course = item.getCourse();
                if (!ProgramRules.countsSeparately(student, group, course)) continue;
                boolean done = completedIds.contains(course.getCourseId());
                totalCredits += course.getCredits();
                if (done) completedCredits += course.getCredits();
                courses.add(new RequirementCourse(code(course), course.getTitle(), course.getCredits(), done));
            } else if (item.getCourseSet() != null) {
                List<CourseSetCourse> options = courseSetCourseRepository.findByCourseSetCourseSetId(
                        item.getCourseSet().getCourseSetId());
                if (options.isEmpty()) continue;
                if (!ProgramRules.countsSeparately(student, group, options.get(0).getCourse())) continue;

                int credits = options.get(0).getCourse().getCredits();
                boolean done = options.stream()
                        .anyMatch(csc -> completedIds.contains(csc.getCourse().getCourseId()));
                totalCredits += credits;
                if (done) completedCredits += credits;

                String codes = options.stream()
                        .map(csc -> code(csc.getCourse()))
                        .collect(Collectors.joining(" or "));
                courses.add(new RequirementCourse(codes, "Choose one", credits, done));
            }
        }

        return new RequirementGroupProgress(group.getName(), totalCredits, completedCredits, courses);
    }

    private static String code(Course course) {
        return course.getSubject() + " " + course.getNumber();
    }
}
