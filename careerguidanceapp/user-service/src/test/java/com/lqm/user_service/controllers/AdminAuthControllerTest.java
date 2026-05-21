package com.lqm.user_service.controllers;

import com.lqm.user_service.exceptions.ResourceNotFoundException;
import com.lqm.user_service.services.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;

import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AdminAuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("AdminAuthController Slice Tests")
class AdminAuthControllerTest {

        @Autowired
        MockMvc mockMvc;

        @MockitoBean
        UserService userService;

        // ======================================================================== GET
        // /api/internal/auth/{email}
        @Nested
        @DisplayName("GET /api/internal/auth/{email}")
        class GetUserForAuth {

                @Test
                @DisplayName("Happy Path – email tồn tại → 200 OK với AdminUserLoginDTO")
                void getUserForAuth_existingEmail_returns200WithAuthInfo() throws Exception {
                        // Arrange
                        UserDetails mockUserDetails = org.springframework.security.core.userdetails.User
                                        .withUsername("student@ou.edu.vn")
                                        .password("encoded_password")
                                        .authorities(List.of(new SimpleGrantedAuthority("ROLE_STUDENT")))
                                        .build();

                        when(userService.loadUserByUsername(eq("student@ou.edu.vn")))
                                        .thenReturn(mockUserDetails);

                        // Act & Assert
                        mockMvc.perform(get("/api/internal/auth/student@ou.edu.vn")
                                        .accept(MediaType.APPLICATION_JSON))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.email").value("student@ou.edu.vn"))
                                        .andExpect(jsonPath("$.password").value("encoded_password"))
                                        .andExpect(jsonPath("$.role").value("STUDENT"));
                }

                @Test
                @DisplayName("Happy Path – user là ADMIN → role trả về 'ADMIN' (không có tiền tố ROLE_)")
                void getUserForAuth_adminUser_returnsRoleWithoutPrefix() throws Exception {
                        // Arrange
                        UserDetails mockUserDetails = org.springframework.security.core.userdetails.User
                                        .withUsername("admin@ou.edu.vn")
                                        .password("admin_encoded")
                                        .authorities(List.of(new SimpleGrantedAuthority("ROLE_ADMIN")))
                                        .build();

                        when(userService.loadUserByUsername(eq("admin@ou.edu.vn")))
                                        .thenReturn(mockUserDetails);

                        // Act & Assert
                        mockMvc.perform(get("/api/internal/auth/admin@ou.edu.vn")
                                        .accept(MediaType.APPLICATION_JSON))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.role").value("ADMIN"));
                }

                @Test
                @DisplayName("Exception Path – email không tồn tại → 404 Not Found")
                void getUserForAuth_emailNotFound_returns404() throws Exception {
                        // Arrange
                        when(userService.loadUserByUsername(eq("ghost@ou.edu.vn")))
                                        .thenThrow(new ResourceNotFoundException("User not found"));

                        // Act & Assert
                        mockMvc.perform(get("/api/internal/auth/ghost@ou.edu.vn"))
                                        .andExpect(status().isNotFound())
                                        .andExpect(jsonPath("$.status").value(404))
                                        .andExpect(jsonPath("$.message").value("User not found"));
                }
        }
}
