package com.coogpath.coogpath.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "degree_program")

@Data // Dont have to write getters and setters
@NoArgsConstructor // Required
@AllArgsConstructor // Want to create object
public class DegreeProgram 
{
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)

    @Column(name = "program_id")
    private Long programId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private String college;

    @Column(name = "catalog_year_start", nullable=false)
    private Integer catalogYearStart;

    @Column(name = "catalog_year_end", nullable = false, comment="null = still active")
    private Integer catalogYearEnd;

    @Column(name = "total_credits_required", nullable = false)
    private Integer totalCreditsRequired;


}
