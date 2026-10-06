package com.coogpath.coogpath.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.coogpath.coogpath.model.DegreeProgram;
import com.coogpath.coogpath.repository.DegreeProgramRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/programs")
@RequiredArgsConstructor
public class DegreeProgramController {

    private final DegreeProgramRepository degreeProgramRepository;

    @GetMapping
    public List<DegreeProgram> getAllPrograms() {
        return degreeProgramRepository.findAll();
    }
}
