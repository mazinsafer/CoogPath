package com.coogpath.coogpath.service;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.coogpath.coogpath.dto.StudentRegistrationDTO;
import com.coogpath.coogpath.model.DegreeProgram;
import com.coogpath.coogpath.model.Student;
import com.coogpath.coogpath.repository.DegreeProgramRepository;
import com.coogpath.coogpath.repository.StudentRepository;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

    @Mock private StudentRepository studentRepository;
    @Mock private DegreeProgramRepository degreeProgramRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @InjectMocks private StudentService studentService;

    private StudentRegistrationDTO validDto;
    private DegreeProgram csProgram;

    @BeforeEach
    void setUp() {
        validDto = new StudentRegistrationDTO();
        validDto.setName("Mazin Safer");
        validDto.setEmail("mazin@uh.edu");
        validDto.setPassword("securePass123");
        validDto.setProgramId(1L);
        validDto.setCatalogYear(2024);
        validDto.setIncludeSummer(false);

        csProgram = new DegreeProgram();
        csProgram.setProgramId(1L);
        csProgram.setName("BS Computer Science");
    }

    // REGISTRATION: HAPPY PATH 

    @Test
    void registering_a_new_student_should_save_and_return_them() {
        when(studentRepository.findByEmail("mazin@uh.edu")).thenReturn(Optional.empty());
        when(degreeProgramRepository.findById(1L)).thenReturn(Optional.of(csProgram));
        when(passwordEncoder.encode("securePass123")).thenReturn("$2a$10$hashedValue");
        when(studentRepository.save(any(Student.class))).thenAnswer(inv -> {
            Student s = inv.getArgument(0);
            s.setStudentId(99L);
            return s;
        });

        Student result = studentService.addStudent(validDto);

        assertNotNull(result.getStudentId());
        assertEquals("Mazin Safer", result.getName());
        assertEquals("mazin@uh.edu", result.getEmail());
        assertEquals(csProgram, result.getDegreeProgram());
        assertEquals(2024, result.getCatalogYear());
        assertFalse(result.isIncludeSummer());
    }

    @Test
    void password_should_be_hashed_before_saving_never_stored_as_plain_text() {
        when(studentRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(degreeProgramRepository.findById(1L)).thenReturn(Optional.of(csProgram));
        when(passwordEncoder.encode("securePass123")).thenReturn("$2a$10$hashedValue");
        when(studentRepository.save(any(Student.class))).thenAnswer(inv -> inv.getArgument(0));

        Student result = studentService.addStudent(validDto);

        assertNotEquals("securePass123", result.getPasswordHash(), "Raw password must never be stored");
        assertEquals("$2a$10$hashedValue", result.getPasswordHash());
        verify(passwordEncoder).encode("securePass123");
    }

    @Test
    void student_should_be_linked_to_the_correct_degree_program() {
        when(studentRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(degreeProgramRepository.findById(1L)).thenReturn(Optional.of(csProgram));
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");
        when(studentRepository.save(any(Student.class))).thenAnswer(inv -> inv.getArgument(0));

        Student result = studentService.addStudent(validDto);

        assertEquals("BS Computer Science", result.getDegreeProgram().getName());
        assertEquals(1L, result.getDegreeProgram().getProgramId());
    }

    // REGISTRATION: DUPLICATE EMAIL

    @Test
    void registering_with_an_existing_email_should_be_rejected() {
        Student existing = new Student();
        existing.setEmail("mazin@uh.edu");
        when(studentRepository.findByEmail("mazin@uh.edu")).thenReturn(Optional.of(existing));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> studentService.addStudent(validDto)
        );

        assertTrue(ex.getMessage().toLowerCase().contains("email"));
        verify(studentRepository, never()).save(any());
    }

    //  REGISTRATION: INVALID PROGRAM 

    @Test
    void registering_with_a_nonexistent_program_should_be_rejected() {
        when(studentRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(degreeProgramRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> studentService.addStudent(validDto)
        );

        verify(studentRepository, never()).save(any());
    }

    // REGISTRATION: FIELD MAPPING

    @Test
    void summer_preference_should_be_respected_when_true() {
        validDto.setIncludeSummer(true);
        when(studentRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(degreeProgramRepository.findById(1L)).thenReturn(Optional.of(csProgram));
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");
        when(studentRepository.save(any(Student.class))).thenAnswer(inv -> inv.getArgument(0));

        Student result = studentService.addStudent(validDto);

        assertTrue(result.isIncludeSummer());
    }
}
