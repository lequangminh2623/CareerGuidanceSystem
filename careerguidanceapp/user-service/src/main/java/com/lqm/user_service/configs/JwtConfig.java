package com.lqm.user_service.configs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtConfig {

    private static String SECRET;
    private static long EXPIRATION_MS;

    @Value("${jwt.secret}")
    public void setSecret(String secret) {
        JwtConfig.SECRET = secret;
    }

    @Value("${jwt.expiration-ms}")
    public void setExpirationMs(long expirationMs) {
        JwtConfig.EXPIRATION_MS = expirationMs;
    }

    public static String getSecret() {
        return SECRET;
    }

    public static long getExpirationMs() {
        return EXPIRATION_MS;
    }
}

