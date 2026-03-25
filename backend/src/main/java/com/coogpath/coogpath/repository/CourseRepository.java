package com.coogpath.coogpath.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.coogpath.coogpath.model.Course;


@Repository
public interface CourseRepository extends JpaRepository<Course, Long>
{
    Optional<Course> findBySubjectAndNumber(String subject, String number);

}
