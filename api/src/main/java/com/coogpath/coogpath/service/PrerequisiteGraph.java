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
 * In-memory view of every requisite_rule / requisite_node row, loaded once per
 * plan generation so the scheduler can evaluate prerequisite trees without
 * issuing a query per course pair.
 */
final class PrerequisiteGraph {

    private final Map<Long, RequisiteRule> ruleByCourseId = new HashMap<>();
    private final Map<Long, List<RequisiteNode>> nodesByRuleId = new HashMap<>();

    PrerequisiteGraph(List<RequisiteRule> rules, List<RequisiteNode> nodes) {
        for (RequisiteRule rule : rules) {
            ruleByCourseId.putIfAbsent(rule.getCourse().getCourseId(), rule);
        }
        for (RequisiteNode node : nodes) {
            if (node.getRule() != null) {
                nodesByRuleId.computeIfAbsent(node.getRule().getRuleId(), id -> new ArrayList<>()).add(node);
            }
        }
    }

    /** True if the course has no prerequisite rule, or its AND/OR tree is satisfied by completedCourseIds. */
    boolean isUnlocked(Long courseId, Set<Long> completedCourseIds) {
        RequisiteRule rule = ruleByCourseId.get(courseId);
        if (rule == null) return true;
        return evaluate(rule.getRootNode(), completedCourseIds, nodesOf(rule));
    }

    /** Course IDs that appear as COURSE leaves anywhere in this course's prerequisite tree. */
    Set<Long> prerequisiteIds(Long courseId) {
        Set<Long> ids = new HashSet<>();
        RequisiteRule rule = ruleByCourseId.get(courseId);
        if (rule == null) return ids;
        for (RequisiteNode node : nodesOf(rule)) {
            if (node.getOperator() == RequisiteNode.Operator.COURSE && node.getCourse() != null) {
                ids.add(node.getCourse().getCourseId());
            }
        }
        return ids;
    }

    boolean hasPrerequisiteIn(Long courseId, Set<Long> candidates) {
        for (Long prereqId : prerequisiteIds(courseId)) {
            if (candidates.contains(prereqId)) return true;
        }
        return false;
    }

    private List<RequisiteNode> nodesOf(RequisiteRule rule) {
        return nodesByRuleId.getOrDefault(rule.getRuleId(), List.of());
    }

    private boolean evaluate(RequisiteNode node, Set<Long> completedCourseIds, List<RequisiteNode> ruleNodes) {
        if (node == null) return true;

        switch (node.getOperator()) {
            case COURSE:
                return node.getCourse() != null && completedCourseIds.contains(node.getCourse().getCourseId());
            case CONDITION:
                // Conditions such as JUNIOR_STANDING are not modeled yet and always pass.
                return true;
            case AND:
                for (RequisiteNode child : childrenOf(node, ruleNodes)) {
                    if (!evaluate(child, completedCourseIds, ruleNodes)) return false;
                }
                return true;
            case OR:
                for (RequisiteNode child : childrenOf(node, ruleNodes)) {
                    if (evaluate(child, completedCourseIds, ruleNodes)) return true;
                }
                return false;
            default:
                return false;
        }
    }

    private List<RequisiteNode> childrenOf(RequisiteNode parent, List<RequisiteNode> ruleNodes) {
        List<RequisiteNode> children = new ArrayList<>();
        for (RequisiteNode n : ruleNodes) {
            if (n.getParentNode() != null && n.getParentNode().getNodeId().equals(parent.getNodeId())) {
                children.add(n);
            }
        }
        return children;
    }
}
