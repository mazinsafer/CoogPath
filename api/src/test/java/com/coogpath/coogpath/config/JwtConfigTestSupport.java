package com.coogpath.coogpath.config;

import javax.crypto.SecretKey;

import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;

/** Exposes the package-private {@link JwtConfig} bean methods to tests in other packages. */
public final class JwtConfigTestSupport {

    private static final JwtConfig CONFIG = new JwtConfig();

    private JwtConfigTestSupport() {
    }

    public static SecretKey signingKey(JwtProperties properties) {
        return CONFIG.jwtSigningKey(properties);
    }

    public static JwtEncoder encoder(SecretKey key) {
        return CONFIG.jwtEncoder(key);
    }

    public static JwtDecoder decoder(SecretKey key, JwtProperties properties) {
        return CONFIG.jwtDecoder(key, properties);
    }
}
