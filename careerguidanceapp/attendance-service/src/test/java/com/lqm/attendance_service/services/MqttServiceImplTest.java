package com.lqm.attendance_service.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lqm.attendance_service.clients.UserClient;
import com.lqm.attendance_service.dtos.AttendanceRecordResult;
import com.lqm.attendance_service.dtos.AttendanceRequestDTO;
import com.lqm.attendance_service.dtos.DeviceStatusDTO;
import com.lqm.attendance_service.dtos.FingerprintRequestDTO;
import com.lqm.attendance_service.dtos.UserResponseDTO;
import com.lqm.attendance_service.dtos.WebSocketEventDTO;
import com.lqm.attendance_service.mappers.FingerprintMapper;
import com.lqm.attendance_service.models.AttendanceSession;
import com.lqm.attendance_service.models.AttendanceStatus;
import com.lqm.attendance_service.models.Device;
import com.lqm.attendance_service.models.Fingerprint;
import com.lqm.attendance_service.services.Impl.MqttServiceImpl;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MqttServiceImplTest {

    @Mock
    private IMqttClient mqttClient;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private DeviceService deviceService;
    @Mock
    private FingerprintService fingerprintService;
    @Mock
    private AttendanceService attendanceService;
    @Mock
    private FingerprintMapper fingerprintMapper;
    @Mock
    private RestTemplate restTemplate;
    @Mock
    private SimpMessagingTemplate messagingTemplate;
    @Mock
    private UserClient userClient;

    @InjectMocks
    private MqttServiceImpl mqttService;

    private String deviceId;
    private UUID classroomId;
    private UUID studentId;

    @BeforeEach
    void setUp() {
        deviceId = "ESP32_001";
        classroomId = UUID.randomUUID();
        studentId = UUID.randomUUID();
    }

    @Test
    void togglePower_WhenActiveTrue_ShouldSendON() throws Exception {
        mqttService.togglePower(deviceId, true);

        ArgumentCaptor<MqttMessage> captor = ArgumentCaptor.forClass(MqttMessage.class);
        verify(mqttClient).publish(eq("device/change-status/" + deviceId), captor.capture());

        MqttMessage message = captor.getValue();
        assertEquals("ON", new String(message.getPayload()));
        assertEquals(1, message.getQos());
    }

    @Test
    void startEnrollment_WithIndex_ShouldSendEDIT() throws Exception {
        FingerprintRequestDTO dto = new FingerprintRequestDTO(5, classroomId, studentId, "John Doe");
        mqttService.startEnrollment(deviceId, dto);

        ArgumentCaptor<MqttMessage> captor = ArgumentCaptor.forClass(MqttMessage.class);
        verify(mqttClient).publish(eq("fingerprint/enroll/" + deviceId), captor.capture());

        MqttMessage message = captor.getValue();
        assertEquals("EDIT:5:John Doe", new String(message.getPayload()));
    }

    @Test
    void cancelEnrollment_ShouldSendCANCEL() throws Exception {
        mqttService.cancelEnrollment(deviceId);

        ArgumentCaptor<MqttMessage> captor = ArgumentCaptor.forClass(MqttMessage.class);
        verify(mqttClient).publish(eq("fingerprint/enroll/" + deviceId), captor.capture());

        MqttMessage message = captor.getValue();
        assertEquals("CANCEL", new String(message.getPayload()));
    }

    @Test
    void deleteFingerprint_ShouldSendJSON() throws Exception {
        mqttService.deleteFingerprint(deviceId, 3);

        ArgumentCaptor<MqttMessage> captor = ArgumentCaptor.forClass(MqttMessage.class);
        verify(mqttClient).publish(eq("fingerprint/delete/" + deviceId), captor.capture());

        MqttMessage message = captor.getValue();
        assertEquals("{\"index\":3}", new String(message.getPayload()));
    }

    @Test
    void clearAllFingerprints_ShouldSendALL() throws Exception {
        mqttService.clearAllFingerprints(deviceId);

        ArgumentCaptor<MqttMessage> captor = ArgumentCaptor.forClass(MqttMessage.class);
        verify(mqttClient).publish(eq("fingerprint/delete/" + deviceId), captor.capture());

        MqttMessage message = captor.getValue();
        assertEquals("{\"index\":\"ALL\"}", new String(message.getPayload()));
    }

    @Test
    void messageArrived_Discover_WhenNewDevice_ShouldSaveAndBroadcast() throws Exception {
        String payload = "{\"chipId\":\"ESP32_001\"}";
        MqttMessage message = new MqttMessage(payload.getBytes());

        JsonNode node = mock(JsonNode.class);
        JsonNode chipIdNode = mock(JsonNode.class);
        when(objectMapper.readTree(payload)).thenReturn(node);
        when(node.get("chipId")).thenReturn(chipIdNode);
        when(chipIdNode.asText()).thenReturn(deviceId);

        when(deviceService.existDeviceById(deviceId)).thenReturn(false);

        mqttService.messageArrived("device/discover", message);

        verify(deviceService).saveDevice(any(Device.class));
        verify(messagingTemplate).convertAndSend(eq("/topic/devices"), any(WebSocketEventDTO.class));
    }

    @Test
    void messageArrived_ChangeStatus_ShouldUpdateDeviceStatus() throws Exception {
        String payload = "{\"id\":\"ESP32_001\",\"isActive\":true}";
        MqttMessage message = new MqttMessage(payload.getBytes());

        DeviceStatusDTO dto = new DeviceStatusDTO(deviceId, true);
        when(objectMapper.readValue(payload, DeviceStatusDTO.class)).thenReturn(dto);
        when(deviceService.existDeviceById(deviceId)).thenReturn(true);

        mqttService.messageArrived("device/change-status", message);

        verify(deviceService).updateDeviceActiveStatus(deviceId, true, false);
    }

    @Test
    void messageArrived_TakeAttendance_ShouldRecordAndBroadcast() throws Exception {
        String payload = "{\"deviceId\":\"ESP32_001\",\"fingerprintIndex\":1}";
        MqttMessage message = new MqttMessage(payload.getBytes());

        AttendanceRequestDTO dto = new AttendanceRequestDTO(deviceId, 1);
        when(objectMapper.readValue(payload, AttendanceRequestDTO.class)).thenReturn(dto);

        Device device = Device.builder().id(deviceId).classroomId(classroomId).build();
        when(deviceService.getDeviceById(deviceId)).thenReturn(device);

        Fingerprint fingerprint = Fingerprint.builder().studentId(studentId).build();
        when(fingerprintService.getFingerprintByFingerprintIndexAndClassroomId(1, classroomId)).thenReturn(fingerprint);

        when(attendanceService.recordAttendance(studentId, classroomId)).thenReturn(
                AttendanceRecordResult.builder()
                        .status(AttendanceStatus.PRESENT)
                        .session(AttendanceSession.MORNING)
                        .isNew(true)
                        .build());

        UserResponseDTO user = new UserResponseDTO(studentId, "STU001", "John", "Doe");
        when(userClient.getUserById(studentId)).thenReturn(user);

        mqttService.messageArrived("attendance/take", message);

        verify(attendanceService).recordAttendance(studentId, classroomId);
        verify(messagingTemplate).convertAndSend(eq("/topic/attendances"), any(WebSocketEventDTO.class));
    }
}
