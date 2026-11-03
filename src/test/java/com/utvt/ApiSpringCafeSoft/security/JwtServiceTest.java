package com.utvt.ApiSpringCafeSoft.security;

import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        byte[] key = "12345678901234567890123456789012".getBytes();
        jwtService = new JwtService(Base64.getEncoder().encodeToString(key), 3600);
    }

    @Test
    void generatesTokenForAuthenticatedUser() {
        String token = jwtService.generateToken("qa@example.com");

        assertEquals("qa@example.com", jwtService.extractUsername(token));
        org.junit.jupiter.api.Assertions.assertTrue(
                jwtService.isTokenValid(token, "qa@example.com"));
    }

    @Test
    void rejectsTokenSignedWithDifferentKey() {
        String token = jwtService.generateToken("qa@example.com");
        JwtService otherService = new JwtService(
                Base64.getEncoder().encodeToString(
                        "abcdefghijklmnopqrstuvwxyz123456".getBytes()),
                3600);

        assertThrows(JwtException.class, () -> otherService.extractUsername(token));
    }

    @Test
    void rejectsMissingOrShortSigningSecret() {
        assertThrows(IllegalStateException.class, () -> new JwtService("", 3600));
        assertThrows(
                IllegalStateException.class,
                () -> new JwtService(Base64.getEncoder().encodeToString(new byte[16]), 3600));
    }
}
