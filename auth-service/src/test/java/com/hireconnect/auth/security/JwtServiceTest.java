package com.hireconnect.auth.security;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.hireconnect.auth.entity.AuthProvider;
import com.hireconnect.auth.entity.Role;
import com.hireconnect.auth.entity.UserCredential;

public class JwtServiceTest {

    private JwtService jwtService;
    private UserCredential user;
    private final String secret = "9a4f2c8d3b7a1e6f4g5h8i9j0k1l2m3n4o5p6q7r8s9t0u1v2w3x4y5z6a7b8c9d";

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret", secret);
        ReflectionTestUtils.setField(jwtService, "expiration", 3600000L);

        user = UserCredential.builder()
                .userId(1L)
                .email("test@example.com")
                .role(Role.CANDIDATE)
                .provider(AuthProvider.LOCAL)
                .build();
    }

    @Test
    void generateAndExtract_Success() {
        String token = jwtService.generateToken(user);
        assertNotNull(token);

        assertEquals("test@example.com", jwtService.extractEmail(token));
        assertEquals(1L, jwtService.extractUserId(token));
        assertEquals("CANDIDATE", jwtService.extractRole(token));
        assertTrue(jwtService.isTokenValid(token));
    }

    @Test
    void isTokenValid_ExpiredToken_ShouldReturnFalse() {
        ReflectionTestUtils.setField(jwtService, "expiration", -1000L);
        String token = jwtService.generateToken(user);

        assertThrows(Exception.class, () -> jwtService.isTokenValid(token));
        // Note: io.jsonwebtoken usually throws ExpiredJwtException during parsing if expired
    }
    @Test
    void isTokenValid_InvalidToken_ShouldThrowException() {
        assertThrows(Exception.class, () -> jwtService.isTokenValid("invalid-token"));
    }

    @Test
    void extractClaims_InvalidToken_ShouldThrowException() {
        assertThrows(Exception.class, () -> jwtService.extractEmail("invalid-token"));
        assertThrows(Exception.class, () -> jwtService.extractUserId("invalid-token"));
        assertThrows(Exception.class, () -> jwtService.extractRole("invalid-token"));
    }
}
