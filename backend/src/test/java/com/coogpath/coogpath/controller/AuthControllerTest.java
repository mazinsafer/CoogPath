package com.coogpath.coogpath.controller;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.coogpath.coogpath.dto.LoginDTO;
import com.coogpath.coogpath.model.DegreeProgram;
import com.coogpath.coogpath.model.Student;
import com.coogpath.coogpath.repository.StudentRepository;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock private StudentRepository studentRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @InjectMocks private AuthController authController;

    private Student jorge;
    private LoginDTO loginDto;

    @BeforeEach
    void setUp() {
        DegreeProgram cs = new DegreeProgram();
        cs.setProgramId(1L);
        cs.setName("BS Computer Science");

        jorge = new Student();
        jorge.setStudentId(1L);
        jorge.setName("Jorge Coog");
        jorge.setEmail("jorge@uh.edu");
        jorge.setPasswordHash("$2a$10$encodedHash");
        jorge.setDegreeProgram(cs);

        loginDto = new LoginDTO();
        loginDto.setEmail("jorge@uh.edu");
        loginDto.setPassword("correctPassword");
    }

    // LOGIN: HAPPY PATH

    @Test
    void valid_credentials_should_return_200_with_student_info() {
        when(studentRepository.findByEmail("jorge@uh.edu")).thenReturn(Optional.of(jorge));
        when(passwordEncoder.matches("correctPassword", "$2a$10$encodedHash")).thenReturn(true);

        ResponseEntity<?> response = authController.login(loginDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @SuppressWarnings("unchecked")
    void successful_login_should_return_student_id_name_and_program() {
        when(studentRepository.findByEmail("jorge@uh.edu")).thenReturn(Optional.of(jorge));
        when(passwordEncoder.matches("correctPassword", "$2a$10$encodedHash")).thenReturn(true);

        ResponseEntity<?> response = authController.login(loginDto);
        Map<String, Object> body = (Map<String, Object>) response.getBody();

        assertNotNull(body);
        assertEquals(1L, body.get("studentId"));
        assertEquals("Jorge Coog", body.get("name"));
        assertEquals(1L, body.get("programId"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void successful_login_should_never_return_the_password() {
        when(studentRepository.findByEmail("jorge@uh.edu")).thenReturn(Optional.of(jorge));
        when(passwordEncoder.matches("correctPassword", "$2a$10$encodedHash")).thenReturn(true);

        ResponseEntity<?> response = authController.login(loginDto);
        Map<String, Object> body = (Map<String, Object>) response.getBody();

        assertNotNull(body);
        assertFalse(body.containsKey("password"), "Password must never be in the response");
        assertFalse(body.containsKey("passwordHash"), "Password hash must never be in the response");
    }

    // LOGIN: WRONG PASSWORD 
    @Test
    void wrong_password_should_return_401() {
        when(studentRepository.findByEmail("jorge@uh.edu")).thenReturn(Optional.of(jorge));
        when(passwordEncoder.matches("wrongPassword", "$2a$10$encodedHash")).thenReturn(false);

        loginDto.setPassword("wrongPassword");
        ResponseEntity<?> response = authController.login(loginDto);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void wrong_password_error_should_not_reveal_that_the_email_exists() {
        when(studentRepository.findByEmail("jorge@uh.edu")).thenReturn(Optional.of(jorge));
        when(passwordEncoder.matches("wrongPassword", "$2a$10$encodedHash")).thenReturn(false);

        loginDto.setPassword("wrongPassword");
        ResponseEntity<?> response = authController.login(loginDto);
        String body = (String) response.getBody();

        assertNotNull(body);
        assertFalse(body.toLowerCase().contains("password is wrong"),
                "Should not reveal which field is incorrect");
        assertTrue(body.toLowerCase().contains("invalid"));
    }

    //  LOGIN: EMAIL NOT FOUND

    @Test
    void nonexistent_email_should_return_401() {
        when(studentRepository.findByEmail("nobody@uh.edu")).thenReturn(Optional.empty());

        loginDto.setEmail("nobody@uh.edu");
        ResponseEntity<?> response = authController.login(loginDto);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void nonexistent_email_should_give_the_same_error_as_wrong_password() {
        when(studentRepository.findByEmail("nobody@uh.edu")).thenReturn(Optional.empty());
        loginDto.setEmail("nobody@uh.edu");
        ResponseEntity<?> emailNotFoundResponse = authController.login(loginDto);

        when(studentRepository.findByEmail("jorge@uh.edu")).thenReturn(Optional.of(jorge));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);
        loginDto.setEmail("jorge@uh.edu");
        loginDto.setPassword("wrongPassword");
        ResponseEntity<?> wrongPasswordResponse = authController.login(loginDto);

        assertEquals(emailNotFoundResponse.getBody(), wrongPasswordResponse.getBody(),
                "Error messages must be identical to prevent email enumeration");
    }

    // LOGIN: PASSWORD COMPARISON

    @Test
    void password_should_be_compared_using_the_encoder_not_string_equals() {
        when(studentRepository.findByEmail("jorge@uh.edu")).thenReturn(Optional.of(jorge));
        when(passwordEncoder.matches("correctPassword", "$2a$10$encodedHash")).thenReturn(true);

        authController.login(loginDto);

        verify(passwordEncoder).matches("correctPassword", "$2a$10$encodedHash");
    }
}
