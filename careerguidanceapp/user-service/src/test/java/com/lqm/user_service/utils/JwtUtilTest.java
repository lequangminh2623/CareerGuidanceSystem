package com.lqm.user_service.utils;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link JwtUtil}.
 *
 * Sau refactor: JwtUtil là Spring @Component với constructor injection.
 * Test tạo trực tiếp bằng constructor → không cần Spring Context, không cần @BeforeAll
 * để set static field.
 */
@DisplayName("JwtUtil Unit Tests")
class JwtUtilTest {

    private static final String TEST_EMAIL = "student@ou.edu.vn";
    private static final String TEST_ROLE  = "ROLE_STUDENT";
    // Secret phải >= 32 bytes cho HS256
    private static final String VALID_SECRET = "test-secret-key-that-is-at-least-32bytes!!";

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        // Khởi tạo trực tiếp qua constructor – không cần Spring Context
        jwtUtil = new JwtUtil(VALID_SECRET, 3_600_000L);
    }

    @Nested
    @DisplayName("generateToken()")
    class GenerateToken {

        @Test
        @DisplayName("Happy Path – tạo token thành công với email và role")
        void generateToken_validInput_returnsNonBlankToken() throws Exception {
            // Act
            String token = jwtUtil.generateToken(TEST_EMAIL, TEST_ROLE);

            // Assert
            assertThat(token).isNotBlank();
            // JWT format: header.payload.signature = 3 phần
            assertThat(token.split("\\.")).hasSize(3);
        }

        @Test
        @DisplayName("Happy Path – token chứa đúng subject (email) và claim role")
        void generateToken_validInput_tokenContainsCorrectClaims() throws Exception {
            // Act
            String token = jwtUtil.generateToken(TEST_EMAIL, TEST_ROLE);
            SignedJWT signed = SignedJWT.parse(token);
            JWTClaimsSet claims = signed.getJWTClaimsSet();

            // Assert
            assertThat(claims.getSubject()).isEqualTo(TEST_EMAIL);
            assertThat(claims.getStringClaim("role")).isEqualTo(TEST_ROLE);
        }

        @Test
        @DisplayName("Happy Path – token có expirationTime trong tương lai")
        void generateToken_validInput_tokenHasFutureExpiration() throws Exception {
            // Arrange
            long before = System.currentTimeMillis();

            // Act
            String token = jwtUtil.generateToken(TEST_EMAIL, TEST_ROLE);
            SignedJWT signed = SignedJWT.parse(token);
            Date expiration = signed.getJWTClaimsSet().getExpirationTime();

            // Assert
            assertThat(expiration.getTime()).isGreaterThan(before);
        }

        @Test
        @DisplayName("Exception Path – secret ngắn hơn 32 bytes → JOSEException được ném")
        void generateToken_shortSecret_throwsException() {
            // Arrange – JwtUtil với secret không đủ độ dài
            JwtUtil weakJwtUtil = new JwtUtil("short", 3_600_000L);

            // Act & Assert
            assertThatThrownBy(() -> weakJwtUtil.generateToken(TEST_EMAIL, TEST_ROLE))
                    .isInstanceOf(Exception.class);
        }

    }
}
