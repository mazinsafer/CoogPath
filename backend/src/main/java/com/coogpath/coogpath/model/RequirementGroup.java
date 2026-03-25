package com.coogpath.coogpath.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "requirement_group")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequirementGroup 
{
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name = "group_id")
    private Long groupId;

    @ManyToOne
    @JoinColumn(name = "program_id")
    DegreeProgram degreeProgram;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "rule_type", nullable=false)
    private RuleType ruleType;

    public enum RuleType{
        ALL_OF, CHOOSE_N, MIN_CREDITS, AT_LEAST_FROM_LIST
    }

    @Column(name = "min_credits",comment="used for MIN_CREDITS and AT_LEAST_FROM_LIST")
    private Integer minCredits;

    @Column(name = "min_courses", comment="used for CHOOSE_N")
    private Integer minCourses;

    @ManyToOne
    @JoinColumn(name = "parent_group_id")
    private RequirementGroup parentGroup;
}
