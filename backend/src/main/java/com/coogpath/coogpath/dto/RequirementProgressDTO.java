package com.coogpath.coogpath.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequirementProgressDTO 
{
    private String groupName; // Ex: "CS Foundation"
    private Integer creditsCompleted;
    private Integer creditsRequired;
    private boolean isSatisfied;
}
