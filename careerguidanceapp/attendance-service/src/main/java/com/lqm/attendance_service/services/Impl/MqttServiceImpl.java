package com.lqm.attendance_service.services.Impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lqm.attendance_service.dtos.AttendanceRequestDTO;
import com.lqm.attendance_service.dtos.DeviceStatusDTO;
import com.lqm.attendance_service.dtos.FingerprintRequestDTO;
import com.lqm.attendance_service.dtos.UserResponseDTO;
import com.lqm.attendance_service.dtos.WebSocketEventDTO;
import com.lqm.attendance_service.mappers.FingerprintMapper;
import com.lqm.attendance_service.models.Device;
import com.lqm.attendance_service.models.Fingerprint;
import com.lqm.attendance_service.services.*;
import com.lqm.attendance_service.clients.UserClient;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class MqttServiceImpl implements MqttService, MqttCallback {

    private final IMqttClient mqttClient;
    private final ObjectMapper objectMapper;
    private final DeviceService deviceService;
    private final FingerprintService fingerprintService;
    private final AttendanceService attendanceService;
    private final FingerprintMapper fingerprintMapper;
    private final RestTemplate restTemplate;
    private final SimpMessagingTemplate messagingTemplate;
    private final UserClient userClient;

    @Value("${admin.service.url}")
    private String adminServiceUrl;

    private final Map<String, FingerprintRequestDTO> pendingEnrollments = new ConcurrentHashMap<>();

    private static final String TOPIC_DISCOVER_DEVICE = "device/discover";
    private static final String TOPIC_ENROLL_FINGERPRINT = "fingerprint/enroll";
    private static final String TOPIC_TAKE_ATTENDANCE = "attendance/take";
    private static final String TOPIC_CHANGE_DEVICE_STATUS = "device/change-status";
    private static final String TOPIC_DELETE_FINGERPRINT = "fingerprint/delete";

    public MqttServiceImpl(@Lazy DeviceService deviceService,
            IMqttClient mqttClient,
            ObjectMapper objectMapper,
            @Lazy FingerprintService fingerprintService,
            @Lazy AttendanceService attendanceService,
            FingerprintMapper fingerprintMapper,
            RestTemplate restTemplate,
            SimpMessagingTemplate messagingTemplate,
            UserClient userClient) {
        this.deviceService = deviceService;
        this.mqttClient = mqttClient;
        this.objectMapper = objectMapper;
        this.fingerprintService = fingerprintService;
        this.attendanceService = attendanceService;
        this.fingerprintMapper = fingerprintMapper;
        this.restTemplate = restTemplate;
        this.messagingTemplate = messagingTemplate;
        this.userClient = userClient;
    }

    @PostConstruct
    public void init() {
        try {
            mqttClient.setCallback(this);
            mqttClient.subscribe(TOPIC_DISCOVER_DEVICE);
            mqttClient.subscribe(TOPIC_ENROLL_FINGERPRINT);
            mqttClient.subscribe(TOPIC_TAKE_ATTENDANCE);
            mqttClient.subscribe(TOPIC_CHANGE_DEVICE_STATUS);
            log.info("MqttService đã khởi tạo và subscribe các topic.");
        } catch (Exception e) {
            log.error("Lỗi khi subscribe MQTT: {}", e.getMessage());
        }
    }

    @Override
    public void connectionLost(Throwable cause) {
        log.warn("Mất kết nối MQTT: {}", cause.getMessage());
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
    }

    @Override
    public void togglePower(String deviceId, Boolean active) {
        try {
            String payload = active ? "ON" : "OFF";
            MqttMessage message = new MqttMessage(payload.getBytes());
            message.setQos(1);
            mqttClient.publish(TOPIC_CHANGE_DEVICE_STATUS + "/" + deviceId, message);
            log.info("Đã gửi lệnh tới {}: {}", deviceId, payload);
        } catch (Exception e) {
            log.error("Lỗi khi gửi MQTT: {}", e.getMessage());
        }
    }

    @Override
    public void startEnrollment(String deviceId, FingerprintRequestDTO dto) {
        try {
            pendingEnrollments.put(deviceId, dto);
            String name = dto.studentName() != null ? dto.studentName() : "Unknown";
            String shortName = name.length() > 16 ? name.substring(0, 16) : name;
            String payload = (dto.fingerprintIndex() != null) ? "EDIT:" + dto.fingerprintIndex() + ":" + shortName
                    : "ADD:" + shortName;
            MqttMessage message = new MqttMessage(payload.getBytes());
            message.setQos(1);
            mqttClient.publish(TOPIC_ENROLL_FINGERPRINT + "/" + deviceId, message);
            log.info("Đã yêu cầu thiết bị {} bắt đầu đăng ký vân tay với lệnh: {}", deviceId, payload);
        } catch (Exception e) {
            log.error("Lỗi khi yêu cầu đăng ký vân tay qua MQTT: {}", e.getMessage());
            pendingEnrollments.remove(deviceId);
        }
    }

    @Override
    public void cancelEnrollment(String deviceId) {
        try {
            pendingEnrollments.remove(deviceId);
            String payload = "CANCEL";
            MqttMessage message = new MqttMessage(payload.getBytes());
            message.setQos(1);
            mqttClient.publish(TOPIC_ENROLL_FINGERPRINT + "/" + deviceId, message);
            log.info("Đã yêu cầu thiết bị {} hủy đăng ký vân tay với lệnh: {}", deviceId, payload);
        } catch (Exception e) {
            log.error("Lỗi khi yêu cầu hủy đăng ký vân tay qua MQTT: {}", e.getMessage());
        }
    }

    @Override
    public void deleteFingerprint(String deviceId, Integer fingerprintIndex) {
        try {
            String payload = "{\"index\":" + fingerprintIndex + "}";
            MqttMessage message = new MqttMessage(payload.getBytes());
            message.setQos(1);
            mqttClient.publish(TOPIC_DELETE_FINGERPRINT + "/" + deviceId, message);
            log.info("Đã gửi lệnh xóa vân tay index {} trên thiết bị {}", fingerprintIndex, deviceId);
        } catch (Exception e) {
            log.error("Lỗi khi gửi lệnh xóa vân tay qua MQTT: {}", e.getMessage());
        }
    }

    @Override
    public void clearAllFingerprints(String deviceId) {
        try {
            String payload = "{\"index\":\"ALL\"}";
            MqttMessage message = new MqttMessage(payload.getBytes());
            message.setQos(1);
            mqttClient.publish(TOPIC_DELETE_FINGERPRINT + "/" + deviceId, message);
            log.info("Đã gửi lệnh xóa TOÀN BỘ vân tay (format thẻ nhớ R307) trên thiết bị {}", deviceId);
        } catch (Exception e) {
            log.error("Lỗi khi gửi lệnh xóa toàn bộ vân tay qua MQTT: {}", e.getMessage());
        }
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) {
        String payload = new String(message.getPayload());
        log.debug("Nhận MQTT message từ {}: {}", topic, payload);

        try {
            switch (topic) {
                case TOPIC_DISCOVER_DEVICE:
                    handleDiscover(payload);
                    break;
                case TOPIC_ENROLL_FINGERPRINT:
                    handleEnrollResponse(payload);
                    break;
                case TOPIC_TAKE_ATTENDANCE:
                    handleAttendance(payload);
                    break;
                case TOPIC_CHANGE_DEVICE_STATUS:
                    handleChangeStatus(payload);
                    break;
            }
        } catch (Exception e) {
            log.error("Lỗi khi xử lý MQTT message từ {}: {}", topic, e.getMessage());
        }
    }

    private void handleDiscover(String payload) throws Exception {
        JsonNode node = objectMapper.readTree(payload);
        String chipId = node.get("chipId").asText();

        log.info("Nhận yêu cầu discover thiết bị qua MQTT: {}", chipId);
        boolean isNew = false;
        if (!deviceService.existDeviceById(chipId)) {
            log.info("Phát hiện thiết bị mới hoàn toàn qua MQTT: {}", chipId);
            deviceService.saveDevice(Device.builder().id(chipId).isActive(true).build());
            isNew = true;
        } else {
            log.info("Thiết bị {} đã từng được gán trước đó (qua MQTT discover).", chipId);
        }

        // Broadcast WebSocket event cho device discover
        WebSocketEventDTO<Map<String, Object>> event = WebSocketEventDTO.<Map<String, Object>>builder()
                .eventType("DEVICE_DISCOVERED")
                .data(Map.of(
                        "chipId", chipId,
                        "isActive", true,
                        "isNew", isNew))
                .timestamp(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")))
                .build();
        messagingTemplate.convertAndSend("/topic/devices", event);
        log.info("Đã broadcast WebSocket event DEVICE_DISCOVERED cho chipId: {}", chipId);
    }

    private void handleChangeStatus(String payload) throws Exception {
        DeviceStatusDTO dto = objectMapper.readValue(payload, DeviceStatusDTO.class);

        if (dto != null && deviceService.existDeviceById(dto.id())) {
            deviceService.updateDeviceActiveStatus(dto.id(), dto.isActive(), false);
        } else {
            log.warn("Nhận MQTT status nhưng payload không hợp lệ: {}", payload);
        }
    }

    private void handleEnrollResponse(String payload) throws Exception {
        JsonNode node = objectMapper.readTree(payload);
        String deviceId = node.get("id").asText();
        int fingerprintIndex = node.get("index").asInt();

        if (fingerprintIndex < 1 || fingerprintIndex > 255) {
            log.warn("Nhận kết quả đăng ký từ thiết bị nhưng fingerprint index không hợp lệ: {}", payload);
            FingerprintRequestDTO dto = pendingEnrollments.remove(deviceId);
            if (dto != null) {
                notifyEnrollResult(dto.classroomId(), false, "Đăng ký vân tay thất bại. Vui lòng thử lại.");
            }
        } else {
            FingerprintRequestDTO dto = pendingEnrollments.remove(deviceId);
            if (dto != null) {
                Fingerprint fingerprint = fingerprintMapper.toEntity(dto);
                fingerprint.setFingerprintIndex(fingerprintIndex);
                fingerprintService.saveFingerprint(fingerprint);

                log.info("Đã lưu vân tay cho học sinh {} tại lớp {} với chỉ mục {}",
                        dto.studentId(), dto.classroomId(), fingerprintIndex);
                notifyEnrollResult(dto.classroomId(), true, "Đăng ký vân tay thành công!");
            }
        }
    }

    private void notifyEnrollResult(UUID classroomId, boolean success, String message) {
        try {
            String url = adminServiceUrl + "/internal/enroll-result/" + classroomId;
            Map<String, Object> body = Map.of(
                    "success", success,
                    "message", message);
            ResponseEntity<Void> response = restTemplate.postForEntity(url, body, Void.class);
            log.info("Đã gửi enroll result callback về admin-service cho classroom {}: success={}, status={}",
                    classroomId, success, response.getStatusCode());
        } catch (Exception e) {
            log.error("Lỗi khi gửi enroll result callback về admin-service: {}", e.getMessage());
        }
    }

    private void handleAttendance(String payload) throws Exception {
        AttendanceRequestDTO dto = objectMapper.readValue(payload, AttendanceRequestDTO.class);
        Device device = deviceService.getDeviceById(dto.deviceId());

        if (device.getClassroomId() != null) {
            Fingerprint fingerprint = fingerprintService.getFingerprintByFingerprintIndexAndClassroomId(
                    dto.fingerprintIndex(), device.getClassroomId());

            var status = attendanceService.recordAttendance(fingerprint.getStudentId(), device.getClassroomId());
            log.info("Ghi nhận điểm danh cho học sinh {} tại lớp {} từ thiết bị {} - Status: {}",
                    fingerprint.getStudentId(), device.getClassroomId(), device.getId(), status);

            // Lấy thông tin học sinh để broadcast đầy đủ (cho UI update)
            String studentName = "N/A";
            String studentCode = "N/A";
            try {
                UserResponseDTO user = userClient.getUserById(fingerprint.getStudentId());
                if (user != null) {
                    studentName = user.lastName() + " " + user.firstName();
                    studentCode = user.code();
                }
            } catch (Exception e) {
                log.error("Không thể lấy thông tin học sinh từ user-service: {}", e.getMessage());
            }

            // Broadcast WebSocket event cho attendance
            LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
            WebSocketEventDTO<Map<String, Object>> event = WebSocketEventDTO.<Map<String, Object>>builder()
                    .eventType("ATTENDANCE_RECORDED")
                    .data(Map.of(
                            "studentId", fingerprint.getStudentId().toString(),
                            "studentName", studentName,
                            "studentCode", studentCode,
                            "status", status.toString(),
                            "classroomId", device.getClassroomId().toString(),
                            "deviceId", device.getId(),
                            "checkInTime", now.toLocalTime().toString().substring(0, 8),
                            "attendanceDate", now.toLocalDate().toString()))
                    .timestamp(now)
                    .build();
            messagingTemplate.convertAndSend("/topic/attendances", event);
            log.info("Đã broadcast WebSocket event ATTENDANCE_RECORDED cho student: {}, classroom: {}",
                    fingerprint.getStudentId(), device.getClassroomId());
        } else {
            log.warn("Nhận log điểm danh từ thiết bị {} nhưng thiết bị chưa được gán cho lớp nào.", device.getId());
        }
    }

}
