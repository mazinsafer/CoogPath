package com.coogpath.coogpath.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.coogpath.coogpath.model.RoadmapSnapshot;

public interface RoadmapSnapshotRepository extends JpaRepository<RoadmapSnapshot, Long>
{
    
}
