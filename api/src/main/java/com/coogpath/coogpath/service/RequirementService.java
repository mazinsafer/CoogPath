package com.coogpath.coogpath.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.coogpath.coogpath.dto.RequirementGroupProgress;
import com.coogpath.coogpath.dto.RequirementGroupProgress.RequirementCourse;
import com.coogpath.coogpath.exception.ResourceNotFoundException;
import com.coogpath.coogpath.model.Course;
import com.coogpath.coogpath.model.RequirementGroup;
import com.coogpath.coogpath.model.Student;
import com.coogpath.coogpath.model.StudentCourse;
import com.coogpath.coogpath.repository.StudentCourseRepository;
import com.coogpath.coogpath.repository.StudentRepository;
import com.coogpath.coogpath.service.CatalogCache.GroupRequirements;
import com.coogpath.coogpath.service.CatalogCache.ItemRequirement;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RequirementService {

    private final StudentRepository studentRepository;
    private final StudentCourseRepository studentCourseRepository;
    private final CatalogCache catalogCache;

    /** Progress toward every requirement group that applies to the student's program and options. */
    @Transactional(readOnly = true)
    public List<RequirementGroupProgress> getProgress(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        Set<Long> completedIds = studentCourseRepository.findByStudentStudentId(studentId).stream()
                .filter(sc -> sc.getStatus() == StudentCourse.Status.TAKEN
                        || sc.getStatus() == StudentCourse.Status.TRANSFER)
                .map(sc -> sc.getCourse().getCourseId())
                .collect(Collectors.toSet());

        List<RequirementGroupProgress> result = new ArrayList<>();
        for (GroupRequirements requirements : catalogCache.requirementsFor(student.getDegreeProgram().getProgramId())) {
            if (!ProgramRules.appliesTo(student, requirements.group())) continue;
            result.add(progressFor(student, requirements, completedIds));
        }
        return result;
    }

    private RequirementGroupProgress progressFor(Student student, GroupRequirements requirements, Set<Long> completedIds) {
        RequirementGroup group = requirements.group();
        int totalCredits = 0;
        int completedCredits = 0;
        List<RequirementCourse> courses = new ArrayList<>();

        for (ItemRequirement item : requirements.items()) {
            if (!item.isChoice()) {
                Course course = item.course();
                if (!ProgramRules.countsSeparately(student, group, course)) continue;
                boolean done = completedIds.contains(course.getCourseId());
                totalCredits += course.getCredits();
                if (done) completedCredits += course.getCredits();
                courses.add(new RequirementCourse(code(course), course.getTitle(), course.getCredits(), done));
            } else {
                List<Course> options = item.options();
                if (options.isEmpty()) continue;
                if (!ProgramRules.countsSeparately(student, group, options.get(0))) continue;

                int credits = options.get(0).getCredits();
                boolean done = options.stream().anyMatch(option -> completedIds.contains(option.getCourseId()));
                totalCredits += credits;
                if (done) completedCredits += credits;

                String codes = options.stream()
                        .map(RequirementService::code)
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
