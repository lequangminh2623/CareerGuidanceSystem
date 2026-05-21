package com.lqm.score_service.controllers;

import com.lqm.score_service.clients.UserClient;
import com.lqm.score_service.dtos.*;
import com.lqm.score_service.services.StatisticsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.MessageSource;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ApiStatisticsController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("ApiStatisticsController Web MVC Slice Tests")
class ApiStatisticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserClient userClient;
    @MockitoBean
    private StatisticsService statisticsService;
    @MockitoBean
    private MessageSource messageSource;

    private static final String BASE_URL = "/api/secure/statistics";
    private UUID currentUserId;

    @BeforeEach
    void setUp() {
        currentUserId = UUID.randomUUID();
        given(messageSource.getMessage(anyString(), any(), any(Locale.class)))
                .willAnswer(inv -> inv.getArgument(0));
    }

    @Nested
    @DisplayName("GET /student")
    class GetStudentStatisticsTests {
        @Test
        @DisplayName("Happy Path: returns 200 with student statistics")
        void getStudentStatistics_HappyPath_Returns200() throws Exception {
            // Arrange
            UserResponseDTO currentUser = new UserResponseDTO(currentUserId, "code", "firstName", "lastName");
            given(userClient.getCurrentUser()).willReturn(currentUser);

            StudentStatisticsResponseDTO dto = new StudentStatisticsResponseDTO(Collections.emptyList(),
                    Collections.emptyList());
            given(statisticsService.getStudentStatistics(currentUserId)).willReturn(dto);

            // Act & Assert
            mockMvc.perform(get(BASE_URL + "/student")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Exception Path: User has no student code -> Returns 403")
        void getStudentStatistics_UserHasNoCode_Returns403() throws Exception {
            // Arrange
            UserResponseDTO currentUser = new UserResponseDTO(currentUserId, null, "firstName", "lastName");
            given(userClient.getCurrentUser()).willReturn(currentUser);

            // Act & Assert
            mockMvc.perform(get(BASE_URL + "/student")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("GET /teacher/sections")
    class GetTeacherSectionStatisticsTests {
        @Test
        @DisplayName("Happy Path: returns 200 with teacher section stats")
        void getTeacherSectionStatistics_HappyPath_Returns200() throws Exception {
            // Arrange
            UserResponseDTO currentUser = new UserResponseDTO(currentUserId, null, "firstName", "lastName");
            given(userClient.getCurrentUser()).willReturn(currentUser);

            TeacherSectionAvgDTO dto = new TeacherSectionAvgDTO("label", 8.5);
            given(statisticsService.getTeacherSectionStatistics(currentUserId, null)).willReturn(List.of(dto));

            // Act & Assert
            mockMvc.perform(get(BASE_URL + "/teacher/sections")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].avgScore").value(8.5));
        }
    }

    @Nested
    @DisplayName("GET /teacher/grades")
    class GetTeacherGradeStatisticsTests {
        @Test
        @DisplayName("Happy Path: returns 200 with teacher grade stats")
        void getTeacherGradeStatistics_HappyPath_Returns200() throws Exception {
            // Arrange
            UserResponseDTO currentUser = new UserResponseDTO(currentUserId, null, "firstName", "lastName");
            given(userClient.getCurrentUser()).willReturn(currentUser);

            TeacherGradeStatisticsDTO dto = new TeacherGradeStatisticsDTO("Grade 10", Collections.emptyList());
            given(statisticsService.getTeacherGradeStatistics(currentUserId, null)).willReturn(List.of(dto));

            // Act & Assert
            mockMvc.perform(get(BASE_URL + "/teacher/grades")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].gradeName").value("Grade 10"));
        }
    }

    @Nested
    @DisplayName("GET /subjects")
    class GetAllSubjectsTests {
        @Test
        @DisplayName("Happy Path: returns 200 with list of subjects")
        void getAllSubjects_HappyPath_Returns200() throws Exception {
            // Arrange
            SubjectResponseDTO dto = new SubjectResponseDTO(UUID.randomUUID(), "Toán");
            given(statisticsService.getAllSubjects()).willReturn(List.of(dto));

            // Act & Assert
            mockMvc.perform(get(BASE_URL + "/subjects")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].name").value("Toán"));
        }
    }
}
