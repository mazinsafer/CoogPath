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
@Table(name = "requisite_node")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequisiteNode 
{
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name = "node_id")
    private Long nodeId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Operator operator;
    public enum Operator {AND, OR, COURSE, CONDITION}

    @ManyToOne
    @JoinColumn(name = "course_id", comment = "only set when operator = COURSE")
    private Course course;

    @Column(name = "condition_code", length = 50, comment="e.g. JUNIOR_STANDING")
    private String conditionCode;


    @ManyToOne
    @JoinColumn(name = "parent_node_id", comment= "null = root node")
    private RequisiteNode parentNode;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    @ManyToOne
    @JoinColumn(name = "rule_id", comment = "set after requisite_rule is created")
    private RequisiteRule rule;

    
}
