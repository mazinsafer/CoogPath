package com.coogpath.coogpath.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.time.Instant;

import javax.crypto.SecretKey;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;

import com.coogpath.coogpath.config.JwtConfigTestSupport;
import com.coogpath.coogpath.config.JwtProperties;
import com.coogpath.coogpath.dto.AuthResponse;
import com.coogpath.coogpath.model.Student;

class TokenServiceTest {

    private static final String SECRET = "test-secret-that-is-at-least-32-bytes-long";

    private final JwtProperties properties = new JwtProperties(SECRET, Duration.ofHours(2), "coogpath");
    private final SecretKey key = JwtConfigTestSupport.signingKey(properties);
    private final TokenService tokenService = new TokenService(JwtConfigTestSupport.encoder(key), properties);
    private final JwtDecoder decoder = JwtConfigTestSupport.decoder(key, properties);

    private static Student student(long id) {
        Student student = new Student();
        student.setStudentId(id);
        student.setName("Jorge Coog");
        student.setEmail("jorge@uh.edu");
        return student;
    }

    @Test
    void token_subject_is_the_student_id() {
        AuthResponse auth = tokenService.issue(student(42));

        Jwt jwt = decoder.decode(auth.token());
        assertEquals("42", jwt.getSubject());
        assertEquals(42L, auth.student().studentId());
    }

    @Test
    void token_expires_after_the_configured_ttl() {
        Instant before = Instant.now();
        AuthResponse auth = tokenService.issue(student(1));

        Duration lifetime = Duration.between(before, auth.expiresAt());
        assertTrue(lifetime.compareTo(Duration.ofHours(2)) >= 0 && lifetime.compareTo(Duration.ofHours(2).plusSeconds(5)) < 0);
    }

    @Test
    void token_signed_with_another_secret_is_rejected() {
        JwtProperties other = new JwtProperties("a-completely-different-secret-of-32-bytes", Duration.ofHours(2), "coogpath");
        TokenService forger = new TokenService(
                JwtConfigTestSupport.encoder(JwtConfigTestSupport.signingKey(other)), other);

        String forged = forger.issue(student(1)).token();
        assertThrows(JwtException.class, () -> decoder.decode(forged));
    }

    @Test
    void secret_shorter_than_32_bytes_is_refused() {
        JwtProperties weak = new JwtProperties("too-short", Duration.ofHours(1), "coogpath");
        assertThrows(IllegalStateException.class, () -> JwtConfigTestSupport.signingKey(weak));
    }
}
