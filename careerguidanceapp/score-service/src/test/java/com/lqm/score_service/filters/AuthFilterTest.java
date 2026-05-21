package com.lqm.score_service.filters;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link AuthFilter}.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthFilter Unit Tests")
class AuthFilterTest {

    @InjectMocks
    private AuthFilter authFilter;

    @Mock
    private FilterChain filterChain;

    @BeforeEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void resetContext() {
        SecurityContextHolder.clearContext();
    }

    @Nested
    @DisplayName("doFilterInternal()")
    class DoFilterInternal {

        @Test
        @DisplayName("Happy Path: Valid X-User-Email and X-User-Role headers -> sets Authentication")
        void doFilter_withValidHeaders_setsAuthentication() throws Exception {
            // Arrange
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.addHeader("X-User-Email", "student@ou.edu.vn");
            request.addHeader("X-User-Role", "ROLE_STUDENT");
            MockHttpServletResponse response = new MockHttpServletResponse();

            // Act
            authFilter.doFilterInternal(request, response, filterChain);

            // Assert
            var auth = SecurityContextHolder.getContext().getAuthentication();
            assertThat(auth).isNotNull();
            assertThat(auth.getPrincipal()).isEqualTo("student@ou.edu.vn");
            assertThat(auth.getAuthorities())
                    .anyMatch(a -> a.getAuthority().equals("ROLE_STUDENT"));
            verify(filterChain).doFilter(request, response);
        }

        @Test
        @DisplayName("Exception Path: No headers -> skips Authentication, continues filter chain")
        void doFilter_withNoHeaders_skipsAuthentication() throws Exception {
            // Arrange
            MockHttpServletRequest request = new MockHttpServletRequest();
            MockHttpServletResponse response = new MockHttpServletResponse();

            // Act
            authFilter.doFilterInternal(request, response, filterChain);

            // Assert
            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
            verify(filterChain).doFilter(request, response);
        }

        @Test
        @DisplayName("Exception Path: Only email header, missing role -> skips Authentication")
        void doFilter_withOnlyEmailHeader_skipsAuthentication() throws Exception {
            // Arrange
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.addHeader("X-User-Email", "student@ou.edu.vn");
            MockHttpServletResponse response = new MockHttpServletResponse();

            // Act
            authFilter.doFilterInternal(request, response, filterChain);

            // Assert
            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
            verify(filterChain).doFilter(request, response);
        }

        @Test
        @DisplayName("Exception Path: SecurityContext already has authentication -> does not overwrite")
        void doFilter_withExistingAuthentication_doesNotOverwrite() throws Exception {
            // Arrange
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.addHeader("X-User-Email", "new@ou.edu.vn");
            request.addHeader("X-User-Role", "ROLE_ADMIN");
            MockHttpServletResponse response = new MockHttpServletResponse();

            // Set existing context
            SecurityContext ctx = SecurityContextHolder.createEmptyContext();
            var existingAuth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                    "existing@ou.edu.vn", null,
                    java.util.List.of(
                            new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_TEACHER")));
            ctx.setAuthentication(existingAuth);
            SecurityContextHolder.setContext(ctx);

            // Act
            authFilter.doFilterInternal(request, response, filterChain);

            // Assert
            assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal())
                    .isEqualTo("existing@ou.edu.vn");
            verify(filterChain).doFilter(request, response);
        }
    }
}
