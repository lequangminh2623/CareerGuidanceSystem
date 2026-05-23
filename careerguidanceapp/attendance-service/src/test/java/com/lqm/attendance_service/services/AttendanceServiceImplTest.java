package com.lqm.attendance_service.services;

import com.lqm.attendance_service.dtos.AttendanceResponseDTO;
import com.lqm.attendance_service.dtos.AttendanceSummaryDTO;
import com.lqm.attendance_service.mappers.AttendanceMapper;
import com.lqm.attendance_service.models.Attendance;
import com.lqm.attendance_service.models.AttendanceConfig;
import com.lqm.attendance_service.models.AttendanceSession;
import com.lqm.attendance_service.models.AttendanceStatus;
import com.lqm.attendance_service.repositories.AttendanceRepository;
import com.lqm.attendance_service.services.Impl.AttendanceServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AttendanceServiceImplTest {

    @Mock
    private AttendanceRepository attendanceRepo;

    @Mock
    private AttendanceMapper attendanceMapper;

    @Mock
    private AttendanceConfigService configService;

    @InjectMocks
    private AttendanceServiceImpl attendanceService;

    private UUID classroomId;
    private UUID studentId;
    private LocalDate today;
    private Attendance attendance;
    private AttendanceResponseDTO attendanceResponseDTO;
    private AttendanceConfig defaultConfig;

    @BeforeEach
    void setUp() {
        classroomId = UUID.randomUUID();
        studentId = UUID.randomUUID();
        today = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));

        attendance = Attendance.builder()
                .id(UUID.randomUUID())
                .classroomId(classroomId)
                .studentId(studentId)
                .attendanceDate(today)
                .status(AttendanceStatus.PRESENT)
                .checkInTime(LocalTime.of(6, 30))
                .session(AttendanceSession.MORNING)
                .build();

        attendanceResponseDTO = new AttendanceResponseDTO(
                attendance.getId(),
                studentId,
                today,
                LocalTime.of(6, 30),
                AttendanceStatus.PRESENT.name(),
                "MORNING"
        );

        defaultConfig = AttendanceConfig.builder()
                .id(1L)
                .sessionsPerDay(2)
                .morningStartTime(LocalTime.of(7, 0))
                .morningEndTime(LocalTime.of(12, 30))
                .afternoonStartTime(LocalTime.of(13, 0))
                .afternoonEndTime(LocalTime.of(23, 59))
                .build();
    }

    @Test
    void getAttendancesByClassroomAndDate_ShouldReturnList() {
        when(attendanceRepo.findByClassroomIdAndAttendanceDate(classroomId, today)).thenReturn(List.of(attendance));
        when(attendanceMapper.toAttendanceResponseDTO(attendance)).thenReturn(attendanceResponseDTO);

        List<AttendanceResponseDTO> result = attendanceService.getAttendancesByClassroomAndDate(classroomId, today);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(AttendanceStatus.PRESENT.name(), result.get(0).status());
        verify(attendanceRepo, times(1)).findByClassroomIdAndAttendanceDate(classroomId, today);
    }

    @Test
    void saveAttendances_WhenAttendanceExists_ShouldUpdate() {
        List<Attendance> existingAttendances = new ArrayList<>(List.of(attendance));
        
        Attendance newAttendance = Attendance.builder()
                .studentId(studentId)
                .status(AttendanceStatus.LATE)
                .checkInTime(LocalTime.of(8, 0))
                .build();

        when(attendanceRepo.findByClassroomIdAndAttendanceDate(classroomId, today)).thenReturn(existingAttendances);

        attendanceService.saveAttendances(classroomId, today, List.of(newAttendance));

        assertEquals(AttendanceStatus.LATE, existingAttendances.get(0).getStatus());
        assertEquals(LocalTime.of(8, 0), existingAttendances.get(0).getCheckInTime());
        verify(attendanceRepo, times(1)).saveAll(existingAttendances);
    }

    @Test
    void saveAttendances_WhenAttendanceDoesNotExist_ShouldAdd() {
        List<Attendance> existingAttendances = new ArrayList<>();
        
        Attendance newAttendance = Attendance.builder()
                .studentId(studentId)
                .status(AttendanceStatus.PRESENT)
                .build();

        when(attendanceRepo.findByClassroomIdAndAttendanceDate(classroomId, today)).thenReturn(existingAttendances);

        attendanceService.saveAttendances(classroomId, today, List.of(newAttendance));

        assertEquals(1, existingAttendances.size());
        assertEquals(studentId, existingAttendances.get(0).getStudentId());
        verify(attendanceRepo, times(1)).saveAll(existingAttendances);
    }

    @Test
    void deleteAttendancesByClassroomAndDate_ShouldCallRepository() {
        attendanceService.deleteAttendancesByClassroomAndDate(classroomId, today);
        verify(attendanceRepo, times(1)).deleteByClassroomIdAndAttendanceDate(classroomId, today);
    }

    @Test
    void recordAttendance_WhenAlreadyExists_ShouldReturnExistingStatus() {
        when(configService.getConfig()).thenReturn(defaultConfig);
        when(attendanceRepo.findByStudentIdAndAttendanceDateAndSession(eq(studentId), any(LocalDate.class), any(AttendanceSession.class)))
                .thenReturn(Optional.of(attendance));

        var result = attendanceService.recordAttendance(studentId, classroomId);

        assertEquals(AttendanceStatus.PRESENT, result.status());
        assertFalse(result.isNew());
        assertNotNull(result.session());
        verify(attendanceRepo, never()).save(any());
    }

    @Test
    void recordAttendance_WhenNotExists_ShouldSaveAndReturnStatus() {
        when(configService.getConfig()).thenReturn(defaultConfig);
        when(attendanceRepo.findByStudentIdAndAttendanceDateAndSession(eq(studentId), any(LocalDate.class), any(AttendanceSession.class)))
                .thenReturn(Optional.empty());

        var result = attendanceService.recordAttendance(studentId, classroomId);

        assertNotNull(result.status());
        assertTrue(result.isNew());
        assertNotNull(result.session());
        verify(attendanceRepo, times(1)).save(any(Attendance.class));
    }

    @Test
    void getStudentAttendanceByClassroom_ShouldReturnList() {
        when(attendanceRepo.findByStudentIdAndClassroomId(studentId, classroomId)).thenReturn(List.of(attendance));
        when(attendanceMapper.toAttendanceResponseDTO(attendance)).thenReturn(attendanceResponseDTO);

        List<AttendanceResponseDTO> result = attendanceService.getStudentAttendanceByClassroom(studentId, classroomId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(studentId, result.get(0).studentId());
    }

    @Test
    void getAttendanceSummary_ShouldReturnCorrectCounts() {
        Attendance attendance2 = Attendance.builder().status(AttendanceStatus.LATE).build();
        Attendance attendance3 = Attendance.builder().status(AttendanceStatus.ABSENT).build();
        
        when(attendanceRepo.findByStudentId(studentId)).thenReturn(List.of(attendance, attendance2, attendance3));

        AttendanceSummaryDTO summary = attendanceService.getAttendanceSummary(studentId);

        assertNotNull(summary);
        assertEquals(1, summary.presentCount());
        assertEquals(1, summary.lateCount());
        assertEquals(1, summary.absentCount());
    }

    @Test
    void deleteAttendancesByClassroomAndStudentIds_ShouldCallRepository() {
        List<UUID> studentIds = List.of(studentId);
        attendanceService.deleteAttendancesByClassroomAndStudentIds(classroomId, studentIds);
        verify(attendanceRepo, times(1)).deleteByClassroomIdAndStudentIdIn(classroomId, studentIds);
    }
}
