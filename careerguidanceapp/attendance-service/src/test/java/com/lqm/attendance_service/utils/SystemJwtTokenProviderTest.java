package com.lqm.attendance_service.utils;

import com.lqm.attendance_service.configs.JwtConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SystemJwtTokenProviderTest {

    private SystemJwtTokenProvider tokenProvider;

    @BeforeEach
    void setUp() {
        JwtConfig config = new JwtConfig();
        config.setSecret("my-super-secret-key-for-testing-only-1234567890");
        config.setExpirationMs(3600000L);
        tokenProvider = new SystemJwtTokenProvider();
    }

    @Test
    void getBearerToken_ShouldReturnValidToken() {
        String bearerToken = tokenProvider.getBearerToken();

        assertNotNull(bearerToken);
        assertTrue(bearerToken.startsWith("Bearer "));
        
        String[] parts = bearerToken.substring(7).split("\\.");
        assertEquals(3, parts.length);
    }
    
    @Test
    void getBearerToken_CalledTwice_ShouldReturnSameToken() {
        String token1 = tokenProvider.getBearerToken();
        String token2 = tokenProvider.getBearerToken();
        
        assertEquals(token1, token2);
    }

    private void assertEquals(Object expected, Object actual) {
        org.junit.jupiter.api.Assertions.assertEquals(expected, actual);
    }
}
