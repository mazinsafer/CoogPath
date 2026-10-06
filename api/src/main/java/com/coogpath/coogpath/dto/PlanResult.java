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
    
    // Courses still unscheduled when the planner hit its term limit.
    private List<String> unmetRequirements = new ArrayList<>();
    
    // Human-readable reasons courses could not be scheduled.
    private List<String> blockers = new ArrayList<>();
}
