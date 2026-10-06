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
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "roadmap_snapshot")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoadmapSnapshot 
{
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name = "snapshot_id")
    private Long snapshotId;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(name = "target_graduation", length = 20, comment = "ex: Spring 2027")
    private String targetGraduation; 

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
    
}
