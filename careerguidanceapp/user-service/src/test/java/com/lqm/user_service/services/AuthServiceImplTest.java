package com.lqm.user_service.services;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.lqm.user_service.dtos.UserLoginDTO;
import com.lqm.user_service.exceptions.AuthenticationFailedException;
import com.lqm.user_service.exceptions.ResourceNotFoundException;
import com.lqm.user_service.models.Role;
import com.lqm.user_service.models.User;
import com.lqm.user_service.repositories.UserRepository;
import com.lqm.user_service.services.impl.AuthServiceImpl;
import com.lqm.user_service.utils.JwtUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link AuthServiceImpl}.
 *
 * Sau khi refactor:
 * - {@link GoogleIdTokenVerifier} được inject → có thể mock hoàn toàn.
 * - {@link JwtUtil} được inject → có thể mock hoàn toàn.
 * - try-catch tách rõ → các exception nghiệp vụ (IllegalArgumentException) không bị nuốt,
 *   nên test có thể kiểm tra từng exception path chính xác.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthServiceImpl Unit Tests")
class AuthServiceImplTest {

    @Mock private AuthenticationManager authenticationManager;
    @Mock private UserRepository userRepo;
    @Mock private MessageSource messageSource;
    @Mock private GoogleIdTokenVerifier googleIdTokenVerifier;
    @Mock private JwtUtil jwtUtil;

    @InjectMocks
    private AuthServiceImpl authService;

    // ------------------------------------------------------------------ helpers
    private User buildUser(String email, Role role) {
        return User.builder()
                .id(UUID.randomUUID())
                .firstName("Minh").lastName("Le")
                .email(email).password("encoded")
                .role(role).gender(true).active(true)
                .build();
    }

    private void stubMessage(String code, String msg) {
        when(messageSource.getMessage(eq(code), any(), any())).thenReturn(msg);
    }

    // ================================================================== login()
    @Nested
    @DisplayName("login()")
    class Login {

        @Test
        @DisplayName("Happy Path – đăng nhập thành công, trả về JWT token hợp lệ")
        void login_validCredentials_returnsJwtToken() throws Exception {
            // Arrange
            UserLoginDTO dto = UserLoginDTO.builder()
                    .email("student@ou.edu.vn")
                    .password("correctPass")
                    .build();
            User user = buildUser("student@ou.edu.vn", Role.ROLE_STUDENT);

            when(userRepo.findByEmailAndActiveTrue("student@ou.edu.vn"))
                    .thenReturn(Optional.of(user));

            Authentication auth = mock(Authentication.class);
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(auth);
            when(jwtUtil.generateToken("student@ou.edu.vn", "ROLE_STUDENT"))
                    .thenReturn("mocked.jwt.token");

            // Act
            String token = authService.login(dto);

            // Assert
            assertThat(token).isEqualTo("mocked.jwt.token");
            verify(authenticationManager).authenticate(any());
            verify(jwtUtil).generateToken("student@ou.edu.vn", "ROLE_STUDENT");
        }

        @Test
        @DisplayName("Exception Path – ném ResourceNotFoundException khi email không tồn tại")
        void login_emailNotFound_throwsResourceNotFoundException() {
            // Arrange
            UserLoginDTO dto = UserLoginDTO.builder()
                    .email("ghost@ou.edu.vn")
                    .password("anyPass")
                    .build();
            when(userRepo.findByEmailAndActiveTrue("ghost@ou.edu.vn"))
                    .thenReturn(Optional.empty());
            stubMessage("user.notFound", "User not found");

            // Act & Assert
            assertThatThrownBy(() -> authService.login(dto))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("User not found");

            verifyNoInteractions(authenticationManager);
            verifyNoInteractions(jwtUtil);
        }

        @Test
        @DisplayName("Exception Path – ném AuthenticationFailedException khi sai password")
        void login_wrongPassword_throwsAuthenticationFailedException() {
            // Arrange
            UserLoginDTO dto = UserLoginDTO.builder()
                    .email("student@ou.edu.vn")
                    .password("wrongPass")
                    .build();
            when(userRepo.findByEmailAndActiveTrue("student@ou.edu.vn"))
                    .thenReturn(Optional.of(buildUser("student@ou.edu.vn", Role.ROLE_STUDENT)));
            when(authenticationManager.authenticate(any()))
                    .thenThrow(new BadCredentialsException("Bad credentials"));
            stubMessage("password.invalid", "Invalid password");

            // Act & Assert
            assertThatThrownBy(() -> authService.login(dto))
                    .isInstanceOf(AuthenticationFailedException.class)
                    .hasMessageContaining("Invalid password");

            verifyNoInteractions(jwtUtil);
        }
    }

    // ================================================================== loginWithGoogle()
    @Nested
    @DisplayName("loginWithGoogle()")
    class LoginWithGoogle {

        @Test
        @DisplayName("Happy Path – user đã tồn tại → trả về JWT token")
        void loginWithGoogle_existingUser_returnsToken() throws Exception {
            // Arrange
            GoogleIdToken mockIdToken = mock(GoogleIdToken.class);
            GoogleIdToken.Payload payload = new GoogleIdToken.Payload();
            payload.setEmail("student@ou.edu.vn");
            payload.set("given_name", "Minh");
            payload.set("family_name", "Le");

            when(googleIdTokenVerifier.verify("valid.google.token")).thenReturn(mockIdToken);
            when(mockIdToken.getPayload()).thenReturn(payload);

            User user = buildUser("student@ou.edu.vn", Role.ROLE_STUDENT);
            when(userRepo.findByEmailAndActiveTrue("student@ou.edu.vn"))
                    .thenReturn(Optional.of(user));
            when(jwtUtil.generateToken("student@ou.edu.vn", "ROLE_STUDENT"))
                    .thenReturn("mocked.jwt.token");

            // Act
            Map<String, Object> result = authService.loginWithGoogle("valid.google.token");

            // Assert
            assertThat(result).containsKey("token");
            assertThat(result.get("token")).isEqualTo("mocked.jwt.token");
        }

        @Test
        @DisplayName("Happy Path – user chưa tồn tại → trả về info để đăng ký")
        void loginWithGoogle_newUser_returnsRegistrationInfo() throws Exception {
            // Arrange
            GoogleIdToken mockIdToken = mock(GoogleIdToken.class);
            GoogleIdToken.Payload payload = new GoogleIdToken.Payload();
            payload.setEmail("newstudent@ou.edu.vn");
            payload.set("given_name", "New");
            payload.set("family_name", "Student");

            when(googleIdTokenVerifier.verify("valid.google.token")).thenReturn(mockIdToken);
            when(mockIdToken.getPayload()).thenReturn(payload);
            when(userRepo.findByEmailAndActiveTrue("newstudent@ou.edu.vn"))
                    .thenReturn(Optional.empty());

            // Act
            Map<String, Object> result = authService.loginWithGoogle("valid.google.token");

            // Assert
            assertThat(result).containsEntry("email", "newstudent@ou.edu.vn");
            assertThat(result).containsEntry("firstName", "New");
            assertThat(result).containsEntry("isNewUser", true);
            verifyNoInteractions(jwtUtil);
        }

        @Test
        @DisplayName("Exception Path – token Google không hợp lệ (verifier trả null) → IllegalArgumentException")
        void loginWithGoogle_invalidToken_throwsIllegalArgumentException() throws Exception {
            // Arrange
            when(googleIdTokenVerifier.verify("bad.token")).thenReturn(null);
            stubMessage("token.invalid", "Token không hợp lệ");

            // Act & Assert
            // Sau refactor: IllegalArgumentException KHÔNG bị nuốt thành RuntimeException
            assertThatThrownBy(() -> authService.loginWithGoogle("bad.token"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Token không hợp lệ");

            verifyNoInteractions(userRepo);
            verifyNoInteractions(jwtUtil);
        }

        @Test
        @DisplayName("Exception Path – email không phải @ou.edu.vn → IllegalArgumentException")
        void loginWithGoogle_invalidEmailDomain_throwsIllegalArgumentException() throws Exception {
            // Arrange
            GoogleIdToken mockIdToken = mock(GoogleIdToken.class);
            GoogleIdToken.Payload payload = new GoogleIdToken.Payload();
            payload.setEmail("hacker@gmail.com");   // email ngoài trường
            payload.set("given_name", "Hack");
            payload.set("family_name", "Er");

            when(googleIdTokenVerifier.verify("valid.google.token")).thenReturn(mockIdToken);
            when(mockIdToken.getPayload()).thenReturn(payload);
            stubMessage("user.email.invalid", "Email không hợp lệ");

            // Act & Assert
            assertThatThrownBy(() -> authService.loginWithGoogle("valid.google.token"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Email không hợp lệ");

            verifyNoInteractions(userRepo);
            verifyNoInteractions(jwtUtil);
        }

        @Test
        @DisplayName("Exception Path – verifier ném GeneralSecurityException → RuntimeException (lỗi I/O)")
        void loginWithGoogle_verifierThrowsSecurityException_throwsRuntimeException() throws Exception {
            // Arrange
            when(googleIdTokenVerifier.verify(anyString()))
                    .thenThrow(new java.security.GeneralSecurityException("SSL error"));
            stubMessage("error", "Lỗi hệ thống");

            // Act & Assert
            assertThatThrownBy(() -> authService.loginWithGoogle("any.token"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Lỗi hệ thống")
                    .hasCauseInstanceOf(java.security.GeneralSecurityException.class);
        }
    }
}
