package com.coogpath.coogpath.dto;

/** Partial update: any null field is left unchanged. */
public record StudentPreferencesRequest(
        String capstoneChoice,
        String financeTrack,
        Boolean mathMinor,
        Integer freeElectiveCredits,
        Boolean includeSummer) {
}
