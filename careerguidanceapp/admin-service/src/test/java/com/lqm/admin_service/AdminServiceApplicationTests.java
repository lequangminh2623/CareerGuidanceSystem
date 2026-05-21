package com.lqm.admin_service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.lqm.admin_service.clients.*;

@SpringBootTest
@ActiveProfiles("test")
class AdminServiceApplicationTests {

    // Mock all Feign Clients to prevent them from attempting real network calls during context load
    @MockitoBean private AttendanceClient attendanceClient;
    @MockitoBean private AuthClient authClient;
    @MockitoBean private ClassroomClient classroomClient;
    @MockitoBean private CurriculumClient curriculumClient;
    @MockitoBean private DeviceClient deviceClient;
    @MockitoBean private FingerprintClient fingerprintClient;
    @MockitoBean private GradeClient gradeClient;
    @MockitoBean private SectionClient sectionClient;
    @MockitoBean private SemesterClient semesterClient;
    @MockitoBean private SubjectClient subjectClient;
    @MockitoBean private TranscriptClient transcriptClient;
    @MockitoBean private UserClient userClient;
    @MockitoBean private YearClient yearClient;

    @Test
    void contextLoads() {
        // Test passes if Spring ApplicationContext starts successfully
    }
}
