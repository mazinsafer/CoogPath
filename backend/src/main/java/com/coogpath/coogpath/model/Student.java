package com.coogpath.coogpath.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
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
@Table(name = "student", uniqueConstraints={@UniqueConstraint(columnNames={"email"})})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Student 
{
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)

    @Column(name = "student_id")
    private Long studentId;

    @Column(nullable=false, length = 100)
    private String name;

    @Column(nullable = false, length = 100)
    private String email;

    @Column(nullable=false, length = 100)
    private String passwordHash;


    // Foreign key
    @ManyToOne // Many students can belong to one degree program
    @JoinColumn(name = "program_id") 
    private DegreeProgram degreeProgram;

    @Column(name = "catalog_year")
    private Integer catalogYear;

    @Column(name = "max_credits_fall",nullable=false)
    private Integer maxCreditsFall = 15;

    @Column(name = "max_credits_spring", nullable=false)
    private Integer maxCreditsSpring = 15;

    @Column(name = "max_credits_summer", nullable=false)
    private Integer maxCreditsSummer = 6;

    @Column(name = "include_summer", nullable=false)
    private boolean includeSummer = false;

    @Column(name = "capstone_choice", length = 20)
    private String capstoneChoice = "SENIOR_SE";

    @Column(name = "created_at", insertable=false, updatable=false)
    private LocalDateTime createdAt;

}
