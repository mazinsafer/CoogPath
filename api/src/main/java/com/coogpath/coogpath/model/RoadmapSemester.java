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
@Table(name="roadmap_semester")
@Data
@AllArgsConstructor
@NoArgsConstructor

public class RoadmapSemester 
{
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "snapshot_id", nullable = false)
    private RoadmapSnapshot snapshot;

    @Column(name = "term_label", nullable = false, length = 50, comment = "ex: Fall 2025")
    private String termLabel;

    @Column(name = "semester_order", nullable = false)
    private Integer semesterOrder;
}
