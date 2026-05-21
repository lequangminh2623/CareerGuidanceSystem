package com.lqm.user_service.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lqm.user_service.dtos.UserDetailsResponseDTO;
import com.lqm.user_service.dtos.UserMessageResponseDTO;
import com.lqm.user_service.exceptions.ResourceNotFoundException;
import com.lqm.user_service.exceptions.UnauthorizedException;
import com.lqm.user_service.mappers.UserMapper;
import com.lqm.user_service.models.Role;
import com.lqm.user_service.models.User;
import com.lqm.user_service.services.UserService;
import com.lqm.user_service.utils.PageableUtil;
import com.lqm.user_service.validators.WebAppValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Slice Test cho {@link ApiUserController}.
 *
 * ⚠️ Ghi chú Clean Code / RESTful:
 * 1. GET /api/secure/users nên đặt tên endpoint là /users thay vì /users để
 * đồng nhất
 * (hiện tại đã là /users – OK).
 * 2. Không có GET /users/{id} – cần cân nhắc thêm endpoint này để RESTful hơn
 * (hiện tại chỉ có qua Admin path).
 * 3. Các method không có @ResponseStatus annotation → mặc định trả về 200, đây
 * là behavior đúng.
 */
@WebMvcTest(controllers = ApiUserController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("ApiUserController Slice Tests")
class ApiUserControllerTest {

        @Autowired
        MockMvc mockMvc;
        @Autowired
        ObjectMapper objectMapper;

        @MockitoBean
        UserService userService;
        @MockitoBean
        UserMapper userMapper;
        @MockitoBean
        WebAppValidator webAppValidator;
        @MockitoBean
        PageableUtil pageableUtil;
        // MessageSource cần thiết cho PageableUtil → phải mock
        @MockitoBean
        MessageSource messageSource;

        // ======================================================================== GET
        // /api/secure/me
        @Nested
        @DisplayName("GET /api/secure/me")
        class GetProfile {

                @Test
                @DisplayName("Happy Path – user đã đăng nhập → 200 OK với UserDetailsResponseDTO")
                void getProfile_authenticated_returns200WithProfileData() throws Exception {
                        // Arrange
                        UUID userId = UUID.randomUUID();
                        User user = User.builder()
                                        .id(userId).firstName("Minh").lastName("Le")
                                        .email("minh@ou.edu.vn").role(Role.ROLE_STUDENT)
                                        .gender(true).active(true).build();

                        UserDetailsResponseDTO dto = UserDetailsResponseDTO.builder()
                                        .id(userId).firstName("Minh").lastName("Le")
                                        .email("minh@ou.edu.vn").role("Student")
                                        .active(true).gender(true)
                                        .createdDate(LocalDateTime.of(2024, 1, 1, 0, 0))
                                        .updatedDate(LocalDateTime.of(2024, 1, 1, 0, 0))
                                        .build();

                        when(userService.getCurrentUser()).thenReturn(user);
                        when(userMapper.toUserDetailsResponseDTO(user)).thenReturn(dto);

                        // Act & Assert
                        mockMvc.perform(get("/api/secure/me")
                                        .contentType(MediaType.APPLICATION_JSON))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.email").value("minh@ou.edu.vn"))
                                        .andExpect(jsonPath("$.firstName").value("Minh"))
                                        .andExpect(jsonPath("$.role").value("Student"));
                }

                @Test
                @DisplayName("Exception Path – chưa đăng nhập → 401 Unauthorized")
                void getProfile_notAuthenticated_returns401() throws Exception {
                        // Arrange
                        when(userService.getCurrentUser())
                                        .thenThrow(new UnauthorizedException("Not logged in"));

                        // Act & Assert
                        mockMvc.perform(get("/api/secure/me"))
                                        .andExpect(status().isUnauthorized())
                                        .andExpect(jsonPath("$.status").value(401));
                }

                @Test
                @DisplayName("Exception Path – token hết hạn nhưng email không còn trong DB → 404 Not Found")
                void getProfile_userDeletedAfterLogin_returns404() throws Exception {
                        // Arrange
                        when(userService.getCurrentUser())
                                        .thenThrow(new ResourceNotFoundException("User not found"));

                        // Act & Assert
                        mockMvc.perform(get("/api/secure/me"))
                                        .andExpect(status().isNotFound())
                                        .andExpect(jsonPath("$.status").value(404));
                }
        }

        // ======================================================================== GET
        // /api/secure/users
        @Nested
        @DisplayName("GET /api/secure/users")
        class GetUsers {

                @Test
                @DisplayName("Happy Path – không có filter → 200 OK với danh sách users phân trang")
                void getUsers_noFilter_returns200WithPagedUsers() throws Exception {
                        // Arrange
                        UserMessageResponseDTO userDTO = UserMessageResponseDTO.builder()
                                        .firstName("Minh").lastName("Le")
                                        .email("minh@ou.edu.vn").role("Student")
                                        .avatar("https://avatar.url").build();

                        User user = User.builder()
                                        .firstName("Minh").lastName("Le")
                                        .email("minh@ou.edu.vn").role(Role.ROLE_STUDENT)
                                        .gender(true).active(true).build();

                        Page<User> userPage = new PageImpl<>(List.of(user), PageRequest.of(0, 20), 1);

                        when(pageableUtil.getPageable(any(), any(int.class), any())).thenReturn(PageRequest.of(0, 20));
                        when(userService.getUsers(any(), any())).thenReturn(userPage);
                        when(userMapper.toUserMessageResponseDTO(user)).thenReturn(userDTO);

                        // Act & Assert
                        mockMvc.perform(get("/api/secure/users")
                                        .contentType(MediaType.APPLICATION_JSON))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.content").isArray())
                                        .andExpect(jsonPath("$.content.length()").value(1))
                                        .andExpect(jsonPath("$.content[0].email").value("minh@ou.edu.vn"));
                }

                @Test
                @DisplayName("Happy Path – filter theo lastName → 200 OK với kết quả lọc")
                void getUsers_withLastNameFilter_returns200WithFilteredUsers() throws Exception {
                        // Arrange
                        Page<User> emptyPage = Page.empty();
                        when(pageableUtil.getPageable(any(), any(int.class), any())).thenReturn(PageRequest.of(0, 20));
                        when(userService.getUsers(any(), any())).thenReturn(emptyPage);

                        // Act & Assert
                        mockMvc.perform(get("/api/secure/users")
                                        .param("lastName", "Nguyen"))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.content").isArray());
                }

                @Test
                @DisplayName("Exception Path – trang không hợp lệ (page=0) → 400 Bad Request")
                void getUsers_invalidPageNumber_returns400() throws Exception {
                        // Arrange
                        when(pageableUtil.getPageable(any(), any(int.class), any()))
                                        .thenThrow(new com.lqm.user_service.exceptions.BadRequestException(
                                                        "Invalid page"));

                        // Act & Assert
                        mockMvc.perform(get("/api/secure/users").param("page", "0"))
                                        .andExpect(status().isBadRequest())
                                        .andExpect(jsonPath("$.status").value(400));
                }
        }
}
