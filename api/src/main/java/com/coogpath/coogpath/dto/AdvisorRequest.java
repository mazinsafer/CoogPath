package com.coogpath.coogpath.dto;

import java.util.List;

public record AdvisorRequest(String question, String mode, String startSeason, Integer startYear,
                             Boolean includeSummer, List<Message> history) {
    public record Message(String role, String content) {}
}
