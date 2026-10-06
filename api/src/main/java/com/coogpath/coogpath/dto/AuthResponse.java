package com.coogpath.coogpath.dto;

import java.time.Instant;

/** Returned by login and registration. Send {@code token} as "Authorization: Bearer <token>". */
public record AuthResponse(String token, Instant expiresAt, StudentProfile student) {
}
