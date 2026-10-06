package com.coogpath.coogpath.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.coogpath.coogpath.dto.PlanResult;
import com.coogpath.coogpath.dto.PlannedTerm;
import com.coogpath.coogpath.exception.ResourceNotFoundException;
import com.coogpath.coogpath.model.Course;
import com.coogpath.coogpath.model.CourseSetCourse;
import com.coogpath.coogpath.model.RequirementGroup;
import com.coogpath.coogpath.model.RequirementItem;
import com.coogpath.coogpath.model.RoadmapSemester;
import com.coogpath.coogpath.model.RoadmapSemesterCourse;
import com.coogpath.coogpath.model.RoadmapSnapshot;
import com.coogpath.coogpath.model.Student;
import com.coogpath.coogpath.model.StudentCourse;
import com.coogpath.coogpath.model.Term;
import com.coogpath.coogpath.repository.CourseRepository;
import com.coogpath.coogpath.repository.CourseSetCourseRepository;
import com.coogpath.coogpath.repository.RequirementGroupRepository;
import com.coogpath.coogpath.repository.RequirementItemRepository;
import com.coogpath.coogpath.repository.RequisiteNodeRepository;
import com.coogpath.coogpath.repository.RequisiteRuleRepository;
import com.coogpath.coogpath.repository.RoadmapSemesterCourseRepository;
import com.coogpath.coogpath.repository.RoadmapSemesterRepository;
import com.coogpath.coogpath.repository.RoadmapSnapshotRepository;
import com.coogpath.coogpath.repository.StudentCourseRepository;
import com.coogpath.coogpath.repository.StudentRepository;

import lombok.RequiredArgsConstructor;

/**
 * Greedy, prerequisite-aware term scheduler.
 *
 * Each term: collect remaining courses whose prerequisites are satisfied, rank
 * them by how many other remaining courses they unlock, then fill the term up to
 * the credit cap for the selected mode. Repeats until everything is scheduled or
 * MAX_TERMS is reached. Anything left over is reported as unmet/blocked.
 */
@Service
@RequiredArgsConstructor
public class PlanGeneratorService
{
    static final int MAX_TERMS = 16;
    static final int FASTEST_TERM_CREDITS = 18;
    static final int BALANCED_TERM_CREDITS = 16;
    static final int SUMMER_TERM_CREDITS = 6;
    static final int BALANCED_STEM_CAP = 3;

    private final StudentCourseRepository studentCourseRepository;
    private final StudentRepository studentRepository;
    private final RequirementItemRepository requirementItemRepository;
    private final RequisiteNodeRepository requisiteNodeRepository;
    private final RequisiteRuleRepository requisiteRuleRepository;
    private final RequirementGroupRepository requirementGroupRepository;

    private final CourseRepository courseRepository;
    private final RoadmapSnapshotRepository snapshotRepository;
    private final RoadmapSemesterRepository semesterRepository;
    private final RoadmapSemesterCourseRepository semesterCourseRepository;
    private final CourseSetCourseRepository courseSetCourseRepository;

    @Transactional(readOnly = true)
    public PlanResult generatePlan(Long studentId, String mode, String startSeason, Integer startYear, Boolean includeSummer)
    {
        PlanResult result = new PlanResult();

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        PrerequisiteGraph graph = new PrerequisiteGraph(
                requisiteRuleRepository.findAll(), requisiteNodeRepository.findAll());

        Set<Long> completedCourseIds = getCompletedCourseIds(studentId);
        List<Course> remainingCourses = getRemainingCourses(student, completedCourseIds);

        Term.Season currentSeason = defaultStartSeason();
        int currentYear = defaultStartYear();
        if (startSeason != null) {
            try { currentSeason = Term.Season.valueOf(startSeason.toUpperCase()); } catch (IllegalArgumentException ignored) {}
        }
        if (startYear != null) {
            currentYear = startYear;
        }

        boolean useSummer = includeSummer != null ? includeSummer : student.isIncludeSummer();
        boolean isFastest = !"balanced".equalsIgnoreCase(mode);
        String requiredSubject = ProgramRules.coreSubject(student);

        // Phase 1: schedule into raw buckets (Course objects, not DTOs yet)
        List<String> termSeasons = new ArrayList<>();
        List<Integer> termYears = new ArrayList<>();
        List<List<Course>> termBuckets = new ArrayList<>();
        int termCount = 0;

        while (!remainingCourses.isEmpty() && termCount < MAX_TERMS)
        {
            if (currentSeason == Term.Season.SUMMER && !useSummer)
            {
                currentSeason = getNextSeason(currentSeason);
                continue;
            }

            List<Course> eligible = new ArrayList<>();
            for (Course c : remainingCourses)
            {
                if (graph.isUnlocked(c.getCourseId(), completedCourseIds))
                {
                    eligible.add(c);
                }
            }

            boolean isSummer = currentSeason == Term.Season.SUMMER;

            Map<Long, Integer> scoreCache = new HashMap<>();
            for (Course c : eligible) {
                scoreCache.put(c.getCourseId(), unlockScore(c, remainingCourses, graph));
            }

            if (isSummer) {
                eligible.sort((c1, c2) -> {
                    int p1 = summerPriority(c1);
                    int p2 = summerPriority(c2);
                    if (p1 != p2) return Integer.compare(p1, p2);
                    return Integer.compare(scoreCache.get(c2.getCourseId()), scoreCache.get(c1.getCourseId()));
                });
            } else {
                eligible.sort((c1, c2) -> Integer.compare(
                        scoreCache.get(c2.getCourseId()),
                        scoreCache.get(c1.getCourseId())));
            }

            List<Course> selectedCourses;
            if (isSummer) {
                selectedCourses = selectGreedy(eligible, SUMMER_TERM_CREDITS);
            } else if (isFastest) {
                selectedCourses = selectWithMinSubject(eligible, FASTEST_TERM_CREDITS, requiredSubject);
            } else if ("COSC".equals(requiredSubject)) {
                selectedCourses = selectWithStemLimits(eligible, BALANCED_TERM_CREDITS, BALANCED_STEM_CAP);
            } else {
                // Non-CS majors have no STEM cap in balanced mode; they still get one core-subject course.
                selectedCourses = selectWithMinSubject(eligible, BALANCED_TERM_CREDITS, requiredSubject);
            }

            if (selectedCourses.isEmpty() && !eligible.isEmpty())
            {
                selectedCourses.add(eligible.get(0));
            }

            if (!selectedCourses.isEmpty())
            {
                termSeasons.add(currentSeason.name());
                termYears.add(currentYear);
                termBuckets.add(new ArrayList<>(selectedCourses));

                for (Course c : selectedCourses) {
                    completedCourseIds.add(c.getCourseId());
                    remainingCourses.remove(c);
                }
            }

            termCount++;
            currentSeason = getNextSeason(currentSeason);
            if (currentSeason == Term.Season.SPRING) currentYear++;

            if (selectedCourses.isEmpty() && eligible.isEmpty())
            {
                break;
            }
        }

        // Phase 2: consolidate a short trailing term (<= 2 courses)
        int termCreditCap = isFastest ? FASTEST_TERM_CREDITS : BALANCED_TERM_CREDITS;
        consolidateTrailingRunts(termBuckets, termSeasons, termYears, graph, termCreditCap);

        // Phase 3: convert buckets to DTOs
        for (int i = 0; i < termBuckets.size(); i++)
        {
            PlannedTerm plannedTerm = new PlannedTerm();
            plannedTerm.setTermLabel(termSeasons.get(i) + " " + termYears.get(i));
            plannedTerm.setSeason(termSeasons.get(i));
            plannedTerm.setYear(termYears.get(i));

            for (Course c : termBuckets.get(i))
            {
                plannedTerm.getCourses().add(new PlannedTerm.PlannedCourseDTO(
                        c.getSubject() + " " + c.getNumber(),
                        c.getTitle(),
                        c.getCredits(),
                        "Prerequisites Met",
                        "Scheduled by CoogPath Algorithm"));
                plannedTerm.setTotalCredits(plannedTerm.getTotalCredits() + c.getCredits());
            }
            result.getTerms().add(plannedTerm);
        }

        for (Course c : remainingCourses)
        {
            String courseCode = c.getSubject() + " " + c.getNumber();
            result.getUnmetRequirements().add(courseCode);
            result.getBlockers().add(courseCode + " could not be scheduled due to a missing prerequisite chain.");
        }

        return result;
    }

    /**
     * Eliminates a trailing term with 1-2 courses by merging into the previous
     * fall/spring term, or by moving courses back to balance both terms.
     */
    private void consolidateTrailingRunts(List<List<Course>> buckets, List<String> seasons,
                                          List<Integer> years, PrerequisiteGraph graph, int termCreditCap)
    {
        if (buckets.size() < 2) return;

        int lastIdx = buckets.size() - 1;
        List<Course> lastTerm = buckets.get(lastIdx);

        if (lastTerm.size() > 2) return;
        if ("SUMMER".equals(seasons.get(lastIdx))) return;

        int prevIdx = lastIdx - 1;
        while (prevIdx >= 0 && "SUMMER".equals(seasons.get(prevIdx))) prevIdx--;
        if (prevIdx < 0) return;

        List<Course> prevTerm = buckets.get(prevIdx);
        int prevCredits = prevTerm.stream().mapToInt(Course::getCredits).sum();
        Set<Long> prevCourseIds = prevTerm.stream().map(Course::getCourseId).collect(Collectors.toSet());

        // Step 1: merge runt courses into prevTerm when they don't depend on it
        List<Course> merged = new ArrayList<>();
        for (Course c : lastTerm)
        {
            boolean dependsOnPrev = graph.hasPrerequisiteIn(c.getCourseId(), prevCourseIds);
            if (!dependsOnPrev && prevCredits + c.getCredits() <= termCreditCap) {
                prevTerm.add(c);
                prevCredits += c.getCredits();
                merged.add(c);
            }
        }
        lastTerm.removeAll(merged);

        if (lastTerm.isEmpty()) {
            buckets.remove(lastIdx);
            seasons.remove(lastIdx);
            years.remove(lastIdx);
            return;
        }

        // Step 2: the remaining courses depend on prevTerm, so balance by moving
        // independent low-credit courses from prevTerm forward into lastTerm.
        int targetPerTerm = (prevTerm.size() + lastTerm.size() + 1) / 2;

        Set<Long> lastTermPrereqs = new HashSet<>();
        for (Course c : lastTerm) {
            lastTermPrereqs.addAll(graph.prerequisiteIds(c.getCourseId()));
        }

        List<Course> movable = new ArrayList<>();
        for (Course c : prevTerm) {
            if (!lastTermPrereqs.contains(c.getCourseId())) {
                movable.add(c);
            }
        }
        movable.sort((a, b) -> Integer.compare(a.getCredits(), b.getCredits()));

        int lastCredits = lastTerm.stream().mapToInt(Course::getCredits).sum();
        while (lastTerm.size() < targetPerTerm && !movable.isEmpty())
        {
            Course toMove = movable.remove(0);
            if (lastCredits + toMove.getCredits() <= termCreditCap) {
                prevTerm.remove(toMove);
                lastTerm.add(toMove);
                lastCredits += toMove.getCredits();
            }
        }
    }

    private boolean isStem(Course c) {
        return "COSC".equals(c.getSubject()) || "MATH".equals(c.getSubject());
    }

    private boolean isCS(Course c) {
        return "COSC".equals(c.getSubject());
    }

    /**
     * Summer priority, lower is picked first: 0 = gen-ed/electives,
     * 1 = MATH below 3000, 2 = MATH 3000+, 3 = COSC.
     */
    private int summerPriority(Course c) {
        if ("COSC".equals(c.getSubject())) return 3;
        if ("MATH".equals(c.getSubject())) {
            try {
                int num = Integer.parseInt(c.getNumber().replaceAll("[^0-9]", ""));
                return num < 3000 ? 1 : 2;
            } catch (NumberFormatException e) { return 2; }
        }
        return 0;
    }

    private List<Course> selectGreedy(List<Course> eligible, int maxCredits)
    {
        List<Course> selected = new ArrayList<>();
        int creditsSoFar = 0;
        for (Course c : eligible) {
            if (creditsSoFar + c.getCredits() <= maxCredits) {
                selected.add(c);
                creditsSoFar += c.getCredits();
            }
        }
        return selected;
    }

    /** Greedy fill up to maxCredits, guaranteeing at least one course in requiredSubject when one is eligible. */
    private List<Course> selectWithMinSubject(List<Course> eligible, int maxCredits, String requiredSubject)
    {
        List<Course> selected = new ArrayList<>();
        int creditsSoFar = 0;

        for (Course c : eligible) {
            if (requiredSubject.equals(c.getSubject()) && c.getCredits() <= maxCredits) {
                selected.add(c);
                creditsSoFar += c.getCredits();
                break;
            }
        }

        for (Course c : eligible) {
            if (selected.contains(c)) continue;
            if (creditsSoFar + c.getCredits() <= maxCredits) {
                selected.add(c);
                creditsSoFar += c.getCredits();
            }
        }
        return selected;
    }

    /** CS balanced mode: at least one COSC course, at most stemCap COSC+MATH courses, rest non-STEM. */
    private List<Course> selectWithStemLimits(List<Course> eligible, int maxCredits, int stemCap)
    {
        List<Course> csCourses = new ArrayList<>();
        List<Course> mathCourses = new ArrayList<>();
        List<Course> otherCourses = new ArrayList<>();

        for (Course c : eligible) {
            if (isCS(c)) {
                csCourses.add(c);
            } else if ("MATH".equals(c.getSubject())) {
                mathCourses.add(c);
            } else {
                otherCourses.add(c);
            }
        }

        List<Course> selected = new ArrayList<>();
        int creditsSoFar = 0;
        int stemCount = 0;

        if (!csCourses.isEmpty()) {
            Course first = csCourses.remove(0);
            selected.add(first);
            creditsSoFar += first.getCredits();
            stemCount++;
        }

        List<Course> remainingStem = new ArrayList<>();
        remainingStem.addAll(csCourses);
        remainingStem.addAll(mathCourses);

        for (Course c : remainingStem) {
            if (stemCount >= stemCap) break;
            if (creditsSoFar + c.getCredits() <= maxCredits) {
                selected.add(c);
                creditsSoFar += c.getCredits();
                stemCount++;
            }
        }

        for (Course c : otherCourses) {
            if (creditsSoFar + c.getCredits() <= maxCredits) {
                selected.add(c);
                creditsSoFar += c.getCredits();
            }
        }

        // If no CS course made it in, swap the last non-STEM course for one.
        boolean hasCS = selected.stream().anyMatch(this::isCS);
        if (!hasCS && !csCourses.isEmpty()) {
            Course csToAdd = csCourses.get(0);
            for (int i = selected.size() - 1; i >= 0; i--) {
                Course existing = selected.get(i);
                if (!isStem(existing)) {
                    selected.remove(i);
                    creditsSoFar -= existing.getCredits();
                    if (creditsSoFar + csToAdd.getCredits() <= maxCredits) {
                        selected.add(csToAdd);
                        creditsSoFar += csToAdd.getCredits();
                    }
                    break;
                }
            }
        }

        return selected;
    }

    @Transactional
    public void savePlan(Long studentId, PlanResult plan)
    {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        RoadmapSnapshot snapshot = new RoadmapSnapshot();
        snapshot.setStudent(student);
        if (!plan.getTerms().isEmpty())
        {
            snapshot.setTargetGraduation(plan.getTerms().get(plan.getTerms().size() - 1).getTermLabel());
        }
        RoadmapSnapshot savedSnapshot = snapshotRepository.save(snapshot);

        int order = 1;
        for (PlannedTerm termDto : plan.getTerms())
        {
            RoadmapSemester semester = new RoadmapSemester();
            semester.setSnapshot(savedSnapshot);
            semester.setTermLabel(termDto.getTermLabel());
            semester.setSemesterOrder(order++);
            RoadmapSemester savedSemester = semesterRepository.save(semester);

            for (PlannedTerm.PlannedCourseDTO courseDto : termDto.getCourses())
            {
                // "COSC 3360" -> subject "COSC", number "3360"
                String[] parts = courseDto.getCourseCode().split(" ");
                if (parts.length != 2) continue;

                Optional<Course> courseOpt = courseRepository.findBySubjectAndNumber(parts[0], parts[1]);
                if (courseOpt.isPresent())
                {
                    RoadmapSemesterCourse mappedCourse = new RoadmapSemesterCourse();
                    mappedCourse.setSemester(savedSemester);
                    mappedCourse.setCourse(courseOpt.get());
                    mappedCourse.setReason(courseDto.getReason());
                    semesterCourseRepository.save(mappedCourse);
                }
            }
        }
    }

    private Set<Long> getCompletedCourseIds(Long studentId)
    {
        return studentCourseRepository.findByStudentStudentId(studentId).stream()
                .filter(sc ->
                    sc.getStatus() == StudentCourse.Status.TAKEN ||
                    sc.getStatus() == StudentCourse.Status.TRANSFER ||
                    sc.getStatus() == StudentCourse.Status.IN_PROGRESS)
                .map(sc -> sc.getCourse().getCourseId())
                .collect(Collectors.toSet());
    }

    /**
     * Required courses the student still needs, plus generated free-elective
     * slots (negative IDs, subject ELEC) to reach the program's total credits.
     */
    private List<Course> getRemainingCourses(Student student, Set<Long> completedCourseIds)
    {
        List<Course> remainingCourses = new ArrayList<>();
        Set<Long> seenCourseIds = new HashSet<>();

        List<RequirementGroup> groups = requirementGroupRepository.findByDegreeProgramProgramId(
                student.getDegreeProgram().getProgramId());

        for (RequirementGroup group : groups)
        {
            if (!ProgramRules.appliesTo(student, group)) continue;
            if (group.getName().contains("Free Elective")) continue;
            addGroupCourses(student, group, completedCourseIds, seenCourseIds, remainingCourses);
        }

        int completedCredits = courseRepository.findAllById(completedCourseIds).stream()
                .mapToInt(Course::getCredits).sum();
        int selfReportedFreeElectives = student.getFreeElectiveCredits() != null ? student.getFreeElectiveCredits() : 0;
        int remainingCredits = remainingCourses.stream().mapToInt(Course::getCredits).sum();
        int totalRequired = student.getDegreeProgram().getTotalCreditsRequired();

        int deficit = totalRequired - (completedCredits + selfReportedFreeElectives + remainingCredits);
        int slotNum = 1;
        while (deficit > 0) {
            int cr = Math.min(3, deficit);
            Course freeElective = new Course();
            freeElective.setCourseId(-1000L - slotNum);
            freeElective.setSubject("ELEC");
            freeElective.setNumber("FREE-" + cr + "HR-" + slotNum);
            freeElective.setTitle("Free Elective (" + cr + " cr)");
            freeElective.setCredits(cr);
            remainingCourses.add(freeElective);
            deficit -= cr;
            slotNum++;
        }

        return remainingCourses;
    }

    /** Adds a group's outstanding courses; for a course set, the first option stands in for the choice. */
    private void addGroupCourses(Student student, RequirementGroup group, Set<Long> completedCourseIds,
                                 Set<Long> seenCourseIds, List<Course> remainingCourses) {
        List<RequirementItem> items = requirementItemRepository.findByRequirementGroupGroupId(group.getGroupId());
        for (RequirementItem item : items) {
            if (item.getCourse() != null) {
                Course course = item.getCourse();
                if (!ProgramRules.countsSeparately(student, group, course)) continue;
                if (!completedCourseIds.contains(course.getCourseId()) && seenCourseIds.add(course.getCourseId())) {
                    remainingCourses.add(course);
                }
            } else if (item.getCourseSet() != null) {
                List<CourseSetCourse> setCourses = courseSetCourseRepository.findByCourseSetCourseSetId(
                        item.getCourseSet().getCourseSetId());
                boolean alreadyTakenOne = setCourses.stream()
                        .anyMatch(csc -> completedCourseIds.contains(csc.getCourse().getCourseId()));
                if (!alreadyTakenOne && !setCourses.isEmpty()) {
                    Course chosenCourse = setCourses.get(0).getCourse();
                    if (!ProgramRules.countsSeparately(student, group, chosenCourse)) continue;
                    if (seenCourseIds.add(chosenCourse.getCourseId())) {
                        remainingCourses.add(chosenCourse);
                    }
                }
            }
        }
    }

    /** 1 + 10 for every other remaining course that lists this one as a prerequisite. */
    private int unlockScore(Course course, List<Course> remainingCourses, PrerequisiteGraph graph)
    {
        int score = 1;
        for (Course other : remainingCourses)
        {
            if (other.getCourseId().equals(course.getCourseId())) continue;
            if (graph.prerequisiteIds(other.getCourseId()).contains(course.getCourseId())) {
                score += 10;
            }
        }
        return score;
    }

    private Term.Season getNextSeason(Term.Season current)
    {
        if (current == Term.Season.FALL) return Term.Season.SPRING;
        if (current == Term.Season.SPRING) return Term.Season.SUMMER;
        return Term.Season.FALL;
    }

    /** Jan-Aug defaults to the upcoming fall; Sep-Dec defaults to next spring. */
    private static Term.Season defaultStartSeason() {
        return LocalDate.now().getMonthValue() <= 8 ? Term.Season.FALL : Term.Season.SPRING;
    }

    private static int defaultStartYear() {
        LocalDate today = LocalDate.now();
        return today.getMonthValue() <= 8 ? today.getYear() : today.getYear() + 1;
    }
}
