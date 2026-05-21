package com.lqm.attendance_service.services;

import com.lqm.attendance_service.exceptions.ResourceNotFoundException;
import com.lqm.attendance_service.models.Device;
import com.lqm.attendance_service.models.Fingerprint;
import com.lqm.attendance_service.repositories.FingerprintRepository;
import com.lqm.attendance_service.services.Impl.FingerprintServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FingerprintServiceImplTest {

    @Mock
    private FingerprintRepository fingerprintRepo;

    @Mock
    private MessageSource messageSource;

    @Mock
    private MqttService mqttService;

    @Mock
    private DeviceService deviceService;

    @InjectMocks
    private FingerprintServiceImpl fingerprintService;

    private UUID classroomId;
    private UUID studentId;
    private Integer fingerprintIndex;
    private Fingerprint fingerprint;
    private Device device;

    @BeforeEach
    void setUp() {
        classroomId = UUID.randomUUID();
        studentId = UUID.randomUUID();
        fingerprintIndex = 1;

        fingerprint = Fingerprint.builder()
                .classroomId(classroomId)
                .studentId(studentId)
                .fingerprintIndex(fingerprintIndex)
                .build();

        device = Device.builder()
                .id("ESP32_001")
                .classroomId(classroomId)
                .isActive(true)
                .build();
    }

    @Test
    void getFingerprintByFingerprintIndexAndClassroomId_WhenExists_ShouldReturnFingerprint() {
        when(fingerprintRepo.findByFingerprintIndexAndClassroomId(fingerprintIndex, classroomId))
                .thenReturn(Optional.of(fingerprint));

        Fingerprint result = fingerprintService.getFingerprintByFingerprintIndexAndClassroomId(fingerprintIndex, classroomId);

        assertNotNull(result);
        assertEquals(fingerprintIndex, result.getFingerprintIndex());
    }

    @Test
    void getFingerprintByFingerprintIndexAndClassroomId_WhenNotExists_ShouldThrowException() {
        when(fingerprintRepo.findByFingerprintIndexAndClassroomId(fingerprintIndex, classroomId))
                .thenReturn(Optional.empty());
        when(messageSource.getMessage(eq("fingerprint.notfound"), isNull(), any()))
                .thenReturn("Fingerprint not found");

        assertThrows(ResourceNotFoundException.class, 
                () -> fingerprintService.getFingerprintByFingerprintIndexAndClassroomId(fingerprintIndex, classroomId));
    }

    @Test
    void saveFingerprint_ShouldReturnSavedFingerprint() {
        when(fingerprintRepo.save(fingerprint)).thenReturn(fingerprint);

        Fingerprint result = fingerprintService.saveFingerprint(fingerprint);

        assertNotNull(result);
        assertEquals(studentId, result.getStudentId());
    }

    @Test
    void deleteFingerprintsByClassroomAndStudentIds_WhenDeviceActive_ShouldCallMqttAndDelete() {
        List<UUID> studentIds = List.of(studentId);
        when(fingerprintRepo.findByClassroomIdAndStudentIdIn(classroomId, studentIds)).thenReturn(List.of(fingerprint));
        when(deviceService.getDeviceByClassroom(classroomId)).thenReturn(device);

        fingerprintService.deleteFingerprintsByClassroomAndStudentIds(classroomId, studentIds);

        verify(mqttService).deleteFingerprint(device.getId(), fingerprintIndex);
        verify(fingerprintRepo).deleteByClassroomIdAndStudentIdIn(classroomId, studentIds);
    }

    @Test
    void deleteFingerprintsByClassroomAndStudentIds_WhenDeviceInactive_ShouldOnlyDeleteInDb() {
        device.setIsActive(false);
        List<UUID> studentIds = List.of(studentId);
        when(fingerprintRepo.findByClassroomIdAndStudentIdIn(classroomId, studentIds)).thenReturn(List.of(fingerprint));
        when(deviceService.getDeviceByClassroom(classroomId)).thenReturn(device);

        fingerprintService.deleteFingerprintsByClassroomAndStudentIds(classroomId, studentIds);

        verify(mqttService, never()).deleteFingerprint(anyString(), anyInt());
        verify(fingerprintRepo).deleteByClassroomIdAndStudentIdIn(classroomId, studentIds);
    }

    @Test
    void deleteFingerprintsByClassroomAndStudentIds_WhenMqttFails_ShouldStillDeleteInDb() {
        List<UUID> studentIds = List.of(studentId);
        when(fingerprintRepo.findByClassroomIdAndStudentIdIn(classroomId, studentIds)).thenReturn(List.of(fingerprint));
        when(deviceService.getDeviceByClassroom(classroomId)).thenReturn(device);
        doThrow(new RuntimeException("MQTT Error")).when(mqttService).deleteFingerprint(anyString(), anyInt());

        fingerprintService.deleteFingerprintsByClassroomAndStudentIds(classroomId, studentIds);

        verify(fingerprintRepo).deleteByClassroomIdAndStudentIdIn(classroomId, studentIds);
    }

    @Test
    void deleteFingerprintsByClassroomId_WhenDeviceActive_ShouldCallMqttClearAllAndDelete() {
        when(deviceService.getDeviceByClassroom(classroomId)).thenReturn(device);

        fingerprintService.deleteFingerprintsByClassroomId(classroomId);

        verify(mqttService).clearAllFingerprints(device.getId());
        verify(fingerprintRepo).deleteByClassroomId(classroomId);
    }

    @Test
    void deleteFingerprintsByClassroomId_WhenDeviceInactive_ShouldOnlyDeleteInDb() {
        device.setIsActive(false);
        when(deviceService.getDeviceByClassroom(classroomId)).thenReturn(device);

        fingerprintService.deleteFingerprintsByClassroomId(classroomId);

        verify(mqttService, never()).clearAllFingerprints(anyString());
        verify(fingerprintRepo).deleteByClassroomId(classroomId);
    }

    @Test
    void deleteFingerprintsByClassroomId_WhenMqttFails_ShouldStillDeleteInDb() {
        when(deviceService.getDeviceByClassroom(classroomId)).thenReturn(device);
        doThrow(new RuntimeException("MQTT Error")).when(mqttService).clearAllFingerprints(anyString());

        fingerprintService.deleteFingerprintsByClassroomId(classroomId);

        verify(fingerprintRepo).deleteByClassroomId(classroomId);
    }
}
