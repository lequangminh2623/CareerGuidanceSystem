package com.lqm.admin_service.it;

import com.lqm.admin_service.clients.*;
import com.lqm.admin_service.dtos.ClassroomRequestDTO;
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

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("SectionControllerIT — Integration Tests")
class SectionControllerIT {

        @Autowired
        private MockMvc mockMvc;

        @MockitoBean
        private SectionClient sectionClient;
        @MockitoBean
        private ClassroomClient classroomClient;
        @MockitoBean
        private CurriculumClient curriculumClient;
        @MockitoBean
        private UserClient userClient;
        @MockitoBean
        private GradeClient gradeClient;

        @MockitoBean
        private AuthClient authClient;
        @MockitoBean
        private DeviceClient deviceClient;
        @MockitoBean
        private SubjectClient subjectClient;
        @MockitoBean
        private FingerprintClient fingerprintClient;
        @MockitoBean
        private AttendanceClient attendanceClient;
        @MockitoBean
        private SemesterClient semesterClient;
        @MockitoBean
        private TranscriptClient transcriptClient;
        @MockitoBean
        private YearClient yearClient;

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("GET /classrooms/{classroomId}/sections — Tải danh sách sections thành công")
        void getSections_Success() throws Exception {
                UUID classroomId = UUID.randomUUID();
                UUID gradeId = UUID.randomUUID();

                ClassroomRequestDTO classroomDto = ClassroomRequestDTO.builder()
                                .id(classroomId)
                                .name("12A1")
                                .gradeId(gradeId)
                                .build();

                when(classroomClient.getClassroomRequestById(classroomId)).thenReturn(classroomDto);

                when(sectionClient.getSectionRequests(ArgumentMatchers.<Map<String, String>>any()))
                                .thenReturn(new PageImpl<>(List.of()));

                when(curriculumClient.getCurriculums(ArgumentMatchers.<Map<String, String>>any()))
                                .thenReturn(new PageImpl<>(List.of()));

                mockMvc.perform(get("/classrooms/" + classroomId + "/sections"))
                                .andExpect(status().isOk())
                                .andExpect(view().name("section/list"))
                                .andExpect(model().attributeExists("classroomName", "sections", "curriculums",
                                                "teachers"));
        }
}
