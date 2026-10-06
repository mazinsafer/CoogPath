package com.coogpath.coogpath.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "requirement_item")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequirementItem 
{
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name = "item_id" )
    private Long itemId;

    @ManyToOne
    @JoinColumn(name = "group_id", nullable=false)
    private RequirementGroup requirementGroup;

    @ManyToOne
    @JoinColumn(name = "course_id", comment="set if a specific course is required")
    private Course course;

    @ManyToOne
    @JoinColumn(name = "course_set_id", comment="set if choosing from a list")
    private CourseSet courseSet;

    @Column(nullable=false )
    private boolean required = true;

    @Column(name = "min_grade", length = 2, comment = "e.g. C, B")
    private String minGrade;
}

