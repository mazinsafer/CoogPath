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

    public PlanResult generatePlan(Long studentId) 
    {
        PlanResult result = new PlanResult();

        // 0. Fetch the Student
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));

        // STEP 1: What have they already taken?
        Set<Long> completedCourseIds = getCompletedCourseIds(studentId);

        // STEP 2: What do they still need to graduate?
        List<Course> remainingCourses = getRemainingCourses(student, completedCourseIds);

        // STEP 5: GREEDY SCHEDULER
        int termCount = 0;
        int currentYear = 2025; // Hardcoded start for MVP (Next upcoming Spring)
        Term.Season currentSeason = Term.Season.SPRING;

        // Keep scheduling until they have no classes left, or we hit a 12-semester safety limit
        while (!remainingCourses.isEmpty() && termCount < 12) 
        {
            // If it's summer and they don't want summer classes, skip to Fall
            if (currentSeason == Term.Season.SUMMER && !student.isIncludeSummer()) 
            {
                currentSeason = getNextSeason(currentSeason);
                continue;
            }

            // 5a. Find all classes that are officially UNLOCKED for this semester
            List<Course> eligible = new ArrayList<>();
            for (Course c : remainingCourses) 
            {
                if (isCourseUnlocked(c.getCourseId(), completedCourseIds)) 
                {
                    eligible.add(c);
                }
            }

            // 5b. Precompute scores, then sort by Priority Score (Highest first!)
            Map<Long, Integer> scoreCache = new HashMap<>();
            for (Course c : eligible) {
                scoreCache.put(c.getCourseId(), calculateScore(c, remainingCourses));
            }
            eligible.sort((c1, c2) -> Integer.compare(
                    scoreCache.get(c2.getCourseId()), 
                    scoreCache.get(c1.getCourseId())
            ));

            int maxCredits = getMaxCreditsForSeason(student, currentSeason);
            int creditsSoFar = 0;
            List<Course> selectedCourses = new ArrayList<>();

            // 5c. Pack the courses into the semester until we hit the credit limit
            for (Course c : eligible) 
            {
                if (creditsSoFar + c.getCredits() <= maxCredits) 
                {
                    selectedCourses.add(c);
                    creditsSoFar += c.getCredits();
                }
            }

            // Edge Case: If the credit limit is too low for even ONE class, force the top priority one in anyway
            if (selectedCourses.isEmpty() && !eligible.isEmpty()) 
            {
                selectedCourses.add(eligible.get(0));
            }

            // 5d. Plate the meal! Package this semester into a DTO for React
            if (!selectedCourses.isEmpty()) 
            {
                // E.g., "Spring 2025"
                PlannedTerm plannedTerm = new PlannedTerm();
                plannedTerm.setTermLabel(currentSeason + " " + currentYear);
                plannedTerm.setSeason(currentSeason.name());
                plannedTerm.setYear(currentYear);
                
                for (Course c : selectedCourses) 
                {
                    plannedTerm.getCourses().add(new PlannedTerm.PlannedCourseDTO(
                            c.getSubject() + " " + c.getNumber(),
                            c.getTitle(),
                            c.getCredits(),
                            "Prerequisites Met", 
                            "Scheduled by CoogPath Algorithm" 
                    ));
                    plannedTerm.setTotalCredits(plannedTerm.getTotalCredits() + c.getCredits());
                    
                    // Add it to our "completed" list so NEXT semester's loop knows it's done!
                    completedCourseIds.add(c.getCourseId());
                    remainingCourses.remove(c);
                }
                result.getTerms().add(plannedTerm);
            }

            // Advance the clock to the next semester
            termCount++;
            currentSeason = getNextSeason(currentSeason);
            if (currentSeason == Term.Season.SPRING) currentYear++;
            
            // Safety break: If we found ZERO eligible classes, there's an unsolvable bottleneck (missing data)
            if (selectedCourses.isEmpty() && eligible.isEmpty()) 
            {
                break;
            }
        }

        // STEP 6: DETECT BLOCKERS
        // If the while loop finished but they still have classes left, something broke.
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

        // 1. Get all the requirement buckets (CS Core, Math, etc.) for their specific major
        List<RequirementGroup> groups = requirementGroupRepository.findByDegreeProgramProgramId(
                student.getDegreeProgram().getProgramId()
        );

        // 2. Loop through every bucket to see what courses are inside
        for (RequirementGroup group : groups) 
        {
            List<RequirementItem> items = requirementItemRepository.findByRequirementGroupGroupId(group.getGroupId());

            for (RequirementItem item : items) 
            {
                // SCENARIO A: This requirement is a specific course (like COSC 3360)
                if (item.getCourse() != null) 
                {
                    Long courseId = item.getCourse().getCourseId();
                    // Check if the student has NOT taken it yet and we haven't already added it
                    if (!completedCourseIds.contains(courseId) && seenCourseIds.add(courseId)) 
                    {
                        remainingCourses.add(item.getCourse());
                    }
                }
                // SCENARIO B: This requirement is a "Choose 1" List (like 4351 OR 4353)
                else if (item.getCourseSet() != null) 
                {
                    Long courseSetId = item.getCourseSet().getCourseSetId();
                    List<CourseSetCourse> setCourses = courseSetCourseRepository.findByCourseSetCourseSetId(courseSetId);
                    
                    boolean alreadyTakenOne = false;
                    
                    // Did the student already pass one of the choices?
                    for (CourseSetCourse csc : setCourses) 
                    {
                        if (completedCourseIds.contains(csc.getCourse().getCourseId())) 
                        {
                            alreadyTakenOne = true;
                            break;
                        }
                    }
                    
                    // If they haven't taken ANY of the choices in the list
                    if (!alreadyTakenOne && !setCourses.isEmpty()) 
                    {
                        // For the MVP, we will automatically assign them the FIRST choice in the list
                        Course chosenCourse = setCourses.get(0).getCourse();
                        if (seenCourseIds.add(chosenCourse.getCourseId())) 
                        {
                            remainingCourses.add(chosenCourse);
                        }
                    }
                }
            }
        }

        return remainingCourses;
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

    private int getMaxCreditsForSeason(Student student, Term.Season season) 
    {
        if (season == Term.Season.FALL) return student.getMaxCreditsFall();
        if (season == Term.Season.SPRING) return student.getMaxCreditsSpring();
        return student.getMaxCreditsSummer();
    }



}
