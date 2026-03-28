package com.coogpath.coogpath.controller;


import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.coogpath.coogpath.dto.PlanResult;
import com.coogpath.coogpath.service.PlanGeneratorService;

import lombok.AllArgsConstructor;


@AllArgsConstructor
@RestController
@RequestMapping("/api/plan")
@CrossOrigin(origins = "*")
public class PlanController 
{
    private final PlanGeneratorService planGeneratorService;

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

    /**
     * Endpoint to save a generated plan to the database.
     * POST /api/plan/save/1
     */

     @PostMapping("/save/{studentId}")
     public void savePlanResult(Long studentId, PlanResult plan)
     {
        planGeneratorService.savePlan(studentId, plan);
     }
}


