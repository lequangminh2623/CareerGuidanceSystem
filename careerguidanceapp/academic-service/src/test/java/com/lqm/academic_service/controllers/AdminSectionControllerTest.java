package com.lqm.academic_service.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lqm.academic_service.dtos.SectionRequestDTO;
import com.lqm.academic_service.dtos.SectionResponseDTO;
import com.lqm.academic_service.exceptions.ResourceNotFoundException;
import com.lqm.academic_service.mappers.SectionMapper;
import com.lqm.academic_service.models.Section;
import com.lqm.academic_service.models.ScoreStatusType;
import com.lqm.academic_service.services.SectionService;
import com.lqm.academic_service.utils.PageableUtil;
import com.lqm.academic_service.validators.WebAppValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Web Slice Test for {@link AdminSectionController}.
 *
 * Uses Spring Boot 4 standard imports:
 * - {@code @WebMvcTest} from
 * {@code org.springframework.boot.webmvc.test.autoconfigure}
 * - {@code @AutoConfigureMockMvc(addFilters = false)} to disable Security
 * filter chain
 * - {@code @MockitoBean} (replaces deprecated @MockBean in SB4)
 *
 * Three test paths per endpoint:
 * 1. Happy Path — correct HTTP status + JSON response
 * 2. Validation — 400 Bad Request on invalid input
 * 3. Exception Path — @RestControllerAdvice maps exceptions to proper HTTP
 * statuses
 */
@WebMvcTest(AdminSectionController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminSectionControllerTest {

        @Autowired
        private MockMvc mockMvc;
        @Autowired
        private ObjectMapper objectMapper;

        @MockitoBean
        private SectionService sectionService;
        @MockitoBean
        private SectionMapper sectionMapper;
        @MockitoBean
        private PageableUtil pageableUtil;
        @MockitoBean
        private WebAppValidator webAppValidator;
        @MockitoBean
        private MessageSource messageSource;

        // Base URL
        private static final String BASE_URL = "/api/internal/admin/sections";

        private UUID sectionId;
        private SectionResponseDTO sectionResponseDTO;

        @BeforeEach
        void setUp() {
                sectionId = UUID.randomUUID();

                sectionResponseDTO = new SectionResponseDTO(
                                sectionId,
                                UUID.randomUUID(),
                                "Nguyen Van A",
                                "10A1",
                                "Grade 10",
                                "2024-2025",
                                "First semester",
                                "Toán",
                                "Draft");

                // Default pageable stub
                given(pageableUtil.getPageable(any(), anyInt(), anyList()))
                                .willReturn(Pageable.ofSize(20));

                // WebAppValidator: by default no validation errors (happy path)
                willDoNothing().given(webAppValidator).validate(any(), any());
                given(webAppValidator.supports(any())).willReturn(true);
        }

        // ═══════════════════════════════════════════════════════════════════════════
        // GET /{id}
        // ═══════════════════════════════════════════════════════════════════════════
        @Nested
        @DisplayName("GET /{id}")
        class GetSectionByIdTests {

                @Test
                @DisplayName("Happy Path: returns 200 and section JSON when found")
                void getSectionById_WhenFound_Returns200WithBody() throws Exception {
                        // Arrange
                        Section section = Section.builder().id(sectionId).scoreStatus(ScoreStatusType.DRAFT).build();
                        given(sectionService.getSectionById(sectionId)).willReturn(section);
                        given(sectionService.buildTeacherMap(List.of(section))).willReturn(Map.of());
                        given(sectionMapper.toSectionResponseDTO(eq(section), anyMap())).willReturn(sectionResponseDTO);

                        // Act & Assert
                        mockMvc.perform(get(BASE_URL + "/{id}", sectionId))
                                        .andExpect(status().isOk())
                                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                                        .andExpect(jsonPath("$.id").value(sectionId.toString()))
                                        .andExpect(jsonPath("$.subjectName").value("Toán"))
                                        .andExpect(jsonPath("$.scoreStatus").value("Draft"));
                }

                @Test
                @DisplayName("Exception Path: returns 404 when section not found")
                void getSectionById_WhenNotFound_Returns404() throws Exception {
                        // Arrange
                        given(sectionService.getSectionById(sectionId))
                                        .willThrow(new ResourceNotFoundException("section.notFound"));

                        // Act & Assert
                        mockMvc.perform(get(BASE_URL + "/{id}", sectionId))
                                        .andExpect(status().isNotFound())
                                        .andExpect(jsonPath("$.status").value(404))
                                        .andExpect(jsonPath("$.message").value("section.notFound"));
                }
        }

        // ═══════════════════════════════════════════════════════════════════════════
        // DELETE /{id}
        // ═══════════════════════════════════════════════════════════════════════════
        @Nested
        @DisplayName("DELETE /{id}")
        class DeleteSectionTests {

                @Test
                @DisplayName("Happy Path: returns 204 No Content on successful delete")
                void deleteSection_WhenSuccessful_Returns204() throws Exception {
                        // Arrange
                        willDoNothing().given(sectionService).deleteSection(sectionId);

                        // Act & Assert
                        mockMvc.perform(delete(BASE_URL + "/{id}", sectionId))
                                        .andExpect(status().isNoContent());
                }

                @Test
                @DisplayName("Exception Path: returns 404 when section not found")
                void deleteSection_WhenNotFound_Returns404() throws Exception {
                        // Arrange
                        willThrow(new ResourceNotFoundException("section.notFound"))
                                        .given(sectionService).deleteSection(sectionId);

                        // Act & Assert
                        mockMvc.perform(delete(BASE_URL + "/{id}", sectionId))
                                        .andExpect(status().isNotFound());
                }
        }

        // ═══════════════════════════════════════════════════════════════════════════
        // PATCH /{id}/change-status
        // ═══════════════════════════════════════════════════════════════════════════
        @Nested
        @DisplayName("PATCH /{id}/change-status")
        class ChangeStatusTests {

                @Test
                @DisplayName("Happy Path: returns 200 with valid status")
                void changeStatus_WithValidStatus_Returns200() throws Exception {
                        // Arrange
                        String body = "{\"status\":\"Draft\"}";
                        willDoNothing().given(sectionService).changeScoreStatus(eq(sectionId),
                                        any(ScoreStatusType.class));

                        // Act & Assert
                        mockMvc.perform(patch(BASE_URL + "/{id}/change-status", sectionId)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(body))
                                        .andExpect(status().isOk());
                }

                @Test
                @DisplayName("Validation Path: returns 400 when status field is null")
                void changeStatus_WithNullStatus_Returns400() throws Exception {
                        // Arrange — send body without 'status' field to trigger @NotNull
                        String body = "{}";

                        // Act & Assert
                        mockMvc.perform(patch(BASE_URL + "/{id}/change-status", sectionId)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(body))
                                        .andExpect(status().isBadRequest());
                }
        }

        // ═══════════════════════════════════════════════════════════════════════════
        // POST /single
        // ═══════════════════════════════════════════════════════════════════════════
        @Nested
        @DisplayName("POST /single")
        class SaveSingleSectionTests {

                @Test
                @DisplayName("Happy Path: returns 200 with SectionResponseDTO")
                void saveSingleSection_WithValidBody_Returns200() throws Exception {
                        // Arrange
                        UUID classroomId = UUID.randomUUID();
                        UUID curriculumId = UUID.randomUUID();
                        SectionRequestDTO dto = SectionRequestDTO.builder()
                                        .classroomId(classroomId)
                                        .curriculumId(curriculumId)
                                        .build();

                        Section section = Section.builder().id(sectionId).build();
                        given(sectionMapper.toEntity(any(SectionRequestDTO.class))).willReturn(section);
                        given(sectionService.saveSingleSection(eq(section), eq(classroomId), eq(curriculumId)))
                                        .willReturn(section);
                        given(sectionService.buildTeacherMap(List.of(section))).willReturn(Map.of());
                        given(sectionMapper.toSectionResponseDTO(eq(section), anyMap())).willReturn(sectionResponseDTO);

                        // Act & Assert
                        mockMvc.perform(post(BASE_URL + "/single")
                                        .param("classroomId", classroomId.toString())
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(dto)))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.id").value(sectionId.toString()));
                }

                @Test
                @DisplayName("Validation Path: returns 400 when curriculumId is null")
                void saveSingleSection_WithMissingCurriculumId_Returns400() throws Exception {
                        // Arrange — send DTO with null curriculumId (violates @NotNull)
                        SectionRequestDTO dto = SectionRequestDTO.builder()
                                        .classroomId(UUID.randomUUID())
                                        .curriculumId(null)
                                        .build();

                        // Act & Assert
                        mockMvc.perform(post(BASE_URL + "/single")
                                        .param("classroomId", UUID.randomUUID().toString())
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(dto)))
                                        .andExpect(status().isBadRequest());
                }
        }
}
