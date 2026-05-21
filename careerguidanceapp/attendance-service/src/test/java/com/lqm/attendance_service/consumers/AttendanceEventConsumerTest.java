package com.lqm.attendance_service.consumers;

import com.lqm.attendance_service.events.ClassroomDeletedEvent;
import com.lqm.attendance_service.events.StudentsRemovedEvent;
import com.lqm.attendance_service.services.AttendanceService;
import com.lqm.attendance_service.services.DeviceService;
import com.lqm.attendance_service.services.FingerprintService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AttendanceEventConsumerTest {

    @Mock
    private AttendanceService attendanceService;

    @Mock
    private FingerprintService fingerprintService;

    @Mock
    private DeviceService deviceService;

    @InjectMocks
    private AttendanceEventConsumer attendanceEventConsumer;

    @Test
    void handleStudentsRemoved_HappyPath_ShouldDeleteData() {
        UUID classroomId = UUID.randomUUID();
        List<UUID> studentIds = List.of(UUID.randomUUID());
        StudentsRemovedEvent event = new StudentsRemovedEvent(classroomId, studentIds);

        attendanceEventConsumer.handleStudentsRemoved(event);

        verify(attendanceService).deleteAttendancesByClassroomAndStudentIds(classroomId, studentIds);
        verify(fingerprintService).deleteFingerprintsByClassroomAndStudentIds(classroomId, studentIds);
    }

    @Test
    void handleStudentsRemoved_WhenException_ShouldThrow() {
        UUID classroomId = UUID.randomUUID();
        List<UUID> studentIds = List.of(UUID.randomUUID());
        StudentsRemovedEvent event = new StudentsRemovedEvent(classroomId, studentIds);

        doThrow(new RuntimeException("DB Error")).when(attendanceService).deleteAttendancesByClassroomAndStudentIds(any(), any());

        assertThrows(RuntimeException.class, () -> attendanceEventConsumer.handleStudentsRemoved(event));
    }

    @Test
    void handleClassroomDeleted_HappyPath_ShouldUnassignDevice() {
        UUID classroomId = UUID.randomUUID();
        ClassroomDeletedEvent event = new ClassroomDeletedEvent(classroomId, List.of(UUID.randomUUID()));

        attendanceEventConsumer.handleClassroomDeleted(event);

        verify(deviceService).unassignDeviceByClassroomId(classroomId);
    }

    @Test
    void handleClassroomDeleted_WhenException_ShouldThrow() {
        UUID classroomId = UUID.randomUUID();
        ClassroomDeletedEvent event = new ClassroomDeletedEvent(classroomId, List.of(UUID.randomUUID()));

        doThrow(new RuntimeException("DB Error")).when(deviceService).unassignDeviceByClassroomId(any());

        assertThrows(RuntimeException.class, () -> attendanceEventConsumer.handleClassroomDeleted(event));
    }
}
