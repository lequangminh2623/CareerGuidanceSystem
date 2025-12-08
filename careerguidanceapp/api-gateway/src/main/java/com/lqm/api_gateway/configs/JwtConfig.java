package com.lqm.api_gateway.configs;

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

    public static String getSecret() {
        return SECRET;
    }
}

