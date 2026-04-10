package com.lqm.attendance_service.controllers;

import com.lqm.attendance_service.dtos.FingerprintRequestDTO;
import com.lqm.attendance_service.exceptions.BadRequestException;
import com.lqm.attendance_service.models.Device;
import com.lqm.attendance_service.models.Fingerprint;
import com.lqm.attendance_service.repositories.FingerprintRepository;
import com.lqm.attendance_service.services.DeviceService;
import com.lqm.attendance_service.services.MqttService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.context.MessageSource;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/internal/admin/fingerprints")
public class AdminFingerprintController {

    private final MqttService mqttService;
    private final DeviceService deviceService;
    private final FingerprintRepository fingerprintRepository;
    private final MessageSource messageSource;

    @PostMapping("/enroll")
    public void enrollFingerprint(@RequestBody @Valid FingerprintRequestDTO request) {
        Device device = deviceService.getDeviceByClassroom(request.classroomId());
        if (device != null && device.getIsActive()) {
            Integer index = fingerprintRepository
                    .findByStudentIdAndClassroomId(request.studentId(), request.classroomId())
                    .map(Fingerprint::getFingerprintIndex)
                    .orElse(null);
            FingerprintRequestDTO dto = FingerprintRequestDTO.builder()
                    .fingerprintIndex(index)
                    .classroomId(request.classroomId())
                    .studentId(request.studentId())
                    .studentName(request.studentName())
                    .build();

            mqttService.startEnrollment(device.getId(), dto);
        } else {
            throw new BadRequestException(
                    messageSource.getMessage("device.deactive", null, Locale.getDefault()));
        }
    }

    @PostMapping("/cancel")
    void cancelEnrollment(@RequestBody UUID classroomId) {
        Device device = deviceService.getDeviceByClassroom(classroomId);
        if (device != null && device.getIsActive()) {
            mqttService.cancelEnrollment(device.getId());
        }
    }
}
