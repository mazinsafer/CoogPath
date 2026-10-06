package com.coogpath.coogpath.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.coogpath.coogpath.model.CourseSetCourse;

@Repository
public interface CourseSetCourseRepository extends JpaRepository<CourseSetCourse, Long>
 {
    /** Every course-set membership with its course joined; insertion order decides each set's default option. */
    @EntityGraph(attributePaths = {"courseSet", "course"})
    List<CourseSetCourse> findAllByOrderByIdAsc();
}
