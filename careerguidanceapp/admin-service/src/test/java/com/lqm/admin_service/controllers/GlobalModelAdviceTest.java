package com.lqm.admin_service.controllers;

import com.lqm.admin_service.configs.JwtConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalModelAdviceTest {

    @Mock
    private JwtConfig jwtConfig;

    @InjectMocks
    private GlobalModelAdvice globalModelAdvice;

    private SecurityContext originalSecurityContext;

    @BeforeEach
    void setUp() {
        originalSecurityContext = SecurityContextHolder.getContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.setContext(originalSecurityContext);
    }

    @Test
    @DisplayName("addWebSocketToken: returns null when Authentication is null")
    void addWebSocketToken_NullAuth_ReturnsNull() {
        SecurityContextHolder.clearContext();
        String token = globalModelAdvice.addWebSocketToken();
        assertThat(token).isNull();
    }

    @Test
    @DisplayName("addWebSocketToken: returns null when Authentication is Anonymous")
    void addWebSocketToken_AnonymousAuth_ReturnsNull() {
        AnonymousAuthenticationToken auth = mock(AnonymousAuthenticationToken.class);
        when(auth.isAuthenticated()).thenReturn(true);
        
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);

        String token = globalModelAdvice.addWebSocketToken();
        assertThat(token).isNull();
    }

    @Test
    @DisplayName("addWebSocketToken: returns null when not authenticated")
    void addWebSocketToken_NotAuthenticated_ReturnsNull() {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(false);
        
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);

        String token = globalModelAdvice.addWebSocketToken();
        assertThat(token).isNull();
    }

    @Test
    @DisplayName("addWebSocketToken: returns valid JWT when authenticated")
    void addWebSocketToken_Authenticated_ReturnsJwt() {
        when(jwtConfig.getSecret()).thenReturn("very-long-secret-key-for-testing-purposes-only");
        when(jwtConfig.getExpirationMs()).thenReturn(3600000L);

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "admin@test.com", "password", List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);

        String token = globalModelAdvice.addWebSocketToken();
        
        assertThat(token).isNotNull();
    }
}
