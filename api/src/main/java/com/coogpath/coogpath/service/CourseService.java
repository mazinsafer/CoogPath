package com.coogpath.coogpath.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.coogpath.coogpath.model.Course;
import com.coogpath.coogpath.repository.CourseRepository;

import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class CourseService 
{
    private final CourseRepository courseRepository;



    public List<Course> getCourses()
    {
        return courseRepository.findAll();
    }
}
