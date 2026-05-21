package com.lqm.attendance_service.consumers;

import com.lqm.attendance_service.dtos.AbsentQueueMessage;
import com.lqm.attendance_service.models.Attendance;
import com.lqm.attendance_service.models.AttendanceStatus;
import com.lqm.attendance_service.repositories.AttendanceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AbsentQueueConsumerTest {

    @Mock
    private AttendanceRepository attendanceRepo;

    @InjectMocks
    private AbsentQueueConsumer absentQueueConsumer;

    @Captor
    private ArgumentCaptor<List<Attendance>> captor;

    private UUID classroomId;
    private UUID studentId;
    private LocalDate date;

    @BeforeEach
    void setUp() {
        classroomId = UUID.randomUUID();
        studentId = UUID.randomUUID();
        date = LocalDate.now();
    }

    @Test
    void processAbsentQueue_ValidMessage_ShouldSaveAttendances() {
        AbsentQueueMessage message = AbsentQueueMessage.builder()
                .classroomId(classroomId.toString())
                .studentIds(List.of(studentId.toString()))
                .date(date.toString())
                .build();

        absentQueueConsumer.processAbsentQueue(message);

        verify(attendanceRepo).saveAll(captor.capture());
        List<Attendance> savedAttendances = captor.getValue();
        assertEquals(1, savedAttendances.size());
        assertEquals(studentId, savedAttendances.get(0).getStudentId());
        assertEquals(classroomId, savedAttendances.get(0).getClassroomId());
        assertEquals(AttendanceStatus.ABSENT, savedAttendances.get(0).getStatus());
    }

    @Test
    void processAbsentQueue_EmptyStudentIds_ShouldNotSaveAttendances() {
        AbsentQueueMessage message = AbsentQueueMessage.builder()
                .classroomId(classroomId.toString())
                .studentIds(List.of())
                .date(date.toString())
                .build();

        absentQueueConsumer.processAbsentQueue(message);

        verify(attendanceRepo, never()).saveAll(anyList());
    }

    @Test
    void processAbsentQueue_InvalidClassroomId_ShouldThrowException() {
        AbsentQueueMessage message = AbsentQueueMessage.builder()
                .classroomId("invalid-uuid")
                .studentIds(List.of(studentId.toString()))
                .date(date.toString())
                .build();

        assertThrows(IllegalArgumentException.class, () -> absentQueueConsumer.processAbsentQueue(message));
    }
}
