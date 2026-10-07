package com.coogpath.coogpath.controller;

import com.coogpath.coogpath.config.OwnStudentOnly;
import com.coogpath.coogpath.dto.AdvisorRequest;
import com.coogpath.coogpath.dto.AdvisorResponse;
import com.coogpath.coogpath.service.AdvisorService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/advisor")
@RequiredArgsConstructor
public class AdvisorController {
    private final AdvisorService advisorService;

    @OwnStudentOnly
    @PostMapping("/{studentId}")
    public AdvisorResponse ask(@PathVariable Long studentId, @RequestBody AdvisorRequest request) {
        return advisorService.ask(studentId, request);
    }
}
