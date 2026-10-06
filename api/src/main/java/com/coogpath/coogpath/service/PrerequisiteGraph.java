package com.coogpath.coogpath.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.coogpath.coogpath.model.RequisiteNode;
import com.coogpath.coogpath.model.RequisiteRule;

/**
 * In-memory view of every requisite_rule / requisite_node row, so the scheduler
 * can evaluate prerequisite trees without issuing a query per course pair.
 * Immutable after construction and shared across requests by {@link CatalogCache}.
 */
final class PrerequisiteGraph {

    private final Map<Long, RequisiteRule> ruleByCourseId = new HashMap<>();
    private final Map<Long, List<RequisiteNode>> childrenByNodeId = new HashMap<>();
    private final Map<Long, Set<Long>> prerequisiteIdsByCourseId = new HashMap<>();

    PrerequisiteGraph(List<RequisiteRule> rules, List<RequisiteNode> nodes) {
        for (RequisiteRule rule : rules) {
            ruleByCourseId.putIfAbsent(rule.getCourse().getCourseId(), rule);
        }

        Map<Long, Set<Long>> courseLeavesByRuleId = new HashMap<>();
        for (RequisiteNode node : nodes) {
            if (node.getRule() == null) continue;
            if (node.getParentNode() != null) {
                childrenByNodeId.computeIfAbsent(node.getParentNode().getNodeId(), id -> new ArrayList<>()).add(node);
            }
            if (node.getOperator() == RequisiteNode.Operator.COURSE && node.getCourse() != null) {
                courseLeavesByRuleId.computeIfAbsent(node.getRule().getRuleId(), id -> new HashSet<>())
                        .add(node.getCourse().getCourseId());
            }
        }

        ruleByCourseId.forEach((courseId, rule) -> prerequisiteIdsByCourseId.put(
                courseId, Set.copyOf(courseLeavesByRuleId.getOrDefault(rule.getRuleId(), Set.of()))));
    }

    /** True if the course has no prerequisite rule, or its AND/OR tree is satisfied by completedCourseIds. */
    boolean isUnlocked(Long courseId, Set<Long> completedCourseIds) {
        RequisiteRule rule = ruleByCourseId.get(courseId);
        if (rule == null) return true;
        return evaluate(rule.getRootNode(), completedCourseIds);
    }

    /** Course IDs that appear as COURSE leaves anywhere in this course's prerequisite tree. */
    Set<Long> prerequisiteIds(Long courseId) {
        return prerequisiteIdsByCourseId.getOrDefault(courseId, Set.of());
    }

    boolean hasPrerequisiteIn(Long courseId, Set<Long> candidates) {
        for (Long prereqId : prerequisiteIds(courseId)) {
            if (candidates.contains(prereqId)) return true;
        }
        return false;
    }

    private boolean evaluate(RequisiteNode node, Set<Long> completedCourseIds) {
        if (node == null) return true;

        switch (node.getOperator()) {
            case COURSE:
                return node.getCourse() != null && completedCourseIds.contains(node.getCourse().getCourseId());
            case CONDITION:
                // Conditions such as JUNIOR_STANDING are not modeled yet and always pass.
                return true;
            case AND:
                for (RequisiteNode child : childrenOf(node)) {
                    if (!evaluate(child, completedCourseIds)) return false;
                }
                return true;
            case OR:
                for (RequisiteNode child : childrenOf(node)) {
                    if (evaluate(child, completedCourseIds)) return true;
                }
                return false;
            default:
                return false;
        }
    }

    private List<RequisiteNode> childrenOf(RequisiteNode parent) {
        return childrenByNodeId.getOrDefault(parent.getNodeId(), List.of());
    }
}
