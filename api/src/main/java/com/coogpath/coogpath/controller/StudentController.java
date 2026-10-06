package com.coogpath.coogpath.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.coogpath.coogpath.dto.StudentPreferencesRequest;
import com.coogpath.coogpath.dto.StudentProfile;
import com.coogpath.coogpath.dto.StudentRegistrationDTO;
import com.coogpath.coogpath.dto.TranscriptEntry;
import com.coogpath.coogpath.model.Student;
import com.coogpath.coogpath.service.StudentService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public StudentProfile registerStudent(@RequestBody StudentRegistrationDTO dto) {
        Student student = studentService.addStudent(dto);
        return StudentProfile.from(student);
    }

    @GetMapping("/{studentId}")
    public StudentProfile getStudent(@PathVariable Long studentId) {
        return studentService.getProfile(studentId);
    }

    @PatchMapping("/{studentId}/preferences")
    public StudentProfile updatePreferences(
            @PathVariable Long studentId,
            @RequestBody StudentPreferencesRequest request) {
        return studentService.updatePreferences(studentId, request);
    }

    @GetMapping("/{studentId}/transcript")
    public List<TranscriptEntry> getTranscript(@PathVariable Long studentId) {
        return studentService.getTranscript(studentId);
    }

    @GetMapping("/{studentId}/courses")
    public List<Long> getCompletedCourses(@PathVariable Long studentId) {
        return studentService.getCompletedCourseIds(studentId);
    }

    @PutMapping("/{studentId}/courses")
    public Map<String, Integer> replaceCompletedCourses(
            @PathVariable Long studentId,
            @RequestBody List<Number> courseIds) {
        List<Long> ids = courseIds.stream().map(Number::longValue).toList();
        return Map.of("saved", studentService.replaceCompletedCourses(studentId, ids));
    }
}
