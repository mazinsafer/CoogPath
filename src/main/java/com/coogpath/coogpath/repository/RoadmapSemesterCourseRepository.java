package com.coogpath.coogpath.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.coogpath.coogpath.model.RoadmapSemesterCourse;

@Repository
public interface RoadmapSemesterCourseRepository extends JpaRepository<RoadmapSemesterCourse, Long> 
{
    
}
