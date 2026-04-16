package com.lqm.attendance_service.services.Impl;

import com.lqm.attendance_service.exceptions.ResourceNotFoundException;
import com.lqm.attendance_service.models.Device;
import com.lqm.attendance_service.models.Fingerprint;
import com.lqm.attendance_service.repositories.FingerprintRepository;
import com.lqm.attendance_service.services.DeviceService;
import com.lqm.attendance_service.services.FingerprintService;
import com.lqm.attendance_service.services.MqttService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Slf4j
@Service
@RequiredArgsConstructor
public class FingerprintServiceImpl implements FingerprintService {

    private final FingerprintRepository fingerprintRepo;
    private final MessageSource messageSource;
    private final MqttService mqttService;
    private final DeviceService deviceService;

    @Override
    public Fingerprint getFingerprintByFingerprintIndexAndClassroomId(Integer fingerprintIndex, UUID classroomId) {
        return fingerprintRepo.findByFingerprintIndexAndClassroomId(fingerprintIndex, classroomId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        messageSource.getMessage("fingerprint.notfound", null, Locale.getDefault())));
    }

    @Override
    public Fingerprint saveFingerprint(Fingerprint fingerprint) {
        return fingerprintRepo.save(fingerprint);
    }

    @Override
    public void deleteFingerprintsByClassroomAndStudentIds(UUID classroomId, List<UUID> studentIds) {
        // Lấy danh sách vân tay trước khi xóa để biết index cần xóa trên ESP32
        List<Fingerprint> toDelete = fingerprintRepo.findByClassroomIdAndStudentIdIn(classroomId, studentIds);

        // Gửi lệnh MQTT đến thiết bị để xóa vân tay khỏi bộ nhớ vật lý
        if (!toDelete.isEmpty()) {
            try {
                Device device = deviceService.getDeviceByClassroom(classroomId);
                if (device != null && device.getIsActive()) {
                    for (Fingerprint fp : toDelete) {
                        if (fp.getFingerprintIndex() != null) {
                            mqttService.deleteFingerprint(device.getId(), fp.getFingerprintIndex());
                        }
                    }
                } else {
                    log.warn("Không tìm thấy thiết bị active cho lớp {} -- bỏ qua việc xóa vân tay trên ESP32",
                            classroomId);
                }
            } catch (Exception e) {
                log.warn("Lỗi khi gửi MQTT xóa vân tay: {} -- tiếp tục xóa trong DB", e.getMessage());
            }
        }

        // Xóa trong DB
        fingerprintRepo.deleteByClassroomIdAndStudentIdIn(classroomId, studentIds);
    }

    @Override
    public void deleteFingerprintsByClassroomId(UUID classroomId) {
        // Gửi lệnh MQTT đến thiết bị để xóa vân tay khỏi bộ nhớ vật lý
        try {
            Device device = deviceService.getDeviceByClassroom(classroomId);
            if (device != null && device.getIsActive()) {
                mqttService.clearAllFingerprints(device.getId());
            } else {
                log.warn("Không tìm thấy thiết bị active cho lớp {} -- bỏ qua việc xóa vân tay trên ESP32",
                        classroomId);
            }
        } catch (Exception e) {
            log.warn("Lỗi khi gửi MQTT xóa vân tay cho lớp {}: {} -- tiếp tục xóa trong DB", classroomId, e.getMessage());
        }

        fingerprintRepo.deleteByClassroomId(classroomId);
        log.info("Đã xóa toàn bộ vân tay trong DB đối với lớp: {}", classroomId);
    }
}
