package com.coogpath.coogpath.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.coogpath.coogpath.dto.LoginDTO;
import com.coogpath.coogpath.model.Student;
import com.coogpath.coogpath.repository.StudentRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {

    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Endpoint for user login.
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginDTO loginDto) 
    {
        // 1. Find the student by their email
        Optional<Student> studentOpt = studentRepository.findByEmail(loginDto.getEmail());
        
        if (studentOpt.isEmpty()) 
        {
            // Email not found
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid email or password");
        }
        
        Student student = studentOpt.get();

        // 2. Check if the typed password matches the scrambled hash in the database
        if (passwordEncoder.matches(loginDto.getPassword(), student.getPasswordHash())) 
        {
            
            // send back their ID and Name so React knows who is logged in
            Map<String, Object> response = new HashMap<>();
            response.put("studentId", student.getStudentId());
            response.put("name", student.getName());
            response.put("programId", student.getDegreeProgram().getProgramId());
            response.put("programName", student.getDegreeProgram().getName());
            response.put("capstoneChoice", student.getCapstoneChoice());
            
            return ResponseEntity.ok(response);
            
        } 
        else 
        {
            // Wrong password
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid email or password");
        }
    }
}
