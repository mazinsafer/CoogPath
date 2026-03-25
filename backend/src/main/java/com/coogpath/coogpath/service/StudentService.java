package com.coogpath.coogpath.service;

import org.springframework.stereotype.Service;

import com.coogpath.coogpath.dto.StudentRegistrationDTO;
import com.coogpath.coogpath.model.DegreeProgram;
import com.coogpath.coogpath.model.Student;
import com.coogpath.coogpath.repository.DegreeProgramRepository;
import com.coogpath.coogpath.repository.StudentRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StudentService 
{
    private final StudentRepository studentRepository;
    private final DegreeProgramRepository degreeProgramRepository;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    public Student addStudent(StudentRegistrationDTO dto)
    {
        if (studentRepository.findByEmail(dto.getEmail()).isPresent()) 
        {
            throw new IllegalArgumentException("Email is already in use.");
        }
        Student student = new Student();
        student.setName(dto.getName());
        student.setEmail(dto.getEmail());
        student.setPasswordHash(passwordEncoder.encode(dto.getPassword())); 
        DegreeProgram program = degreeProgramRepository.findById(dto.getProgramId())
                .orElseThrow(() -> new IllegalArgumentException("Degree program not found"));
        student.setDegreeProgram(program);
        student.setCatalogYear(dto.getCatalogYear());
        student.setIncludeSummer(dto.isIncludeSummer());

        return studentRepository.save(student);

    }

}
