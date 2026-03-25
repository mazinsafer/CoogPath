package com.coogpath.coogpath.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlanResult 
{
    private List<PlannedTerm> terms = new ArrayList<>();
    
    // If the algorithm couldn't fit everything in 12 semesters
    private List<String> unmetRequirements = new ArrayList<>();
    
    // Human-readable explanations of what went wrong (e.g., "COSC 3360 blocked by COSC 2436")
    private List<String> blockers = new ArrayList<>();
}
