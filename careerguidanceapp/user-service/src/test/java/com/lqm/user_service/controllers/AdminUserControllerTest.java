package com.lqm.user_service.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lqm.user_service.dtos.AdminUserRequestDTO;
import com.lqm.user_service.dtos.UserDetailsResponseDTO;
import com.lqm.user_service.dtos.UserResponseDTO;
import com.lqm.user_service.exceptions.ResourceNotFoundException;
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
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Slice Test cho {@link AdminUserController}.
 *
 * ⚠️ Ghi chú Clean Code / RESTful:
 * 1. POST /api/internal/admin/users (saveUser) trả về void → không có response
 * body.
 * Chuẩn RESTful nên trả về 201 Created với body là resource vừa tạo/cập nhật.
 * Nếu là update thì 200 OK với body.
 * Ít nhất nên thêm @ResponseStatus(HttpStatus.CREATED) cho create.
 * 2. DELETE /api/internal/admin/users/{id} trả về void → nên trả về 204 No
 * Content.
 * Hiện tại mặc định 200 OK với body rỗng → không chuẩn.
 * 3. GET /api/internal/admin/users và /batch không có @ResponseStatus → mặc
 * định 200 OK.
 * Nên thêm ResponseEntity<Page<...>> cho type-safety.
 */
@WebMvcTest(controllers = AdminUserController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("AdminUserController Slice Tests")
class AdminUserControllerTest {

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
        @MockitoBean
        MessageSource messageSource;

        // ======================================================================== GET
        // /api/internal/admin/users
        @Nested
        @DisplayName("GET /api/internal/admin/users")
        class GetUsers {

                @Test
                @DisplayName("Happy Path – không có filter → 200 OK với danh sách users phân trang")
                void getUsers_noFilter_returns200WithPagedData() throws Exception {
                        // Arrange
                        UUID userId = UUID.randomUUID();
                        User user = User.builder()
                                        .id(userId).firstName("Minh").lastName("Le")
                                        .email("minh@ou.edu.vn").role(Role.ROLE_STUDENT)
                                        .gender(true).active(true).build();

                        UserResponseDTO responseDTO = UserResponseDTO.builder()
                                        .id(userId).firstName("Minh").lastName("Le").build();

                        Page<User> page = new PageImpl<>(List.of(user), PageRequest.of(0, 20), 1);

                        when(pageableUtil.getPageable(any(), any(int.class), any())).thenReturn(PageRequest.of(0, 20));
                        when(userService.getUsers(any(), any())).thenReturn(page);
                        when(userMapper.toUserResponseDTO(user)).thenReturn(responseDTO);

                        // Act & Assert
                        mockMvc.perform(get("/api/internal/admin/users"))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.content", hasSize(1)))
                                        .andExpect(jsonPath("$.content[0].firstName").value("Minh"));
                }

                @Test
                @DisplayName("Exception Path – tham số page không hợp lệ → 400 Bad Request")
                void getUsers_invalidPageParam_returns400() throws Exception {
                        // Arrange
                        when(pageableUtil.getPageable(any(), any(int.class), any()))
                                        .thenThrow(new com.lqm.user_service.exceptions.BadRequestException(
                                                        "Invalid page parameter"));

                        // Act & Assert
                        mockMvc.perform(get("/api/internal/admin/users").param("page", "-1"))
                                        .andExpect(status().isBadRequest())
                                        .andExpect(jsonPath("$.status").value(400));
                }
        }

        // ======================================================================== GET
        // /api/internal/admin/users/{id}/response
        @Nested
        @DisplayName("GET /api/internal/admin/users/{id}/response")
        class GetUserById {

                @Test
                @DisplayName("Happy Path – ID hợp lệ và tồn tại → 200 OK với UserResponseDTO")
                void getUserResponseById_existingId_returns200() throws Exception {
                        // Arrange
                        UUID userId = UUID.randomUUID();
                        User user = User.builder().id(userId).firstName("Minh").lastName("Le")
                                        .email("minh@ou.edu.vn").role(Role.ROLE_STUDENT).gender(true).active(true)
                                        .build();

                        UserResponseDTO dto = UserResponseDTO.builder()
                                        .id(userId).firstName("Minh").lastName("Le").code("2051012345").build();

                        when(userService.getUserById(eq(userId))).thenReturn(user);
                        when(userMapper.toUserResponseDTO(user)).thenReturn(dto);

                        // Act & Assert
                        mockMvc.perform(get("/api/internal/admin/users/{id}/response", userId))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.id").value(userId.toString()))
                                        .andExpect(jsonPath("$.firstName").value("Minh"))
                                        .andExpect(jsonPath("$.code").value("2051012345"));
                }

                @Test
                @DisplayName("Exception Path – ID không tồn tại → 404 Not Found")
                void getUserResponseById_notFound_returns404() throws Exception {
                        // Arrange
                        UUID randomId = UUID.randomUUID();
                        when(userService.getUserById(eq(randomId)))
                                        .thenThrow(new ResourceNotFoundException("User not found"));

                        // Act & Assert
                        mockMvc.perform(get("/api/internal/admin/users/{id}/response", randomId))
                                        .andExpect(status().isNotFound())
                                        .andExpect(jsonPath("$.status").value(404))
                                        .andExpect(jsonPath("$.message").value("User not found"));
                }
        }

        // ======================================================================== GET
        // /api/internal/admin/users/{id}/request
        @Nested
        @DisplayName("GET /api/internal/admin/users/{id}/request")
        class GetUserRequestById {

                @Test
                @DisplayName("Happy Path – ID hợp lệ → 200 OK với AdminUserRequestDTO")
                void getUserRequestById_existingId_returns200() throws Exception {
                        // Arrange
                        UUID userId = UUID.randomUUID();
                        User user = User.builder().id(userId).firstName("Minh").lastName("Le")
                                        .email("minh@ou.edu.vn").role(Role.ROLE_STUDENT).gender(true).active(true)
                                        .build();

                        AdminUserRequestDTO dto = AdminUserRequestDTO.builder()
                                        .id(userId).firstName("Minh").lastName("Le")
                                        .email("minh@ou.edu.vn").role("Student")
                                        .active(true).gender(true).build();

                        when(userService.getUserById(eq(userId))).thenReturn(user);
                        when(userMapper.toAdminUserRequestDTO(user)).thenReturn(dto);

                        // Act & Assert
                        mockMvc.perform(get("/api/internal/admin/users/{id}/request", userId))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.email").value("minh@ou.edu.vn"))
                                        .andExpect(jsonPath("$.role").value("Student"));
                }

                @Test
                @DisplayName("Exception Path – ID không tồn tại → 404 Not Found")
                void getUserRequestById_notFound_returns404() throws Exception {
                        // Arrange
                        UUID randomId = UUID.randomUUID();
                        when(userService.getUserById(eq(randomId)))
                                        .thenThrow(new ResourceNotFoundException("User not found"));

                        // Act & Assert
                        mockMvc.perform(get("/api/internal/admin/users/{id}/request", randomId))
                                        .andExpect(status().isNotFound());
                }
        }

        // ========================================================================
        // DELETE /api/internal/admin/users/{id}
        @Nested
        @DisplayName("DELETE /api/internal/admin/users/{id}")
        class DeleteUser {

                @Test
                @DisplayName("Happy Path – ID tồn tại → 200 OK (void)")
                void deleteUserById_existingId_returns200() throws Exception {
                        // Arrange
                        UUID userId = UUID.randomUUID();
                        doNothing().when(userService).deleteUser(eq(userId));

                        // Act & Assert
                        // ⚠️ Note: Hiện tại trả 200 vì void, nhưng chuẩn REST nên là 204 No Content.
                        mockMvc.perform(delete("/api/internal/admin/users/{id}", userId))
                                        .andExpect(status().isOk());

                        verify(userService).deleteUser(userId);
                }

                @Test
                @DisplayName("Exception Path – ID không tồn tại → 404 Not Found")
                void deleteUserById_notFound_returns404() throws Exception {
                        // Arrange
                        UUID randomId = UUID.randomUUID();
                        doThrow(new ResourceNotFoundException("User not found"))
                                        .when(userService).deleteUser(eq(randomId));

                        // Act & Assert
                        mockMvc.perform(delete("/api/internal/admin/users/{id}", randomId))
                                        .andExpect(status().isNotFound())
                                        .andExpect(jsonPath("$.status").value(404));
                }
        }

        // ======================================================================== GET
        // /api/internal/admin/users/stats
        @Nested
        @DisplayName("GET /api/internal/admin/users/stats")
        class GetStats {

                @Test
                @DisplayName("Happy Path – trả về 200 OK với map thống kê")
                void getStats_returns200WithStatisticsMap() throws Exception {
                        // Arrange
                        Map<String, Object> stats = Map.of(
                                        "totalUsers", 100L,
                                        "byRole", Map.of("Student", 80, "Teacher", 20));
                        when(userService.getUserStatistics()).thenReturn(stats);

                        // Act & Assert
                        mockMvc.perform(get("/api/internal/admin/users/stats"))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.totalUsers").value(100));
                }
        }

        // ======================================================================== GET
        // /api/internal/admin/users/me
        @Nested
        @DisplayName("GET /api/internal/admin/users/me")
        class GetCurrentUser {

                @Test
                @DisplayName("Happy Path – admin đã đăng nhập → 200 OK với UserDetailsResponseDTO")
                void getCurrentUser_authenticated_returns200() throws Exception {
                        // Arrange
                        UUID userId = UUID.randomUUID();
                        User user = User.builder()
                                        .id(userId).firstName("Admin").lastName("User")
                                        .email("admin@ou.edu.vn").role(Role.ROLE_ADMIN)
                                        .gender(true).active(true).build();

                        UserDetailsResponseDTO dto = UserDetailsResponseDTO.builder()
                                        .id(userId).firstName("Admin").lastName("User")
                                        .email("admin@ou.edu.vn").role("Admin")
                                        .active(true).gender(true)
                                        .createdDate(LocalDateTime.now()).updatedDate(LocalDateTime.now())
                                        .build();

                        when(userService.getCurrentUser()).thenReturn(user);
                        when(userMapper.toUserDetailsResponseDTO(user)).thenReturn(dto);

                        // Act & Assert
                        mockMvc.perform(get("/api/internal/admin/users/me"))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.email").value("admin@ou.edu.vn"))
                                        .andExpect(jsonPath("$.role").value("Admin"));
                }

                @Test
                @DisplayName("Exception Path – chưa đăng nhập → 401 Unauthorized")
                void getCurrentUser_notAuthenticated_returns401() throws Exception {
                        // Arrange
                        when(userService.getCurrentUser())
                                        .thenThrow(new com.lqm.user_service.exceptions.UnauthorizedException(
                                                        "Not logged in"));

                        // Act & Assert
                        mockMvc.perform(get("/api/internal/admin/users/me"))
                                        .andExpect(status().isUnauthorized())
                                        .andExpect(jsonPath("$.status").value(401));
                }
        }

        // ======================================================================== POST
        // /api/internal/admin/users/batch
        @Nested
        @DisplayName("POST /api/internal/admin/users/batch")
        class GetUsersByIds {

                @Test
                @DisplayName("Happy Path – list ID hợp lệ → 200 OK với Page<UserResponseDTO>")
                void getUsersByIds_validIds_returns200WithPagedData() throws Exception {
                        // Arrange
                        UUID id1 = UUID.randomUUID();
                        UUID id2 = UUID.randomUUID();

                        User user = User.builder()
                                        .id(id1).firstName("Minh").lastName("Le")
                                        .email("minh@ou.edu.vn").role(Role.ROLE_STUDENT)
                                        .gender(true).active(true).build();

                        UserResponseDTO responseDTO = UserResponseDTO.builder()
                                        .id(id1).firstName("Minh").lastName("Le").build();

                        Page<User> page = new PageImpl<>(List.of(user), PageRequest.of(0, 20), 1);

                        when(pageableUtil.getPageable(any(), any(int.class), any())).thenReturn(PageRequest.of(0, 20));
                        when(userService.getUsersByIds(any(), any(), any())).thenReturn(page);
                        when(userMapper.toUserResponseDTO(user)).thenReturn(responseDTO);
                        when(webAppValidator.supports(any())).thenReturn(true);

                        String body = objectMapper.writeValueAsString(List.of(id1, id2));

                        // Act & Assert
                        mockMvc.perform(post("/api/internal/admin/users/batch")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(body))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.content", hasSize(1)))
                                        .andExpect(jsonPath("$.content[0].firstName").value("Minh"));
                }

                @Test
                @DisplayName("Happy Path – list rỗng → 200 OK với page trống")
                void getUsersByIds_emptyList_returns200WithEmptyPage() throws Exception {
                        // Arrange
                        when(pageableUtil.getPageable(any(), any(int.class), any())).thenReturn(PageRequest.of(0, 20));
                        when(userService.getUsersByIds(any(), any(), any())).thenReturn(Page.empty());
                        when(webAppValidator.supports(any())).thenReturn(true);

                        // Act & Assert
                        mockMvc.perform(post("/api/internal/admin/users/batch")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content("[]"))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.content", hasSize(0)));
                }
        }
}
