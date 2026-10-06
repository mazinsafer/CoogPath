package com.coogpath.coogpath.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.coogpath.coogpath.model.Course;
import com.coogpath.coogpath.model.RequisiteNode;
import com.coogpath.coogpath.model.RequisiteNode.Operator;
import com.coogpath.coogpath.model.RequisiteRule;

class PrerequisiteGraphTest {

    private static final long CALC_I = 15L;
    private static final long CALC_II = 16L;
    private static final long LINEAR_ALGEBRA = 19L;
    private static final long ENG_MATH = 20L;

    private final List<RequisiteRule> rules = new ArrayList<>();
    private final List<RequisiteNode> nodes = new ArrayList<>();
    private long nextNodeId = 1;

    private static Course course(long id) {
        return new Course(id, "MATH", String.valueOf(id), "Course " + id, 3);
    }

    private RequisiteNode node(Operator op, Long courseId, RequisiteNode parent, RequisiteRule rule) {
        RequisiteNode node = new RequisiteNode();
        node.setNodeId(nextNodeId++);
        node.setOperator(op);
        node.setCourse(courseId != null ? course(courseId) : null);
        node.setParentNode(parent);
        node.setRule(rule);
        nodes.add(node);
        return node;
    }

    private RequisiteRule rule(long courseId) {
        RequisiteRule rule = new RequisiteRule();
        rule.setRuleId((long) rules.size() + 1);
        rule.setCourse(course(courseId));
        rule.setType(RequisiteRule.Type.PREREQ);
        rules.add(rule);
        return rule;
    }

    @Test
    void course_without_a_rule_is_always_unlocked() {
        PrerequisiteGraph graph = new PrerequisiteGraph(rules, nodes);
        assertTrue(graph.isUnlocked(CALC_I, Set.of()));
    }

    @Test
    void single_course_prerequisite_must_be_completed() {
        RequisiteRule calcII = rule(CALC_II);
        calcII.setRootNode(node(Operator.COURSE, CALC_I, null, calcII));
        PrerequisiteGraph graph = new PrerequisiteGraph(rules, nodes);

        assertFalse(graph.isUnlocked(CALC_II, Set.of()));
        assertTrue(graph.isUnlocked(CALC_II, Set.of(CALC_I)));
        assertEquals(Set.of(CALC_I), graph.prerequisiteIds(CALC_II));
    }

    @Test
    void or_tree_is_satisfied_by_any_child() {
        // MATH 2318 requires MATH 2414 OR MATH 3321
        RequisiteRule linear = rule(LINEAR_ALGEBRA);
        RequisiteNode root = node(Operator.OR, null, null, linear);
        node(Operator.COURSE, CALC_II, root, linear);
        node(Operator.COURSE, ENG_MATH, root, linear);
        linear.setRootNode(root);
        PrerequisiteGraph graph = new PrerequisiteGraph(rules, nodes);

        assertFalse(graph.isUnlocked(LINEAR_ALGEBRA, Set.of(CALC_I)));
        assertTrue(graph.isUnlocked(LINEAR_ALGEBRA, Set.of(ENG_MATH)));
        assertTrue(graph.hasPrerequisiteIn(LINEAR_ALGEBRA, Set.of(CALC_II)));
    }

    @Test
    void and_tree_requires_every_child() {
        RequisiteRule engMath = rule(ENG_MATH);
        RequisiteNode root = node(Operator.AND, null, null, engMath);
        node(Operator.COURSE, CALC_I, root, engMath);
        node(Operator.COURSE, CALC_II, root, engMath);
        engMath.setRootNode(root);
        PrerequisiteGraph graph = new PrerequisiteGraph(rules, nodes);

        assertFalse(graph.isUnlocked(ENG_MATH, Set.of(CALC_I)));
        assertTrue(graph.isUnlocked(ENG_MATH, Set.of(CALC_I, CALC_II)));
    }
}
