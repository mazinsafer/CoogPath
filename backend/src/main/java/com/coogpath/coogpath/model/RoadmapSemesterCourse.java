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
@Table(name = "roadmap_semester_course")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoadmapSemesterCourse 
{
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "semester_id", nullable = false)
    private RoadmapSemester semester;

    
    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false, comment = "The actualy course being scheduled")
    private Course course;


    @ManyToOne
    @JoinColumn(name = "requirement_group_id", comment="which group this course satisfies")
    private RequirementGroup requirementGroup;

    @Column(columnDefinition = "TEXT", comment= "AI or algorithm explanation")
    private String reason;
}
