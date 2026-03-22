package com.coogpath.coogpath.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.coogpath.coogpath.model.RoadmapSemester;

@Repository
public interface RoadmapSemesterRepository extends JpaRepository<RoadmapSemester, Long> 
{
    
}
