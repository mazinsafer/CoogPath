package com.coogpath.coogpath.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.coogpath.coogpath.dto.RequirementGroupProgress;
import com.coogpath.coogpath.service.RequirementService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/requirements")
@RequiredArgsConstructor
public class RequirementController {

    private final RequirementService requirementService;

    @GetMapping("/{studentId}")
    public List<RequirementGroupProgress> getRequirements(@PathVariable Long studentId) {
        return requirementService.getProgress(studentId);
    }
}
