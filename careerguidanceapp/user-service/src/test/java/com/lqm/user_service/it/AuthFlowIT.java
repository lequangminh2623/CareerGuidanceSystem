package com.lqm.user_service.it;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.lqm.user_service.BaseIntegrationTest;
import com.lqm.user_service.models.Role;
import com.lqm.user_service.models.User;
import com.lqm.user_service.repositories.UserRepository;
import com.lqm.user_service.services.CloudinaryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration test cho authentication endpoints:
 * - POST /api/auth/login
 * - POST /api/auth/google
 *
 * Sử dụng {@code @SpringBootTest} + {@code @AutoConfigureMockMvc} với:
 * - PostgreSQL + Redis thực (qua {@link BaseIntegrationTest})
 * - {@link CloudinaryService} được mock bằng {@code @MockitoBean}
 * - {@link GoogleIdTokenVerifier} được mock bằng {@code @MockitoBean}
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("Auth Flow — Integration Tests")
class AuthFlowIT extends BaseIntegrationTest {

    @MockitoBean
    CloudinaryService cloudinaryService;

    @MockitoBean
    GoogleIdTokenVerifier googleIdTokenVerifier;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    UserRepository userRepository;

    @Autowired
    BCryptPasswordEncoder passwordEncoder;

    private User teacherUser;

    @BeforeEach
    void setUp() throws Exception {
        userRepository.deleteAll();
        userRepository.flush();

        teacherUser = User.builder()
                .firstName("Nguyen")
                .lastName("Van A")
                .email("teacher.vana@ou.edu.vn")
                .password(passwordEncoder.encode("password123"))
                .gender(true)
                .role(Role.ROLE_TEACHER)
                .active(true)
                .build();

        userRepository.save(teacherUser);

        // Mặc định: verify trả null (invalid token)
        when(cloudinaryService.uploadFile(any())).thenReturn(null);
        when(googleIdTokenVerifier.verify(anyString())).thenReturn(null);
    }

    // -----------------------------------------------------------------------
    // POST /api/auth/login — Happy Path
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("POST /api/auth/login — đúng credentials → 200 + token trong body")
    void login_withValidCredentials_returns200WithToken() throws Exception {
        Map<String, String> body = Map.of(
                "email", "teacher.vana@ou.edu.vn",
                "password", "password123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    // -----------------------------------------------------------------------
    // POST /api/auth/login — Error Cases
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("POST /api/auth/login — sai password → 401")
    void login_withWrongPassword_returns401() throws Exception {
        Map<String, String> body = Map.of(
                "email", "teacher.vana@ou.edu.vn",
                "password", "wrongpassword");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/auth/login — email không tồn tại → 404")
    void login_withNonExistentEmail_returns404() throws Exception {
        Map<String, String> body = Map.of(
                "email", "nobody@ou.edu.vn",
                "password", "password123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/auth/login — user bị deactivated → 404")
    void login_withDeactivatedUser_returns404() throws Exception {
        teacherUser.setActive(false);
        userRepository.save(teacherUser);

        Map<String, String> body = Map.of(
                "email", "teacher.vana@ou.edu.vn",
                "password", "password123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/auth/login — email không đúng định dạng @ou.edu.vn → 400")
    void login_withInvalidEmailFormat_returns400() throws Exception {
        Map<String, String> body = Map.of(
                "email", "invalid-email@gmail.com",
                "password", "password123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    // -----------------------------------------------------------------------
    // POST /api/auth/google — Error: invalid token
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("POST /api/auth/google — Google token không hợp lệ (verifier trả null) → 400")
    void loginWithGoogle_withInvalidToken_returns400() throws Exception {
        // googleIdTokenVerifier.verify() mặc định trả null → IllegalArgumentException → 400
        Map<String, String> body = Map.of("token", "invalid-google-token");

        mockMvc.perform(post("/api/auth/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/auth/google — user tồn tại trong DB → 200 + JWT token")
    void loginWithGoogle_withExistingUser_returns200WithToken() throws Exception {
        GoogleIdToken mockIdToken = mock(GoogleIdToken.class);
        GoogleIdToken.Payload mockPayload = mock(GoogleIdToken.Payload.class);
        when(mockIdToken.getPayload()).thenReturn(mockPayload);
        when(mockPayload.getEmail()).thenReturn("teacher.vana@ou.edu.vn");
        when(mockPayload.get("given_name")).thenReturn("Nguyen");
        when(mockPayload.get("family_name")).thenReturn("Van A");
        when(googleIdTokenVerifier.verify(anyString())).thenReturn(mockIdToken);

        Map<String, String> body = Map.of("token", "valid-google-id-token");

        mockMvc.perform(post("/api/auth/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isString());
    }

    @Test
    @DisplayName("POST /api/auth/google — user mới (chưa có trong DB) → 200 + isNewUser=true")
    void loginWithGoogle_withNewUser_returns200WithIsNewUserFlag() throws Exception {
        GoogleIdToken mockIdToken = mock(GoogleIdToken.class);
        GoogleIdToken.Payload mockPayload = mock(GoogleIdToken.Payload.class);
        when(mockIdToken.getPayload()).thenReturn(mockPayload);
        when(mockPayload.getEmail()).thenReturn("newstudent@ou.edu.vn");
        when(mockPayload.get("given_name")).thenReturn("New");
        when(mockPayload.get("family_name")).thenReturn("Student");
        when(googleIdTokenVerifier.verify(anyString())).thenReturn(mockIdToken);

        Map<String, String> body = Map.of("token", "valid-google-id-token-new-user");

        mockMvc.perform(post("/api/auth/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isNewUser").value(true))
                .andExpect(jsonPath("$.email").value("newstudent@ou.edu.vn"));
    }

    @Test
    @DisplayName("POST /api/auth/google — email không phải @ou.edu.vn → 400")
    void loginWithGoogle_withNonOuEmail_returns400() throws Exception {
        GoogleIdToken mockIdToken = mock(GoogleIdToken.class);
        GoogleIdToken.Payload mockPayload = mock(GoogleIdToken.Payload.class);
        when(mockIdToken.getPayload()).thenReturn(mockPayload);
        when(mockPayload.getEmail()).thenReturn("someone@gmail.com");
        when(mockPayload.get("given_name")).thenReturn("Someone");
        when(mockPayload.get("family_name")).thenReturn("User");
        when(googleIdTokenVerifier.verify(anyString())).thenReturn(mockIdToken);

        Map<String, String> body = Map.of("token", "google-token-non-ou-email");

        mockMvc.perform(post("/api/auth/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }
}
