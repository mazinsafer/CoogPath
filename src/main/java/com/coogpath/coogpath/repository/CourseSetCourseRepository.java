package com.coogpath.coogpath.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.coogpath.coogpath.model.CourseSetCourse;

@Repository
public interface CourseSetCourseRepository extends JpaRepository<CourseSetCourse, Long>
 {
    // Fetches all the courses mapped to a specific Course Set (e.g., both 4351 and 4353)
    List<CourseSetCourse> findByCourseSetCourseSetId(Long courseSetId);
}