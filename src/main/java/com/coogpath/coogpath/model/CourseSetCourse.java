package com.coogpath.coogpath.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "course_set_course", uniqueConstraints={@UniqueConstraint(columnNames={"course_set_id", "course_id"})})
@Data
@NoArgsConstructor
@AllArgsConstructor

public class CourseSetCourse 
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "course_set_id", nullable = false)
    private CourseSet courseSet;

    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;
}
