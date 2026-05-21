package com.lqm.admin_service.it;

import com.lqm.admin_service.clients.*;
import com.lqm.admin_service.dtos.ClassroomResponseDTO;
import com.lqm.admin_service.dtos.GradeDetailsResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("ClassroomControllerIT — Integration Tests")
class ClassroomControllerIT {

        @Autowired
        private MockMvc mockMvc;

        @MockitoBean
        private ClassroomClient classroomClient;
        @MockitoBean
        private UserClient userClient;
        @MockitoBean
        private GradeClient gradeClient;
        @MockitoBean
        private AuthClient authClient;
        @MockitoBean
        private DeviceClient deviceClient;
        @MockitoBean
        private SectionClient sectionClient;
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
        private TranscriptClient transcriptClient;
        @MockitoBean
        private YearClient yearClient;

        private UUID gradeId;

        @BeforeEach
        void setUp() {
                gradeId = UUID.randomUUID();

                // Mock grades dropdown (for @ModelAttribute "groupedGrades")
                GradeDetailsResponseDTO gradeDto = new GradeDetailsResponseDTO(gradeId, "K12", "2024");
                when(gradeClient.getGradesDetails(ArgumentMatchers.<Map<String, String>>any()))
                                .thenReturn(List.of(gradeDto));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("GET /classrooms — Truy cập thành công và render danh sách")
        void getClassrooms_Success() throws Exception {
                ClassroomResponseDTO classroomDto = ClassroomResponseDTO.builder()
                                .id(UUID.randomUUID())
                                .name("12A1")
                                .studentCount(35)
                                .build();

                when(classroomClient.getClassrooms(ArgumentMatchers.<Map<String, String>>any()))
                                .thenReturn(new PageImpl<>(List.of(classroomDto)));

                mockMvc.perform(get("/classrooms")
                                .param("gradeId", gradeId.toString()))
                                .andExpect(status().isOk())
                                .andExpect(view().name("classroom/list"))
                                .andExpect(model().attributeExists("classrooms", "groupedGrades"));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("POST /classrooms — Tạo lớp học thành công")
        void saveClassroom_Success() throws Exception {

                doNothing().when(classroomClient).saveClassroom(any());

                mockMvc.perform(post("/classrooms")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                                .param("name", "12A2")
                                .param("gradeId", gradeId.toString()))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrl("/classrooms?gradeId=" + gradeId.toString()));

                verify(classroomClient, times(1)).saveClassroom(any());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("POST /classrooms — Form bị lỗi validation (không có tên)")
        void saveClassroom_ValidationFailed() throws Exception {
                // Omitting 'name' to trigger Bean Validation
                mockMvc.perform(post("/classrooms")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                                .param("gradeId", gradeId.toString()))
                                .andExpect(status().isOk()) // Returns back to form with 200 OK
                                .andExpect(view().name("classroom/form"))
                                .andExpect(model().attributeHasFieldErrors("classroom", "name"));

                verify(classroomClient, never()).saveClassroom(any());
        }
}
