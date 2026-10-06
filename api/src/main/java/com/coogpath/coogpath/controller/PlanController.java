package com.coogpath.coogpath.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.coogpath.coogpath.config.OwnStudentOnly;
import com.coogpath.coogpath.dto.PlanResult;
import com.coogpath.coogpath.service.PlanGeneratorService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/plan")
@RequiredArgsConstructor
public class PlanController
{
    private final PlanGeneratorService planGeneratorService;

    @OwnStudentOnly
    @GetMapping("/generate/{studentId}")
    public PlanResult generatePlan(
            @PathVariable Long studentId,
            @RequestParam(defaultValue = "fastest") String mode,
            @RequestParam(required = false) String startSeason,
            @RequestParam(required = false) Integer startYear,
            @RequestParam(required = false) Boolean includeSummer)
    {
        return planGeneratorService.generatePlan(studentId, mode, startSeason, startYear, includeSummer);
    }

    @OwnStudentOnly
    @PostMapping("/save/{studentId}")
    @ResponseStatus(HttpStatus.CREATED)
    public void savePlan(@PathVariable Long studentId, @RequestBody PlanResult plan)
    {
        planGeneratorService.savePlan(studentId, plan);
    }
}
