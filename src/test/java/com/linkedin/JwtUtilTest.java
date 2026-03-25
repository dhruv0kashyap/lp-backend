package com.linkedin;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.linkedin.security.JwtUtil;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

private JwtUtil jwtUtil;

@BeforeEach
void setUp() {
    jwtUtil = new JwtUtil();
    ReflectionTestUtils.setField(jwtUtil, "secret", "linkedin-capstone-super-secret-key-must-be-256-bits-long-for-hs256");
    ReflectionTestUtils.setField(jwtUtil, "expiration", 3600000L);
}

@Test
void generateToken_ReturnsValidToken() {
    String token = jwtUtil.generateToken("john.doe@example.com");

    assertNotNull(token);
    assertFalse(token.isBlank());
}

@Test
void extractEmail_ReturnsCorrectEmail() {
    String token = jwtUtil.generateToken("john.doe@example.com");

    String email = jwtUtil.extractEmail(token);

    assertEquals("john.doe@example.com", email);
}

@Test
void validateToken_ValidToken_ReturnsTrue() {
    String token = jwtUtil.generateToken("john.doe@example.com");

    boolean valid = jwtUtil.validateToken(token);

    assertTrue(valid);
}

@Test
void validateToken_InvalidToken_ReturnsFalse() {
    boolean valid = jwtUtil.validateToken("this.is.an.invalid.token");

    assertFalse(valid);
}

@Test
void validateToken_ExpiredToken_ReturnsFalse() {
    JwtUtil shortExpiryJwtUtil = new JwtUtil();
    ReflectionTestUtils.setField(shortExpiryJwtUtil, "secret",
            "linkedin-capstone-super-secret-key-must-be-256-bits-long-for-hs256");
    ReflectionTestUtils.setField(shortExpiryJwtUtil, "expiration", -1000L);

    String expiredToken = shortExpiryJwtUtil.generateToken("john.doe@example.com");

    boolean valid = shortExpiryJwtUtil.validateToken(expiredToken);

    assertFalse(valid);
}

}

