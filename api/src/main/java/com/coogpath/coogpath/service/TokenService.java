package com.coogpath.coogpath.service;

import java.time.Instant;

import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import com.coogpath.coogpath.config.JwtProperties;
import com.coogpath.coogpath.dto.AuthResponse;
import com.coogpath.coogpath.dto.StudentProfile;
import com.coogpath.coogpath.model.Student;

import lombok.RequiredArgsConstructor;

/** Issues the signed bearer token a student sends with every authenticated request. */
@Service
@RequiredArgsConstructor
public class TokenService {

    private final JwtEncoder jwtEncoder;
    private final JwtProperties properties;

    /** The token's subject is the student ID, which {@code @OwnStudentOnly} compares against path IDs. */
    public AuthResponse issue(Student student) {
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plus(properties.ttl());

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(properties.issuer())
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .subject(String.valueOf(student.getStudentId()))
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();

        return new AuthResponse(token, expiresAt, StudentProfile.from(student));
    }
}
