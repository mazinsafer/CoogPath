package com.coogpath.coogpath.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.coogpath.coogpath.model.Course;
import com.coogpath.coogpath.service.CourseService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController
{
    private final CourseService courseService;

    @GetMapping
    public List<Course> getAllCourses()
    {
        return courseService.getCourses();
    }
}
