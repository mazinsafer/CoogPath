package com.coogpath.coogpath.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Sign-in token settings. {@code secret} must be at least 32 bytes and identical
 * on every API instance; when blank a random key is generated at startup.
 */
@ConfigurationProperties("app.jwt")
public record JwtProperties(String secret, Duration ttl, String issuer) {

    public JwtProperties {
        if (ttl == null) ttl = Duration.ofDays(7);
        if (issuer == null || issuer.isBlank()) issuer = "coogpath";
    }
}
