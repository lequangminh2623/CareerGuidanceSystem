package com.lqm.academic_service.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lqm.academic_service.clients.UserClient;
import com.lqm.academic_service.dtos.ClassroomRequestDTO;
import com.lqm.academic_service.dtos.ClassroomResponseDTO;
import com.lqm.academic_service.exceptions.BadRequestException;
import com.lqm.academic_service.exceptions.ResourceNotFoundException;
import com.lqm.academic_service.mappers.ClassroomMapper;
import com.lqm.academic_service.models.Classroom;
import com.lqm.academic_service.services.ClassroomService;
import com.lqm.academic_service.services.StudentClassroomService;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Web Slice Test for {@link AdminClassroomController}.
 *
 * Spring Boot 4 imports:
 * - {@code @WebMvcTest} from
 * {@code org.springframework.boot.webmvc.test.autoconfigure}
 * - {@code @AutoConfigureMockMvc(addFilters = false)} to disable Security
 * filter chain
 * - {@code @MockitoBean} (SB4 replacement for deprecated @MockBean)
 */
@WebMvcTest(AdminClassroomController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminClassroomControllerTest {

        @Autowired
        private MockMvc mockMvc;
        @Autowired
        private ObjectMapper objectMapper;

        @MockitoBean
        private ClassroomService classroomService;
        @MockitoBean
        private ClassroomMapper classroomMapper;
        @MockitoBean
        private PageableUtil pageableUtil;
        @MockitoBean
        private WebAppValidator webAppValidator;
        @MockitoBean
        private UserClient userClient;
        @MockitoBean
        private StudentClassroomService studentClassroomService;
        @MockitoBean
        private MessageSource messageSource;

        private static final String BASE_URL = "/api/internal/admin/classrooms";

        private UUID classroomId;
        private UUID gradeId;
        private ClassroomResponseDTO classroomResponseDTO;

        @BeforeEach
        void setUp() {
                classroomId = UUID.randomUUID();
                gradeId = UUID.randomUUID();

                classroomResponseDTO = new ClassroomResponseDTO(classroomId, "10A1", 30);

                given(pageableUtil.getPageable(any(), anyInt(), anyList()))
                                .willReturn(Pageable.ofSize(20));

                // Default WebAppValidator: no errors (happy path)
                willDoNothing().given(webAppValidator).validate(any(), any());
                given(webAppValidator.supports(any())).willReturn(true);
        }

        // ═══════════════════════════════════════════════════════════════════════════
        // GET /
        // ═══════════════════════════════════════════════════════════════════════════
        @Nested
        @DisplayName("GET /")
        class GetClassroomsTests {

                @Test
                @DisplayName("Happy Path: returns 200 with paginated classroom list")
                void getClassrooms_HappyPath_Returns200WithList() throws Exception {
                        // Arrange
                        Classroom classroom = Classroom.builder().id(classroomId).name("10A1").build();
                        given(classroomService.getClassrooms(anyMap(), any()))
                                        .willReturn(new PageImpl<>(List.of(classroom)));
                        given(classroomMapper.toClassroomResponseDTO(classroom)).willReturn(classroomResponseDTO);

                        // Act & Assert
                        mockMvc.perform(get(BASE_URL))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.content", hasSize(1)))
                                        .andExpect(jsonPath("$.content[0].id").value(classroomId.toString()))
                                        .andExpect(jsonPath("$.content[0].name").value("10A1"));
                }

                @Test
                @DisplayName("Happy Path: returns 200 with empty list when no classrooms exist")
                void getClassrooms_EmptyPage_Returns200WithEmptyList() throws Exception {
                        // Arrange
                        given(classroomService.getClassrooms(anyMap(), any()))
                                        .willReturn(new PageImpl<>(Collections.emptyList()));

                        // Act & Assert
                        mockMvc.perform(get(BASE_URL))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.content", empty()));
                }
        }

        // ═══════════════════════════════════════════════════════════════════════════
        // GET /{id}/response
        // ═══════════════════════════════════════════════════════════════════════════
        @Nested
        @DisplayName("GET /{id}/response")
        class GetClassroomByIdTests {

                @Test
                @DisplayName("Happy Path: returns 200 with classroom response DTO")
                void getClassroomResponseById_WhenFound_Returns200() throws Exception {
                        // Arrange
                        Classroom classroom = Classroom.builder().id(classroomId).name("10A1").build();
                        given(classroomService.getClassroomById(classroomId)).willReturn(classroom);
                        given(classroomMapper.toClassroomResponseDTO(classroom)).willReturn(classroomResponseDTO);

                        // Act & Assert
                        mockMvc.perform(get(BASE_URL + "/{id}/response", classroomId))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.id").value(classroomId.toString()))
                                        .andExpect(jsonPath("$.name").value("10A1"))
                                        .andExpect(jsonPath("$.studentCount").value(30));
                }

                @Test
                @DisplayName("Exception Path: returns 404 when classroom not found")
                void getClassroomResponseById_WhenNotFound_Returns404() throws Exception {
                        // Arrange
                        given(classroomService.getClassroomById(classroomId))
                                        .willThrow(new ResourceNotFoundException("classroom.notFound"));

                        // Act & Assert
                        mockMvc.perform(get(BASE_URL + "/{id}/response", classroomId))
                                        .andExpect(status().isNotFound())
                                        .andExpect(jsonPath("$.status").value(404))
                                        .andExpect(jsonPath("$.message").value("classroom.notFound"));
                }
        }

        // ═══════════════════════════════════════════════════════════════════════════
        // POST /
        // ═══════════════════════════════════════════════════════════════════════════
        @Nested
        @DisplayName("POST /")
        class SaveClassroomTests {

                @Test
                @DisplayName("Happy Path (new classroom): returns 200")
                void saveClassroom_NewClassroom_Returns200() throws Exception {
                        // Arrange — dto with no id → new classroom
                        ClassroomRequestDTO dto = ClassroomRequestDTO.builder()
                                        .gradeId(gradeId)
                                        .name("10A1")
                                        .studentIds(Collections.emptyList())
                                        .build();

                        Classroom classroom = new Classroom();
                        given(classroomService.saveClassroom(any(), eq(gradeId), anyList()))
                                        .willReturn(classroom);

                        // Act & Assert
                        mockMvc.perform(post(BASE_URL)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(dto)))
                                        .andExpect(status().isOk());
                }

                @Test
                @DisplayName("Validation Path: returns 400 when gradeId is null")
                void saveClassroom_WithNullGradeId_Returns400() throws Exception {
                        // Arrange — gradeId is @NotNull
                        ClassroomRequestDTO dto = ClassroomRequestDTO.builder()
                                        .gradeId(null)
                                        .name("10A1")
                                        .build();

                        // Act & Assert
                        mockMvc.perform(post(BASE_URL)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(dto)))
                                        .andExpect(status().isBadRequest());
                }

                @Test
                @DisplayName("Exception Path: returns 400 when classroom name already exists (from service/validator)")
                void saveClassroom_WhenNameConflict_Returns400() throws Exception {
                        // Arrange — validator rejects the request
                        ClassroomRequestDTO dto = ClassroomRequestDTO.builder()
                                        .gradeId(gradeId)
                                        .name("10A1")
                                        .studentIds(Collections.emptyList())
                                        .build();

                        // Simulate validator injecting a field error → MethodArgumentNotValidException
                        // pathway
                        // is handled by @RestControllerAdvice → 400
                        willThrow(new BadRequestException("classroom.unique"))
                                        .given(classroomService).saveClassroom(any(), any(), any());

                        // Act & Assert
                        mockMvc.perform(post(BASE_URL)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(dto)))
                                        .andExpect(status().isBadRequest())
                                        .andExpect(jsonPath("$.status").value(400));
                }

                @Test
                @DisplayName("Happy Path (update classroom): returns 200 when id is present")
                void saveClassroom_UpdateExistingClassroom_Returns200() throws Exception {
                        // Arrange
                        Classroom existing = Classroom.builder().id(classroomId).name("10A1").build();
                        ClassroomRequestDTO dto = ClassroomRequestDTO.builder()
                                        .id(classroomId)
                                        .gradeId(gradeId)
                                        .name("10A1 Updated")
                                        .studentIds(Collections.emptyList())
                                        .build();

                        given(classroomService.getClassroomWithStudents(classroomId)).willReturn(existing);
                        given(classroomService.saveClassroom(any(), eq(gradeId), anyList()))
                                        .willReturn(existing);

                        // Act & Assert
                        mockMvc.perform(post(BASE_URL)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(dto)))
                                        .andExpect(status().isOk());
                }
        }

        // ═══════════════════════════════════════════════════════════════════════════
        // DELETE /{id}
        // ═══════════════════════════════════════════════════════════════════════════
        @Nested
        @DisplayName("DELETE /{id}")
        class DeleteClassroomTests {

                @Test
                @DisplayName("Happy Path: returns 204 No Content on successful delete")
                void deleteClassroom_WhenNoStudents_Returns204() throws Exception {
                        // Arrange
                        willDoNothing().given(classroomService).deleteClassroom(classroomId);

                        // Act & Assert
                        mockMvc.perform(delete(BASE_URL + "/{id}", classroomId))
                                        .andExpect(status().isNoContent());
                }

                @Test
                @DisplayName("Exception Path: returns 400 when classroom still has students")
                void deleteClassroom_WhenHasStudents_Returns400() throws Exception {
                        // Arrange
                        willThrow(new BadRequestException("classroom.delete.error.hasStudents"))
                                        .given(classroomService).deleteClassroom(classroomId);

                        // Act & Assert
                        mockMvc.perform(delete(BASE_URL + "/{id}", classroomId))
                                        .andExpect(status().isBadRequest())
                                        .andExpect(jsonPath("$.status").value(400))
                                        .andExpect(jsonPath("$.message").value("classroom.delete.error.hasStudents"));
                }
        }
}
