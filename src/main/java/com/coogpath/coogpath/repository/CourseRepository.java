package com.coogpath.coogpath.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.coogpath.coogpath.model.Course;


@Repository
public interface CourseRepository extends JpaRepository<Course, Long>
{
    
}
