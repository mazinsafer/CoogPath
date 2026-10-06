package com.coogpath.coogpath.model;

import java.time.LocalDateTime;

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
@Table(name = "student_course")
@Data
@AllArgsConstructor
@NoArgsConstructor

public class StudentCourse 
{
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id; // transcript id?

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne
    @JoinColumn(name = "course_id", nullable=false)
    private Course course;

    @ManyToOne
    @JoinColumn(name = "term_id", comment = "nullable for transfer/planned")
    private Term term;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false )
    private Status status;

    public enum Status{TAKEN, IN_PROGRESS, PLANNED, TRANSFER}

    @Column(comment="A, B, C, D, F, W", length = 2)
    private String grade;

    @Column(name = "attempt_no", nullable = false)
    private Integer attemptNo = 1;

    @Column(name = "created_at", nullable=false)
    private LocalDateTime createdAt;

}

