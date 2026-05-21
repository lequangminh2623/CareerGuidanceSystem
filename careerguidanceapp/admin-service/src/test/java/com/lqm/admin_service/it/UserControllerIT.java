package com.lqm.admin_service.it;

import com.lqm.admin_service.clients.*;
import com.lqm.admin_service.dtos.UserDetailsResponseDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("UserControllerIT — Integration Tests")
class UserControllerIT {

        @Autowired
        private MockMvc mockMvc;

        @MockitoBean
        private UserClient userClient;
        @MockitoBean
        private AuthClient authClient;
        @MockitoBean
        private DeviceClient deviceClient;
        @MockitoBean
        private SectionClient sectionClient;
        @MockitoBean
        private ClassroomClient classroomClient;
        @MockitoBean
        private SubjectClient subjectClient;
        @MockitoBean
        private FingerprintClient fingerprintClient;
        @MockitoBean
        private AttendanceClient attendanceClient;
        @MockitoBean
        private SemesterClient semesterClient;
        @MockitoBean
        private CurriculumClient curriculumClient;
        @MockitoBean
        private GradeClient gradeClient;
        @MockitoBean
        private TranscriptClient transcriptClient;
        @MockitoBean
        private YearClient yearClient;

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("GET /users — Lấy danh sách người dùng thành công")
        void getUsers_Success() throws Exception {
                UserDetailsResponseDTO userDto = UserDetailsResponseDTO.builder()
                                .id(UUID.randomUUID())
                                .firstName("Nguyen")
                                .lastName("Van A")
                                .email("nva@ou.edu.vn")
                                .role("STUDENT")
                                .build();

                when(userClient.getUsersDetails(ArgumentMatchers.<Map<String, String>>any()))
                                .thenReturn(new PageImpl<>(List.of(userDto)));

                mockMvc.perform(get("/users"))
                                .andExpect(status().isOk())
                                .andExpect(view().name("user/list"))
                                .andExpect(model().attributeExists("users", "params"));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("POST /users/save — Lưu người dùng thành công")
        void saveUser_Success() throws Exception {
                doNothing().when(userClient).saveUser(any(), any());

                mockMvc.perform(multipart("/users/save")
                                .param("firstName", "Nguyen")
                                .param("lastName", "Van A")
                                .param("email", "nva@ou.edu.vn")
                                .param("role", "Student")
                                .param("gender", "true")
                                .param("active", "true")
                                .param("code", "2151010001") // Code must be 10 characters
                                .with(csrf()))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrl("/users"));

                verify(userClient, times(1)).saveUser(any(), any());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("POST /users/save — Validation bị lỗi")
        void saveUser_ValidationFailed() throws Exception {
                mockMvc.perform(multipart("/users/save")
                                .param("firstName", "") // Missing first name to trigger validation
                                .param("lastName", "Van A")
                                .param("email", "invalid-email") // Invalid email
                                .param("role", "STUDENT")
                                .with(csrf()))
                                .andExpect(status().isOk())
                                .andExpect(view().name("user/form"))
                                .andExpect(model().attributeHasFieldErrors("user", "firstName", "email"));

                verify(userClient, never()).saveUser(any(), any());
        }
}
