package com.lqm.academic_service.controllers;

import com.lqm.academic_service.clients.UserClient;
import com.lqm.academic_service.dtos.AcademicResponseDTO;
import com.lqm.academic_service.dtos.UserResponseDTO;
import com.lqm.academic_service.mappers.ClassroomMapper;
import com.lqm.academic_service.services.StudentClassroomService;
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

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Web Slice Test for {@link ApiClassroomController}.
 */
@WebMvcTest(ApiClassroomController.class)
@AutoConfigureMockMvc(addFilters = false)
class ApiClassroomControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StudentClassroomService studentClassroomService;
    @MockitoBean
    private ClassroomMapper classroomMapper;
    @MockitoBean
    private UserClient userClient;
    @MockitoBean
    private MessageSource messageSource;

    private static final String BASE_URL = "/api/secure/classrooms";

    @Nested
    @DisplayName("GET /me")
    class GetStudentClassroomsTests {

        @Test
        @DisplayName("Happy Path: returns list of classrooms for current student")
        void getStudentClassrooms_HappyPath_Returns200WithList() throws Exception {
            // Arrange
            UUID studentId = UUID.randomUUID();
            UserResponseDTO currentUser = UserResponseDTO.builder().id(studentId).build();
            AcademicResponseDTO responseDTO = new AcademicResponseDTO(UUID.randomUUID(), "10A1");

            given(userClient.getCurrentUser()).willReturn(currentUser);
            given(studentClassroomService.getClassroomsByStudentId(studentId))
                    .willReturn(List.of(new com.lqm.academic_service.models.Classroom()));
            given(classroomMapper.toClassroomDetailNameDTO(org.mockito.ArgumentMatchers.any())).willReturn(responseDTO);

            // Act & Assert
            mockMvc.perform(get(BASE_URL + "/me"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].id").value(responseDTO.id().toString()))
                    .andExpect(jsonPath("$[0].name").value("10A1"));
        }
    }
}
