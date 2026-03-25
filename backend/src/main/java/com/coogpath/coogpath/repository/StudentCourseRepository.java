package com.coogpath.coogpath.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.coogpath.coogpath.model.StudentCourse;

public interface StudentCourseRepository extends JpaRepository<StudentCourse, Long>
{
    List<StudentCourse> findByStudentStudentId(Long studentId); // // Grabs a student's entire transcript in one go
}// ^ studentId from student model
