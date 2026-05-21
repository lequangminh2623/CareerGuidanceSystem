package com.lqm.attendance_service.services;

import com.lqm.attendance_service.clients.ClassroomClient;
import com.lqm.attendance_service.dtos.AcademicResponseDTO;
import com.lqm.attendance_service.exceptions.ResourceNotFoundException;
import com.lqm.attendance_service.models.Device;
import com.lqm.attendance_service.repositories.DeviceRepository;
import com.lqm.attendance_service.services.Impl.DeviceServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DeviceServiceImplTest {

    @Mock
    private DeviceRepository deviceRepository;

    @Mock
    private MqttService mqttService;

    @Mock
    private ClassroomClient classroomClient;

    @Mock
    private MessageSource messageSource;

    @Mock
    private FingerprintService fingerprintService;

    @InjectMocks
    private DeviceServiceImpl deviceService;

    private Device device;
    private String deviceId;
    private UUID classroomId;

    @BeforeEach
    void setUp() {
        deviceId = "ESP32_001";
        classroomId = UUID.randomUUID();

        device = Device.builder()
                .id(deviceId)
                .classroomId(classroomId)
                .isActive(true)
                .build();

        ReflectionTestUtils.setField(deviceService, "fingerprintService", fingerprintService);
    }

    @Test
    void getDeviceById_WhenExists_ShouldReturnDevice() {
        when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(device));

        Device result = deviceService.getDeviceById(deviceId);

        assertNotNull(result);
        assertEquals(deviceId, result.getId());
    }

    @Test
    void getDeviceById_WhenNotExists_ShouldThrowResourceNotFoundException() {
        when(deviceRepository.findById(deviceId)).thenReturn(Optional.empty());
        when(messageSource.getMessage(eq("device.notFound"), isNull(), any())).thenReturn("Device not found");

        assertThrows(ResourceNotFoundException.class, () -> deviceService.getDeviceById(deviceId));
    }

    @Test
    void getAllDevices_ShouldReturnPageOfDevices() {
        Page<Device> page = new PageImpl<>(List.of(device));
        when(deviceRepository.findAll(Mockito.<Specification<Device>>any(), any(PageRequest.class))).thenReturn(page);

        Page<Device> result = deviceService.getAllDevices(new HashMap<>(), PageRequest.of(0, 10));

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void saveDevice_ShouldReturnSavedDevice() {
        when(deviceRepository.save(device)).thenReturn(device);

        Device result = deviceService.saveDevice(device);

        assertNotNull(result);
        assertEquals(deviceId, result.getId());
    }

    @Test
    void updateDeviceActiveStatus_WhenNotifyMqttTrue_ShouldCallMqttService() {
        when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(device));

        deviceService.updateDeviceActiveStatus(deviceId, false, true);

        assertFalse(device.getIsActive());
        verify(deviceRepository).save(device);
        verify(mqttService).togglePower(deviceId, false);
    }

    @Test
    void updateDeviceActiveStatus_WhenNotifyMqttFalse_ShouldNotCallMqttService() {
        when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(device));

        deviceService.updateDeviceActiveStatus(deviceId, false, false);

        assertFalse(device.getIsActive());
        verify(deviceRepository).save(device);
        verify(mqttService, never()).togglePower(anyString(), anyBoolean());
    }

    @Test
    void deleteDevice_WhenExistsAndHasClassroom_ShouldDeleteDeviceAndFingerprints() {
        when(deviceRepository.existsById(deviceId)).thenReturn(true);
        when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(device));

        deviceService.deleteDevice(deviceId);

        verify(fingerprintService).deleteFingerprintsByClassroomId(classroomId);
        verify(deviceRepository).deleteById(deviceId);
    }

    @Test
    void deleteDevice_WhenExistsAndNoClassroom_ShouldOnlyDeleteDevice() {
        device.setClassroomId(null);
        when(deviceRepository.existsById(deviceId)).thenReturn(true);
        when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(device));

        deviceService.deleteDevice(deviceId);

        verify(fingerprintService, never()).deleteFingerprintsByClassroomId(any());
        verify(deviceRepository).deleteById(deviceId);
    }

    @Test
    void deleteDevice_WhenNotExists_ShouldThrowResourceNotFoundException() {
        when(deviceRepository.existsById(deviceId)).thenReturn(false);
        when(messageSource.getMessage(eq("device.notFound"), isNull(), any())).thenReturn("Device not found");

        assertThrows(ResourceNotFoundException.class, () -> deviceService.deleteDevice(deviceId));
    }

    @Test
    void getDevicesWithoutClassroom_ShouldReturnDevicesWithNullClassroomId() {
        Device device2 = Device.builder().id("ESP32_002").classroomId(null).build();
        when(deviceRepository.findAll()).thenReturn(List.of(device, device2));

        List<Device> result = deviceService.getDevicesWithoutClassroom();

        assertEquals(1, result.size());
        assertEquals("ESP32_002", result.get(0).getId());
    }

    @Test
    void getDeviceByClassroom_ShouldReturnDevice() {
        when(deviceRepository.findByClassroomId(classroomId)).thenReturn(Optional.of(device));

        Device result = deviceService.getDeviceByClassroom(classroomId);

        assertNotNull(result);
        assertEquals(classroomId, result.getClassroomId());
    }

    @Test
    void assignDeviceToClassroom_ShouldUpdateAndReturnDevice() {
        UUID newClassroomId = UUID.randomUUID();
        Device updateRequest = Device.builder().id(deviceId).classroomId(newClassroomId).build();

        when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(device));
        when(deviceRepository.save(any(Device.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Device result = deviceService.assignDeviceToClassroom(updateRequest);

        assertEquals(newClassroomId, result.getClassroomId());
    }

    @Test
    void unassignDeviceFromClassroom_ShouldClearClassroomIdAndDeleteFingerprints() {
        when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(device));

        deviceService.unassignDeviceFromClassroom(deviceId);

        assertNull(device.getClassroomId());
        verify(fingerprintService).deleteFingerprintsByClassroomId(classroomId);
        verify(deviceRepository).save(device);
    }

    @Test
    void unassignDeviceByClassroomId_WhenDeviceExists_ShouldUnassign() {
        when(deviceRepository.findByClassroomId(classroomId)).thenReturn(Optional.of(device));
        when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(device));

        deviceService.unassignDeviceByClassroomId(classroomId);

        assertNull(device.getClassroomId());
        verify(deviceRepository).save(device);
    }

    @Test
    void buildClassroomMap_WhenDevicesHaveClassrooms_ShouldReturnMap() {
        AcademicResponseDTO academicResponseDTO = new AcademicResponseDTO(classroomId, "Class 10A");
        when(classroomClient.getClassroomDetailNames(anyList(), anyMap())).thenReturn(List.of(academicResponseDTO));

        Map<UUID, String> result = deviceService.buildClassroomMap(List.of(device));

        assertEquals(1, result.size());
        assertEquals("Class 10A", result.get(classroomId));
    }

    @Test
    void buildClassroomMap_WhenDevicesHaveNoClassrooms_ShouldReturnEmptyMap() {
        device.setClassroomId(null);
        Map<UUID, String> result = deviceService.buildClassroomMap(List.of(device));

        assertTrue(result.isEmpty());
        verify(classroomClient, never()).getClassroomDetailNames(anyList(), anyMap());
    }

    @Test
    void existDeviceById_ShouldReturnRepositoryResult() {
        when(deviceRepository.existsById(deviceId)).thenReturn(true);
        assertTrue(deviceService.existDeviceById(deviceId));
    }

    @Test
    void existDeviceByClassroomId_ShouldReturnRepositoryResult() {
        when(deviceRepository.existsByClassroomId(classroomId)).thenReturn(true);
        assertTrue(deviceService.existDeviceByClassroomId(classroomId));
    }
}
