package com.coogpath.coogpath.dto;

import lombok.Data;

@Data
public class StudentRegistrationDTO
{
    private String name;
    private String email;
    private String password;
    private Long programId;        // e.g. 1 for "BS Computer Science"
    private Integer catalogYear;   // e.g. 2024
    private boolean includeSummer;
    private String capstoneChoice; // SENIOR_SE, SENIOR_DS, MATH_MINOR
}
