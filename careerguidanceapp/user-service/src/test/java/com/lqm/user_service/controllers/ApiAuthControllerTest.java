package com.lqm.user_service.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lqm.user_service.exceptions.AuthenticationFailedException;
import com.lqm.user_service.exceptions.ResourceNotFoundException;
import com.lqm.user_service.dtos.UserDetailsResponseDTO;
import com.lqm.user_service.dtos.UserLoginDTO;
import com.lqm.user_service.mappers.UserMapper;
import com.lqm.user_service.models.Role;
import com.lqm.user_service.models.User;
import com.lqm.user_service.services.AuthService;
import com.lqm.user_service.services.UserService;
import com.lqm.user_service.validators.WebAppValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.context.MessageSource;
import org.springframework.mock.web.MockMultipartFile;

/**
 * Slice Test cho {@link ApiAuthController}.
 *
 * ⚠️ Ghi chú Clean Code / RESTful:
 * 1. POST /auth/login trả về token trong Map → chuẩn, nhưng nên dùng record
 * LoginResponseDTO
 * để type-safe hơn thay vì Map<String, String>.
 * 2. POST /auth/signup nên trả về 201 Created (đã đúng) với Location header trỏ
 * đến
 * /api/secure/me hoặc /api/internal/admin/users/{id} để đúng chuẩn REST.
 * 3. GlobalExceptionHandler inject HttpServletRequest → cần @Import hoặc thêm
 * vào context
 * của @WebMvcTest để hoạt động.
 */
@WebMvcTest(controllers = ApiAuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("ApiAuthController Slice Tests")
class ApiAuthControllerTest {

        @Autowired
        MockMvc mockMvc;
        @Autowired
        ObjectMapper objectMapper;

        @MockitoBean
        AuthService authService;
        @MockitoBean
        UserService userService;
        @MockitoBean
        UserMapper userMapper;
        // WebAppValidator và GlobalExceptionHandler được load tự động bởi @WebMvcTest
        @MockitoBean
        WebAppValidator webAppValidator;
        @MockitoBean
        MessageSource messageSource;

        // ========================================================================
        // /auth/google
        @Nested
        @DisplayName("POST /api/auth/google")
        class LoginWithGoogle {

                @Test
                @DisplayName("Happy Path – Google token hợp lệ, user đã tồn tại → trả về JWT")
                void loginWithGoogle_validToken_existingUser_returns200WithToken() throws Exception {
                        // Arrange
                        Map<String, Object> serviceResult = Map.of("token", "jwt.token.here");
                        when(authService.loginWithGoogle("google-id-token")).thenReturn(serviceResult);

                        String body = objectMapper.writeValueAsString(Map.of("token", "google-id-token"));

                        // Act & Assert
                        mockMvc.perform(post("/api/auth/google")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(body))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.token").value("jwt.token.here"));
                }

                @Test
                @DisplayName("Happy Path – Google token hợp lệ, user mới → trả về info đăng ký")
                void loginWithGoogle_validToken_newUser_returns200WithRegistrationInfo() throws Exception {
                        // Arrange
                        Map<String, Object> serviceResult = Map.of(
                                        "email", "new@ou.edu.vn",
                                        "firstName", "New",
                                        "isNewUser", true);
                        when(authService.loginWithGoogle("google-id-token")).thenReturn(serviceResult);

                        String body = objectMapper.writeValueAsString(Map.of("token", "google-id-token"));

                        // Act & Assert
                        mockMvc.perform(post("/api/auth/google")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(body))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.isNewUser").value(true))
                                        .andExpect(jsonPath("$.email").value("new@ou.edu.vn"));
                }

                @Test
                @DisplayName("Exception Path – Token Google không hợp lệ → 400 Bad Request")
                void loginWithGoogle_invalidToken_returns400() throws Exception {
                        // Arrange
                        when(authService.loginWithGoogle(any()))
                                        .thenThrow(new IllegalArgumentException("Token không hợp lệ"));

                        String body = objectMapper.writeValueAsString(Map.of("token", "bad-token"));

                        // Act & Assert
                        mockMvc.perform(post("/api/auth/google")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(body))
                                        .andExpect(status().isBadRequest())
                                        .andExpect(jsonPath("$.status").value(400));
                }
        }

        // ========================================================================
        // /auth/login
        @Nested
        @DisplayName("POST /api/auth/login")
        class Login {

                @Test
                @DisplayName("Happy Path – email/password đúng → 200 OK với JWT token")
                void login_validCredentials_returns200WithToken() throws Exception {
                        // Arrange
                        UserLoginDTO dto = UserLoginDTO.builder()
                                        .email("student@ou.edu.vn")
                                        .password("correctPassword")
                                        .build();

                        when(webAppValidator.supports(any())).thenReturn(true);
                        when(authService.login(any(UserLoginDTO.class))).thenReturn("mocked.jwt.token");

                        // Act & Assert
                        mockMvc.perform(post("/api/auth/login")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(dto)))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.token").value("mocked.jwt.token"));
                }

                @Test
                @DisplayName("Validation Path – email sai định dạng → 400 Bad Request")
                void login_invalidEmailFormat_returns400() throws Exception {
                        // Arrange – email không đúng pattern @ou.edu.vn
                        UserLoginDTO dto = UserLoginDTO.builder()
                                        .email("invalid-email@gmail.com")
                                        .password("password123")
                                        .build();
                        
                        when(webAppValidator.supports(any())).thenReturn(true);

                        // Act & Assert
                        // Bean validation @Pattern trên UserLoginDTO chặn trước khi vào Controller
                        mockMvc.perform(post("/api/auth/login")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(dto)))
                                        .andExpect(status().isBadRequest());
                }

                @Test
                @DisplayName("Exception Path – email không tồn tại → 404 Not Found")
                void login_emailNotFound_returns404() throws Exception {
                        // Arrange
                        UserLoginDTO dto = UserLoginDTO.builder()
                                        .email("ghost@ou.edu.vn")
                                        .password("anyPassword")
                                        .build();

                        when(webAppValidator.supports(any())).thenReturn(true);
                        when(authService.login(any(UserLoginDTO.class)))
                                        .thenThrow(new ResourceNotFoundException("User not found"));

                        // Act & Assert
                        mockMvc.perform(post("/api/auth/login")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(dto)))
                                        .andExpect(status().isNotFound())
                                        .andExpect(jsonPath("$.status").value(404))
                                        .andExpect(jsonPath("$.message").value("User not found"));
                }

                @Test
                @DisplayName("Exception Path – sai password → 401 Unauthorized")
                void login_wrongPassword_returns401() throws Exception {
                        // Arrange
                        UserLoginDTO dto = UserLoginDTO.builder()
                                        .email("student@ou.edu.vn")
                                        .password("wrongPassword")
                                        .build();

                        when(webAppValidator.supports(any())).thenReturn(true);
                        when(authService.login(any(UserLoginDTO.class)))
                                        .thenThrow(new AuthenticationFailedException("Invalid password"));

                        // Act & Assert
                        mockMvc.perform(post("/api/auth/login")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(dto)))
                                        .andExpect(status().isUnauthorized())
                                        .andExpect(jsonPath("$.status").value(401));
                }
        }

        // ========================================================================
        // /auth/signup
        @Nested
        @DisplayName("POST /api/auth/signup")
        class Signup {

                @Test
                @DisplayName("Happy Path – multipart form hợp lệ → 201 Created với UserDetailsResponseDTO")
                void signup_validRequest_returns201WithUserDetails() throws Exception {
                        // Arrange
                        UUID userId = UUID.randomUUID();
                        UserDetailsResponseDTO responseDTO = UserDetailsResponseDTO.builder()
                                        .id(userId)
                                        .firstName("Minh")
                                        .lastName("Le")
                                        .email("minh@ou.edu.vn")
                                        .role("Student")
                                        .code("2051012345")
                                        .active(true)
                                        .gender(true)
                                        .createdDate(LocalDateTime.now())
                                        .updatedDate(LocalDateTime.now())
                                        .build();

                        User mockUser = User.builder()
                                        .id(userId)
                                        .firstName("Minh").lastName("Le")
                                        .email("minh@ou.edu.vn")
                                        .role(Role.ROLE_STUDENT).gender(true).active(true)
                                        .build();

                        when(webAppValidator.supports(any())).thenReturn(true);
                        when(userMapper.toEntity(any(com.lqm.user_service.dtos.UserRequestDTO.class)))
                                        .thenReturn(mockUser);
                        when(userService.saveUser(any(), any(), any())).thenReturn(mockUser);
                        when(userMapper.toUserDetailsResponseDTO(any())).thenReturn(responseDTO);

                        String jsonPart = objectMapper.writeValueAsString(
                                        com.lqm.user_service.dtos.UserRequestDTO.builder()
                                                        .firstName("Minh").lastName("Le")
                                                        .email("minh@ou.edu.vn").password("pass123")
                                                        .code("2051012345").gender(true).build());
                        MockMultipartFile jsonFile = new MockMultipartFile("data", "", MediaType.APPLICATION_JSON_VALUE, jsonPart.getBytes());

                        // Act & Assert
                        mockMvc.perform(multipart("/api/auth/signup")
                                        .file(jsonFile)
                                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
                                        .andExpect(status().isCreated());
                }
        }
}
