package com.coogpath.coogpath.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import com.coogpath.coogpath.model.Course;
import com.coogpath.coogpath.model.CourseSetCourse;
import com.coogpath.coogpath.model.RequirementGroup;
import com.coogpath.coogpath.model.RequirementItem;
import com.coogpath.coogpath.repository.CourseSetCourseRepository;
import com.coogpath.coogpath.repository.RequirementGroupRepository;
import com.coogpath.coogpath.repository.RequirementItemRepository;
import com.coogpath.coogpath.repository.RequisiteNodeRepository;
import com.coogpath.coogpath.repository.RequisiteRuleRepository;

/**
 * Degree requirements and the prerequisite graph, read from the database once
 * per API process. This data only changes through Flyway migrations, which ship
 * with a deploy and therefore a restart, so it is never refreshed at runtime.
 * Callers must treat the returned entities as read-only.
 */
@Component
public class CatalogCache {

    /** A requirement group with its items, in the order the planner should consider them. */
    public record GroupRequirements(RequirementGroup group, List<ItemRequirement> items) {
    }

    /**
     * Either a single required {@code course}, or a choice among {@code options}
     * (a course set). For a choice, the first option stands in when planning.
     */
    public record ItemRequirement(Course course, List<Course> options) {
        public boolean isChoice() {
            return course == null;
        }
    }

    private final RequisiteRuleRepository requisiteRuleRepository;
    private final RequisiteNodeRepository requisiteNodeRepository;
    private final RequirementGroupRepository requirementGroupRepository;
    private final RequirementItemRepository requirementItemRepository;
    private final CourseSetCourseRepository courseSetCourseRepository;
    private final TransactionTemplate readOnlyTransaction;

    private final Map<Long, List<GroupRequirements>> requirementsByProgramId = new ConcurrentHashMap<>();
    private volatile PrerequisiteGraph prerequisiteGraph;

    public CatalogCache(RequisiteRuleRepository requisiteRuleRepository,
                        RequisiteNodeRepository requisiteNodeRepository,
                        RequirementGroupRepository requirementGroupRepository,
                        RequirementItemRepository requirementItemRepository,
                        CourseSetCourseRepository courseSetCourseRepository,
                        PlatformTransactionManager transactionManager) {
        this.requisiteRuleRepository = requisiteRuleRepository;
        this.requisiteNodeRepository = requisiteNodeRepository;
        this.requirementGroupRepository = requirementGroupRepository;
        this.requirementItemRepository = requirementItemRepository;
        this.courseSetCourseRepository = courseSetCourseRepository;
        this.readOnlyTransaction = new TransactionTemplate(transactionManager);
        this.readOnlyTransaction.setReadOnly(true);
    }

    PrerequisiteGraph prerequisiteGraph() {
        PrerequisiteGraph graph = prerequisiteGraph;
        if (graph == null) {
            synchronized (this) {
                graph = prerequisiteGraph;
                if (graph == null) {
                    // One persistence context for both queries, so each shared node and course loads once.
                    graph = readOnlyTransaction.execute(status -> new PrerequisiteGraph(
                            requisiteRuleRepository.findAll(), requisiteNodeRepository.findAll()));
                    prerequisiteGraph = graph;
                }
            }
        }
        return graph;
    }

    /** Every requirement group of the program (not yet filtered by the student's options). */
    public List<GroupRequirements> requirementsFor(Long programId) {
        return requirementsByProgramId.computeIfAbsent(programId,
                id -> readOnlyTransaction.execute(status -> loadRequirements(id)));
    }

    private List<GroupRequirements> loadRequirements(Long programId) {
        Map<Long, List<RequirementItem>> itemsByGroupId = requirementItemRepository
                .findByRequirementGroupDegreeProgramProgramIdOrderByItemIdAsc(programId).stream()
                .collect(Collectors.groupingBy(item -> item.getRequirementGroup().getGroupId(),
                        LinkedHashMap::new, Collectors.toList()));

        Map<Long, List<Course>> optionsByCourseSetId = courseSetCourseRepository.findAllByOrderByIdAsc().stream()
                .collect(Collectors.groupingBy(csc -> csc.getCourseSet().getCourseSetId(),
                        LinkedHashMap::new, Collectors.mapping(CourseSetCourse::getCourse, Collectors.toList())));

        List<GroupRequirements> result = new ArrayList<>();
        for (RequirementGroup group : requirementGroupRepository.findByDegreeProgramProgramId(programId)) {
            List<ItemRequirement> items = new ArrayList<>();
            for (RequirementItem item : itemsByGroupId.getOrDefault(group.getGroupId(), List.of())) {
                if (item.getCourse() != null) {
                    items.add(new ItemRequirement(item.getCourse(), List.of()));
                } else if (item.getCourseSet() != null) {
                    List<Course> options = optionsByCourseSetId.getOrDefault(item.getCourseSet().getCourseSetId(), List.of());
                    items.add(new ItemRequirement(null, List.copyOf(options)));
                }
            }
            result.add(new GroupRequirements(group, List.copyOf(items)));
        }
        return List.copyOf(result);
    }
}
