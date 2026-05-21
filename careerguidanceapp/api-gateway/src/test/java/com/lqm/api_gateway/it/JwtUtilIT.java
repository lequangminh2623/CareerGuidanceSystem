package com.lqm.api_gateway.it;

import com.lqm.api_gateway.utils.JwtUtil;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DisplayName("JwtUtilIT — Integration Tests")
class JwtUtilIT {

    @Value("${jwt.secret}")
    private String jwtSecret;

    private String generateToken(String email, String role, long expirationMs) throws Exception {
        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject(email)
                .claim("role", role)
                .expirationTime(new Date(System.currentTimeMillis() + expirationMs))
                .build();

        SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claimsSet);
        signedJWT.sign(new MACSigner(jwtSecret));
        return signedJWT.serialize();
    }

    @Test
    @DisplayName("validateToken — Token hợp lệ")
    void validateToken_ValidToken() throws Exception {
        String token = generateToken("test@example.com", "ADMIN", 3600000); // 1 hour valid

        Map<String, String> result = JwtUtil.validateToken(token);

        assertThat(result).isNotNull();
        assertThat(result.get("email")).isEqualTo("test@example.com");
        assertThat(result.get("role")).isEqualTo("ADMIN");
    }

    @Test
    @DisplayName("validateToken — Token hết hạn")
    void validateToken_ExpiredToken() throws Exception {
        String token = generateToken("test@example.com", "ADMIN", -3600000); // 1 hour expired

        Map<String, String> result = JwtUtil.validateToken(token);

        assertThat(result).isNull(); // JwtUtil.validateToken returns null on expiration
    }

    @Test
    @DisplayName("validateToken — Token sai định dạng/chữ ký")
    void validateToken_InvalidSignature() throws Exception {
        // Sign with a different secret
        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject("test@example.com")
                .claim("role", "ADMIN")
                .expirationTime(new Date(System.currentTimeMillis() + 3600000))
                .build();

        SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claimsSet);
        signedJWT.sign(new MACSigner("wrong_secret_key_which_must_be_long_enough"));

        String token = signedJWT.serialize();

        Map<String, String> result = JwtUtil.validateToken(token);

        assertThat(result).isNull(); // JwtUtil.validateToken returns null on invalid signature
    }
}
