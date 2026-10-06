package com.coogpath.coogpath.controller;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.coogpath.coogpath.dto.LoginDTO;
import com.coogpath.coogpath.model.Student;
import com.coogpath.coogpath.repository.StudentRepository;
import com.coogpath.coogpath.service.TokenService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    static final String INVALID_CREDENTIALS = "Invalid email or password";

    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    /**
     * Unknown email and wrong password return the same 401 body so the
     * response never reveals whether an account exists.
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginDTO loginDto) {
        if (loginDto.getEmail() == null || loginDto.getPassword() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(INVALID_CREDENTIALS);
        }

        Optional<Student> studentOpt = studentRepository.findByEmail(loginDto.getEmail().trim());
        if (studentOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(INVALID_CREDENTIALS);
        }

        Student student = studentOpt.get();
        if (!passwordEncoder.matches(loginDto.getPassword(), student.getPasswordHash())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(INVALID_CREDENTIALS);
        }

        return ResponseEntity.ok(tokenService.issue(student));
    }
}
