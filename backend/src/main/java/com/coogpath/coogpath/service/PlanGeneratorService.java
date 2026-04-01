package com.coogpath.coogpath.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.coogpath.coogpath.dto.PlanResult;
import com.coogpath.coogpath.dto.PlannedTerm;
import com.coogpath.coogpath.model.Course;
import com.coogpath.coogpath.model.CourseSetCourse;
import com.coogpath.coogpath.model.RequirementGroup;
import com.coogpath.coogpath.model.RequirementItem;
import com.coogpath.coogpath.model.RequisiteNode;
import com.coogpath.coogpath.model.RequisiteRule;
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

@Service
@RequiredArgsConstructor // auto-generates the constructor for all our 'private final' repositories
public class PlanGeneratorService 
{
    //import repositories with data needed for service

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

    public PlanResult generatePlan(Long studentId, String mode, String startSeason, Integer startYear, Boolean includeSummer) 
    {
        PlanResult result = new PlanResult();

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));

        Set<Long> completedCourseIds = getCompletedCourseIds(studentId);
        List<Course> remainingCourses = getRemainingCourses(student, completedCourseIds);

        int termCount = 0;
        Term.Season currentSeason = Term.Season.FALL;
        int currentYear = 2026;

        if (startSeason != null) {
            try { currentSeason = Term.Season.valueOf(startSeason); } catch (IllegalArgumentException ignored) {}
        }
        if (startYear != null) {
            currentYear = startYear;
        }

        boolean useSummer = includeSummer != null ? includeSummer : student.isIncludeSummer();
        String planMode = mode != null ? mode : "fastest";

        // Phase 1: Schedule into raw buckets (Course objects, not DTOs yet)
        List<String> termLabels = new ArrayList<>();
        List<String> termSeasons = new ArrayList<>();
        List<Integer> termYears = new ArrayList<>();
        List<List<Course>> termBuckets = new ArrayList<>();
        boolean isFastest = "fastest".equals(planMode);

        while (!remainingCourses.isEmpty() && termCount < 16) 
        {
            if (currentSeason == Term.Season.SUMMER && !useSummer) 
            {
                currentSeason = getNextSeason(currentSeason);
                continue;
            }

            List<Course> eligible = new ArrayList<>();
            for (Course c : remainingCourses) 
            {
                if (isCourseUnlocked(c.getCourseId(), completedCourseIds)) 
                {
                    eligible.add(c);
                }
            }

            boolean isSummer = (currentSeason == Term.Season.SUMMER);

            Map<Long, Integer> scoreCache = new HashMap<>();
            for (Course c : eligible) {
                scoreCache.put(c.getCourseId(), calculateScore(c, remainingCourses));
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
                        scoreCache.get(c1.getCourseId())
                ));
            }

            // Fastest: 18 credits every fall/spring, no STEM cap — pure greedy + min 1 CS
            // Balanced: 16 credits fall/spring, max 3 STEM, min 1 CS
            int maxCredits;
            if (isSummer) {
                maxCredits = 6;
            } else if (isFastest) {
                maxCredits = 18;
            } else {
                maxCredits = 16;
            }

            List<Course> selectedCourses;
            if (isSummer) {
                selectedCourses = selectGreedy(eligible, maxCredits);
            } else if (isFastest) {
                selectedCourses = selectFastestWithMinCS(eligible, maxCredits);
            } else {
                selectedCourses = selectWithStemLimits(eligible, maxCredits, 3);
            }

            if (selectedCourses.isEmpty() && !eligible.isEmpty()) 
            {
                selectedCourses.add(eligible.get(0));
            }

            if (!selectedCourses.isEmpty()) 
            {
                termLabels.add(currentSeason + " " + currentYear);
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

        // Phase 2: Consolidate runt trailing semesters (≤ 2 courses)
        consolidateTrailingRunts(termBuckets, termLabels, termSeasons, termYears);

        // Phase 3: Convert buckets to PlannedTerm DTOs
        for (int i = 0; i < termBuckets.size(); i++) 
        {
            PlannedTerm plannedTerm = new PlannedTerm();
            plannedTerm.setTermLabel(termLabels.get(i));
            plannedTerm.setSeason(termSeasons.get(i));
            plannedTerm.setYear(termYears.get(i));

            for (Course c : termBuckets.get(i)) 
            {
                plannedTerm.getCourses().add(new PlannedTerm.PlannedCourseDTO(
                        c.getSubject() + " " + c.getNumber(),
                        c.getTitle(),
                        c.getCredits(),
                        "Prerequisites Met",
                        "Scheduled by CoogPath Algorithm"
                ));
                plannedTerm.setTotalCredits(plannedTerm.getTotalCredits() + c.getCredits());
            }
            result.getTerms().add(plannedTerm);
        }

        if (!remainingCourses.isEmpty()) 
        {
            for (Course c : remainingCourses) 
            {
                String courseCode = c.getSubject() + " " + c.getNumber();
                result.getUnmetRequirements().add(courseCode);
                result.getBlockers().add(courseCode + " could not be scheduled due to a missing prerequisite chain.");
            }
        }

        return result;
    }

    /**
     * Eliminates trailing semesters with 1-2 courses by merging into the previous
     * semester or redistributing courses to balance both semesters.
     */
    private void consolidateTrailingRunts(List<List<Course>> buckets, List<String> labels,
                                           List<String> seasons, List<Integer> years) 
    {
        if (buckets.size() < 2) return;

        int lastIdx = buckets.size() - 1;
        List<Course> lastTerm = buckets.get(lastIdx);

        if (lastTerm.size() > 2) return;
        if ("SUMMER".equals(seasons.get(lastIdx))) return;

        // Find the previous non-summer term
        int prevIdx = lastIdx - 1;
        while (prevIdx >= 0 && "SUMMER".equals(seasons.get(prevIdx))) prevIdx--;
        if (prevIdx < 0) return;

        List<Course> prevTerm = buckets.get(prevIdx);
        int prevCredits = prevTerm.stream().mapToInt(Course::getCredits).sum();
        Set<Long> prevCourseIds = prevTerm.stream().map(Course::getCourseId).collect(Collectors.toSet());

        // Step 1: Try to merge runt courses into prevTerm (allow up to 19 credits)
        List<Course> merged = new ArrayList<>();
        for (Course c : lastTerm) 
        {
            boolean dependsOnPrev = courseHasPrereqInSet(c.getCourseId(), prevCourseIds);

            if (!dependsOnPrev && prevCredits + c.getCredits() <= 19) {
                prevTerm.add(c);
                prevCredits += c.getCredits();
                merged.add(c);
            }
        }
        lastTerm.removeAll(merged);

        if (lastTerm.isEmpty()) {
            buckets.remove(lastIdx);
            labels.remove(lastIdx);
            seasons.remove(lastIdx);
            years.remove(lastIdx);
            return;
        }

        // Step 2: Courses that depend on prevTerm can't be merged.
        // Redistribute: move low-value courses from prevTerm to lastTerm to balance both.
        int totalCourses = prevTerm.size() + lastTerm.size();
        int targetPerTerm = (totalCourses + 1) / 2;

        // Find courses in prevTerm that NO lastTerm course depends on
        Set<Long> lastTermPrereqs = new HashSet<>();
        for (Course c : lastTerm) {
            lastTermPrereqs.addAll(getDirectPrereqIds(c.getCourseId()));
        }

        // Move lowest-credit courses from prevTerm to lastTerm until balanced
        List<Course> movable = new ArrayList<>();
        for (Course c : prevTerm) {
            if (!lastTermPrereqs.contains(c.getCourseId())) {
                movable.add(c);
            }
        }
        // Sort by credits ascending so we move the smallest courses first
        movable.sort((a, b) -> Integer.compare(a.getCredits(), b.getCredits()));

        int lastCredits = lastTerm.stream().mapToInt(Course::getCredits).sum();
        while (lastTerm.size() < targetPerTerm && !movable.isEmpty()) 
        {
            Course toMove = movable.remove(0);
            if (lastCredits + toMove.getCredits() <= 19) {
                prevTerm.remove(toMove);
                lastTerm.add(toMove);
                lastCredits += toMove.getCredits();
            }
        }
    }

    private boolean courseHasPrereqInSet(Long courseId, Set<Long> checkSet) 
    {
        Optional<RequisiteRule> ruleOpt = requisiteRuleRepository.findByCourseCourseId(courseId);
        if (ruleOpt.isEmpty()) return false;
        List<RequisiteNode> nodes = requisiteNodeRepository.findByRuleRuleId(ruleOpt.get().getRuleId());
        return nodes.stream().anyMatch(n -> 
                n.getOperator() == RequisiteNode.Operator.COURSE
                && n.getCourse() != null
                && checkSet.contains(n.getCourse().getCourseId()));
    }

    private Set<Long> getDirectPrereqIds(Long courseId) 
    {
        Set<Long> prereqs = new HashSet<>();
        Optional<RequisiteRule> ruleOpt = requisiteRuleRepository.findByCourseCourseId(courseId);
        if (ruleOpt.isEmpty()) return prereqs;
        List<RequisiteNode> nodes = requisiteNodeRepository.findByRuleRuleId(ruleOpt.get().getRuleId());
        for (RequisiteNode n : nodes) {
            if (n.getOperator() == RequisiteNode.Operator.COURSE && n.getCourse() != null) {
                prereqs.add(n.getCourse().getCourseId());
            }
        }
        return prereqs;
    }

    private boolean isStem(Course c) {
        String subj = c.getSubject();
        return "COSC".equals(subj) || "MATH".equals(subj);
    }

    private boolean isCS(Course c) {
        return "COSC".equals(c.getSubject());
    }

    /**
     * Summer priority: lower number = picked first.
     * 0 = non-CS/non-MATH (gen-ed, electives)
     * 1 = MATH below 3000
     * 2 = MATH 3000+
     * 3 = COSC (advanced CS last)
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

    /**
     * Fastest mode: pure greedy — pack as many credits as possible, guarantee min 1 CS.
     * No STEM cap. Sorts by priority score already, just greedily fills up to maxCredits.
     */
    private List<Course> selectFastestWithMinCS(List<Course> eligible, int maxCredits)
    {
        List<Course> selected = new ArrayList<>();
        int creditsSoFar = 0;

        // First, guarantee at least 1 CS course
        Course firstCS = null;
        for (Course c : eligible) {
            if (isCS(c) && creditsSoFar + c.getCredits() <= maxCredits) {
                firstCS = c;
                break;
            }
        }
        if (firstCS != null) {
            selected.add(firstCS);
            creditsSoFar += firstCS.getCredits();
        }

        // Greedily fill remaining credits with anything that fits
        for (Course c : eligible) {
            if (selected.contains(c)) continue;
            if (creditsSoFar + c.getCredits() <= maxCredits) {
                selected.add(c);
                creditsSoFar += c.getCredits();
            }
        }
        return selected;
    }

    /**
     * Balanced mode: Fall/Spring course selection with STEM limits.
     * Enforces: min 1 COSC, max stemCap CS+MATH, fill rest with non-STEM.
     */
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

        // Guarantee at least 1 CS course if available
        if (!csCourses.isEmpty()) {
            Course first = csCourses.remove(0);
            selected.add(first);
            creditsSoFar += first.getCredits();
            stemCount++;
        }

        // Fill more STEM up to stemCap (CS first, then MATH by priority)
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

        // Fill remaining credits with non-STEM courses
        for (Course c : otherCourses) {
            if (creditsSoFar + c.getCredits() <= maxCredits) {
                selected.add(c);
                creditsSoFar += c.getCredits();
            }
        }

        // If still no CS course selected (none were eligible), and there's a CS course 
        // that could replace a non-STEM course, do the swap
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

    // SAVE GENERATED PLAN TO DATABASE
    public void savePlan(Long studentId, PlanResult plan) 
    {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));

        // 1. Create the Snapshot (The "Header")
        RoadmapSnapshot snapshot = new RoadmapSnapshot();
        snapshot.setStudent(student);
        // Grab the term label of the very last semester in the plan (e.g., "FALL 2027")
        if (!plan.getTerms().isEmpty()) 
        {
            snapshot.setTargetGraduation(plan.getTerms().get(plan.getTerms().size() - 1).getTermLabel());
        }
        RoadmapSnapshot savedSnapshot = snapshotRepository.save(snapshot);

        // 2. Loop through the JSON terms and create RoadmapSemesters
        int order = 1;
        for (PlannedTerm termDto : plan.getTerms()) 
        {
            RoadmapSemester semester = new RoadmapSemester();
            semester.setSnapshot(savedSnapshot);
            semester.setTermLabel(termDto.getTermLabel());
            semester.setSemesterOrder(order++);
            RoadmapSemester savedSemester = semesterRepository.save(semester);

            // 3. Loop through the classes in this semester and create RoadmapSemesterCourses
            for (PlannedTerm.PlannedCourseDTO courseDto : termDto.getCourses()) 
            {
                // Split "COSC 3360" into "COSC" and "3360"
                String[] parts = courseDto.getCourseCode().split(" ");
                if (parts.length == 2) 
                {
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
    }

    private Set<Long> getCompletedCourseIds(Long studentId) 
    {
        // 1. Fetch the student's entire transcript
        List<StudentCourse> transcript = studentCourseRepository.findByStudentStudentId(studentId);

        // 2. Filter for only the passing/in-progress courses and extract their IDs
        return transcript.stream()
                .filter(sc -> 
                    sc.getStatus() == StudentCourse.Status.TAKEN || 
                    sc.getStatus() == StudentCourse.Status.TRANSFER || 
                    sc.getStatus() == StudentCourse.Status.IN_PROGRESS
                )
                .map(sc -> sc.getCourse().getCourseId()) // Pluck out just the course ID
                .collect(Collectors.toSet());
    }


    // BUILD REMAINING REQUIREMENTS

    /**
     * Looks at the student's degree plan and figures out which required courses they haven't taken yet.
     */
    private List<Course> getRemainingCourses(Student student, Set<Long> completedCourseIds) 
    {
        List<Course> remainingCourses = new ArrayList<>();
        Set<Long> seenCourseIds = new HashSet<>();

        List<RequirementGroup> groups = requirementGroupRepository.findByDegreeProgramProgramId(
                student.getDegreeProgram().getProgramId()
        );

        String choice = student.getCapstoneChoice() != null ? student.getCapstoneChoice() : "SENIOR_SE";

        RequirementGroup freeElectivesGroup = null;

        for (RequirementGroup group : groups) 
        {
            String gName = group.getName();
            if (choice.equals("SENIOR_SE") && (gName.contains("Data Science") || gName.contains("Math Minor"))) continue;
            if (choice.equals("SENIOR_DS") && (gName.contains("Software Engineering") || gName.contains("Math Minor"))) continue;
            if (choice.equals("MATH_MINOR") && (gName.contains("Software Engineering") || gName.contains("Data Science"))) continue;

            if (gName.contains("Free Elective")) {
                freeElectivesGroup = group;
                continue;
            }

            addGroupCourses(group, completedCourseIds, seenCourseIds, remainingCourses);
        }

        // Only add enough free electives to reach totalRequired (120)
        if (freeElectivesGroup != null) {
            int completedCredits = courseRepository.findAllById(completedCourseIds).stream()
                    .mapToInt(Course::getCredits).sum();
            int selfReportedFreeElectives = student.getFreeElectiveCredits() != null ? student.getFreeElectiveCredits() : 0;
            int remainingCredits = remainingCourses.stream().mapToInt(Course::getCredits).sum();
            int totalRequired = student.getDegreeProgram().getTotalCreditsRequired();

            int deficit = totalRequired - (completedCredits + selfReportedFreeElectives + remainingCredits);
            if (deficit > 0) {
                List<RequirementItem> freeItems = requirementItemRepository
                        .findByRequirementGroupGroupId(freeElectivesGroup.getGroupId());
                int added = 0;
                for (RequirementItem item : freeItems) {
                    if (added >= deficit) break;
                    if (item.getCourse() != null) {
                        Long cid = item.getCourse().getCourseId();
                        if (!completedCourseIds.contains(cid) && seenCourseIds.add(cid)) {
                            remainingCourses.add(item.getCourse());
                            added += item.getCourse().getCredits();
                        }
                    }
                }
            }
        }

        return remainingCourses;
    }

    private void addGroupCourses(RequirementGroup group, Set<Long> completedCourseIds,
                                  Set<Long> seenCourseIds, List<Course> remainingCourses) {
        List<RequirementItem> items = requirementItemRepository.findByRequirementGroupGroupId(group.getGroupId());
        for (RequirementItem item : items) {
            if (item.getCourse() != null) {
                Long courseId = item.getCourse().getCourseId();
                if (!completedCourseIds.contains(courseId) && seenCourseIds.add(courseId)) {
                    remainingCourses.add(item.getCourse());
                }
            } else if (item.getCourseSet() != null) {
                Long courseSetId = item.getCourseSet().getCourseSetId();
                List<CourseSetCourse> setCourses = courseSetCourseRepository.findByCourseSetCourseSetId(courseSetId);
                boolean alreadyTakenOne = false;
                for (CourseSetCourse csc : setCourses) {
                    if (completedCourseIds.contains(csc.getCourse().getCourseId())) {
                        alreadyTakenOne = true;
                        break;
                    }
                }
                if (!alreadyTakenOne && !setCourses.isEmpty()) {
                    Course chosenCourse = setCourses.get(0).getCourse();
                    if (seenCourseIds.add(chosenCourse.getCourseId())) {
                        remainingCourses.add(chosenCourse);
                    }
                }
            }
        }
    }


    private boolean isCourseUnlocked(Long courseId, Set<Long> completedCourseIds) 
    {
        // 1. Find the rule for this course (e.g., The rule for COSC 3360)
        Optional<RequisiteRule> ruleOpt = requisiteRuleRepository.findByCourseCourseId(courseId);  
              
        if (ruleOpt.isEmpty()) 
        {
            // If there's no rule in the database, it has no prerequisites! (e.g., COSC 1336)
            return true; 
        }
        
        RequisiteRule rule = ruleOpt.get();
        
        // 2. Fetch the ENTIRE tree of nodes for this rule in one highly efficient query
        List<RequisiteNode> allNodes = requisiteNodeRepository.findByRuleRuleId(rule.getRuleId());
        
        // 3. Start the recursive evaluation at the root node
        return evaluateNode(rule.getRootNode(), completedCourseIds, allNodes);
    }

    /**
     * The recursive function that walks down the AND/OR tree.
     */
    private boolean evaluateNode(RequisiteNode node, Set<Long> completedCourseIds, List<RequisiteNode> allNodes) 
    {
        if (node == null) return true;

        switch (node.getOperator()) 
        {
            case COURSE:
                // BASE CASE: Is this specific course ID in the student's list of completed courses?
                return node.getCourse() != null && completedCourseIds.contains(node.getCourse().getCourseId());
            
            case CONDITION:
                // For MVP, we ignore conditions like "JUNIOR_STANDING" and just assume true
                return true;
            
            case AND:
                // ALL children must be true. If even one is false, return false.
                List<RequisiteNode> andChildren = getChildren(node, allNodes);
                for (RequisiteNode child : andChildren) 
                {
                    if (!evaluateNode(child, completedCourseIds, allNodes)) 
                    {
                        return false; 
                    }
                }
                return true;
            
            case OR:
                // AT LEAST ONE child must be true. If we find one true, return true instantly.
                List<RequisiteNode> orChildren = getChildren(node, allNodes);
                for (RequisiteNode child : orChildren) 
                {
                    if (evaluateNode(child, completedCourseIds, allNodes)) 
                    {
                        return true;
                    }
                }
                return false;
                
            default:
                return false;
        }
    }

    /**
     * Helper method to find all child nodes of a specific parent node.
     */
    private List<RequisiteNode> getChildren(RequisiteNode parent, List<RequisiteNode> allNodes) 
    {
        return allNodes.stream()
                .filter(n -> n.getParentNode() != null && n.getParentNode().getNodeId().equals(parent.getNodeId()))
                .collect(Collectors.toList());
    }

    private int calculateScore(Course course, List<Course> remainingCourses) 
    {
        int score = 1; // Base score for just being a required class

        // Loop through all other courses the student still needs to take
        for (Course otherCourse : remainingCourses) 
        {
            // Don't compare a course to itself
            if (otherCourse.getCourseId().equals(course.getCourseId())) continue;

            Optional<RequisiteRule> ruleOpt = requisiteRuleRepository.findByCourseCourseId(otherCourse.getCourseId());
            if (ruleOpt.isPresent()) 
            {
                List<RequisiteNode> nodes = requisiteNodeRepository.findByRuleRuleId(ruleOpt.get().getRuleId());
                
                // Does 'course' appear anywhere in the prerequisite tree of 'otherCourse'?
                boolean isPrereq = nodes.stream()
                        .anyMatch(n -> n.getOperator() == RequisiteNode.Operator.COURSE 
                                  && n.getCourse() != null 
                                  && n.getCourse().getCourseId().equals(course.getCourseId()));
                
                if (isPrereq) 
                {
                    score += 10; // MASSIVE priority boost! It's a bottleneck!
                }
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

}
