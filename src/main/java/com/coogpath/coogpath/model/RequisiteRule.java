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

@Data
@Entity
@Table(name="requisite_rule")
@NoArgsConstructor
@AllArgsConstructor
public class RequisiteRule 
{
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name = "rule_id")
    private Long ruleId;

    @ManyToOne
    @JoinColumn(name= "course_id", nullable=false, comment="the course that HAS this prereq")
    private Course course;

    @ManyToOne
    @JoinColumn(name = "root_course_node")
    private RequisiteNode rootNode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Type type;
    public enum Type{PREREQ, COREQ}
}
