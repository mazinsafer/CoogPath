package com.coogpath.coogpath.controller;


import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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


    /**
     * Endpoint to trigger the AI algorithm and get a degree plan.
     * GET /api/plan/generate/1
     */
    @GetMapping("/generate/{studentId}")
    public PlanResult generatePlan(@PathVariable Long studentId)
    {
        PlanResult result = planGeneratorService.generatePlan(studentId);
        return result;
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


