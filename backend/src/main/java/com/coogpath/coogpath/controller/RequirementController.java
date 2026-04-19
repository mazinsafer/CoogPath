package com.coogpath.coogpath.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

@RestController
@RequestMapping("/api/requirements")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class RequirementController {

    private final StudentRepository studentRepository;
    private final RequirementGroupRepository requirementGroupRepository;
    private final RequirementItemRepository requirementItemRepository;
    private final StudentCourseRepository studentCourseRepository;
    private final CourseSetCourseRepository courseSetCourseRepository;

    @GetMapping("/{studentId}")
    public List<Map<String, Object>> getRequirements(@PathVariable Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));

        Set<Long> completedIds = studentCourseRepository.findByStudentStudentId(studentId).stream()
                .filter(sc -> sc.getStatus() == StudentCourse.Status.TAKEN
                        || sc.getStatus() == StudentCourse.Status.TRANSFER)
                .map(sc -> sc.getCourse().getCourseId())
                .collect(Collectors.toSet());

        List<RequirementGroup> groups = requirementGroupRepository.findByDegreeProgramProgramId(
                student.getDegreeProgram().getProgramId());

        String choice = student.getCapstoneChoice() != null ? student.getCapstoneChoice() : "SENIOR_SE";

        Long programId = student.getDegreeProgram() != null ? student.getDegreeProgram().getProgramId() : null;
        boolean isCSProgram = programId != null && programId == 1L;
        boolean isFinanceProgram = programId != null && programId == 2L;

        String financeTrack = student.getFinanceTrack() != null ? student.getFinanceTrack() : "STANDARD";
        String wantedFinanceTrackName = financeTrackDisplayName(financeTrack);
        boolean wantsMathMinorAddOn = student.isMathMinor();

        List<Map<String, Object>> result = new ArrayList<>();
        for (RequirementGroup group : groups) {
            String gName = group.getName();

            if (isCSProgram) {
                if (choice.equals("SENIOR_SE") && (gName.contains("Data Science") || gName.equals("Math Minor"))) continue;
                if (choice.equals("SENIOR_DS") && (gName.contains("Software Engineering") || gName.equals("Math Minor"))) continue;
                if (choice.equals("MATH_MINOR") && (gName.contains("Software Engineering") || gName.contains("Data Science"))) continue;
            }

            if (isFinanceProgram) {
                if (gName.startsWith("Finance Track:")) {
                    String groupTrackName = gName.substring("Finance Track:".length()).trim();
                    if (!groupTrackName.equalsIgnoreCase(wantedFinanceTrackName)) continue;
                }
                if (gName.startsWith("Finance Bauer Electives:")) {
                    String electiveTrackName = gName.substring("Finance Bauer Electives:".length()).trim();
                    if (!electiveTrackName.equalsIgnoreCase(wantedFinanceTrackName)) continue;
                }
                if (gName.equals("Finance Math Minor") && !wantsMathMinorAddOn) continue;
            }

            List<RequirementItem> items = requirementItemRepository.findByRequirementGroupGroupId(group.getGroupId());

            int totalCredits = 0;
            int completedCredits = 0;
            List<Map<String, Object>> courseList = new ArrayList<>();

            for (RequirementItem item : items) {
                if (item.getCourse() != null) {
                    int cr = item.getCourse().getCredits();
                    totalCredits += cr;
                    boolean done = completedIds.contains(item.getCourse().getCourseId());
                    if (done) completedCredits += cr;

                    Map<String, Object> c = new HashMap<>();
                    c.put("courseCode", item.getCourse().getSubject() + " " + item.getCourse().getNumber());
                    c.put("title", item.getCourse().getTitle());
                    c.put("credits", cr);
                    c.put("completed", done);
                    courseList.add(c);
                } else if (item.getCourseSet() != null) {
                    List<CourseSetCourse> setCourses = courseSetCourseRepository.findByCourseSetCourseSetId(
                            item.getCourseSet().getCourseSetId());
                    if (!setCourses.isEmpty()) {
                        int cr = setCourses.get(0).getCourse().getCredits();
                        totalCredits += cr;
                        boolean done = setCourses.stream()
                                .anyMatch(csc -> completedIds.contains(csc.getCourse().getCourseId()));
                        if (done) completedCredits += cr;

                        String codes = setCourses.stream()
                                .map(csc -> csc.getCourse().getSubject() + " " + csc.getCourse().getNumber())
                                .collect(Collectors.joining(" or "));
                        Map<String, Object> c = new HashMap<>();
                        c.put("courseCode", codes);
                        c.put("title", "Choose one");
                        c.put("credits", cr);
                        c.put("completed", done);
                        courseList.add(c);
                    }
                }
            }

            Map<String, Object> groupMap = new HashMap<>();
            groupMap.put("name", group.getName());
            groupMap.put("totalCredits", totalCredits);
            groupMap.put("completedCredits", completedCredits);
            groupMap.put("courses", courseList);
            result.add(groupMap);
        }
        return result;
    }

    private String financeTrackDisplayName(String track) {
        if (track == null) return "Standard";
        switch (track) {
            case "RE":   return "Real Estate";
            case "PFP":  return "Personal Financial Planning";
            case "CBC":  return "Corporate Banking and Credit";
            case "GEM":  return "Global Energy Management";
            case "ECTC": return "Energy Commodities Trading and Consulting";
            case "STANDARD":
            default:
                return "Standard";
        }
    }
}
