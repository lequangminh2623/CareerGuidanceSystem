package com.lqm.api_gateway.it;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.junit.jupiter.api.BeforeEach;

import java.util.Date;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Import;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(AuthGlobalFilterIT.GatewayTestConfig.class)
@DisplayName("AuthGlobalFilterIT — Integration Tests")
class AuthGlobalFilterIT {

    @TestConfiguration
    static class GatewayTestConfig {
        @Bean
        public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
            return builder.routes()
                    .route("test_route", r -> r.path("/some-protected-path/**")
                            .uri("http://httpbin.org:80"))
                    .build();
        }
    }

    private WebTestClient webTestClient;

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        this.webTestClient = WebTestClient.bindToServer().baseUrl("http://localhost:" + port).build();
    }

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
    @DisplayName("Bỏ qua các URL trong ignored-paths (Ví dụ: /user-service/api/auth)")
    void authFilter_IgnoredPath() {
        // Ignored paths should not check for token. It will just forward the request.
        // Since we don't have a real backend, we expect a 404 from the gateway when
        // trying to route
        // OR 503 if no service is found. The important part is it DOES NOT return 401.
        webTestClient.get().uri("/user-service/api/auth/login")
                .exchange()
                .expectStatus().isNotFound(); // No route matched, or route matched but no service (503). But definitely
                                              // not 401 Unauthorized.
    }

    @Test
    @DisplayName("Không có token -> 401")
    void authFilter_MissingToken() {
        webTestClient.get().uri("/some-protected-path/abc")
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Missing or invalid Auth header");
    }

    @Test
    @DisplayName("Token sai định dạng (không có Bearer) -> 401")
    void authFilter_InvalidTokenFormat() {
        webTestClient.get().uri("/some-protected-path/abc")
                .header("Authorization", "InvalidFormatToken")
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Missing or invalid Auth header");
    }

    @Test
    @DisplayName("Token không hợp lệ (sai chữ ký) -> 401")
    void authFilter_InvalidSignature() throws Exception {
        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject("test@example.com")
                .claim("role", "ADMIN")
                .expirationTime(new Date(System.currentTimeMillis() + 3600000))
                .build();

        SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claimsSet);
        signedJWT.sign(new MACSigner("wrong_secret_key_which_must_be_long_enough"));

        webTestClient.get().uri("/some-protected-path/abc")
                .header("Authorization", "Bearer " + signedJWT.serialize())
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Invalid Token or Expired");
    }

    @Test
    @DisplayName("Token hết hạn -> 401")
    void authFilter_ExpiredToken() throws Exception {
        String token = generateToken("test@example.com", "ADMIN", -3600000);

        webTestClient.get().uri("/some-protected-path/abc")
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Invalid Token or Expired");
    }
}
