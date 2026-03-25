package com.coogpath.coogpath.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.coogpath.coogpath.dto.StudentRegistrationDTO;
import com.coogpath.coogpath.model.Student;
import com.coogpath.coogpath.service.StudentService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // Allows React to hit this endpoint
public class StudentController {

    private final StudentService studentService;

    /**
     * Endpoint to register a new student.
     * POST /api/students/register
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerStudent(@RequestBody StudentRegistrationDTO dto) {
        try {
            Student newStudent = studentService.addStudent(dto);
            // return a simple success string so we don't leak there password
            return ResponseEntity.ok("Student registered successfully with ID: " + newStudent.getStudentId());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("An error occurred during registration.");
        }
    }
}
