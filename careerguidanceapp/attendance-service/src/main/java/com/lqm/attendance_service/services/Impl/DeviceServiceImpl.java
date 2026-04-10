package com.lqm.attendance_service.services.Impl;

import com.lqm.attendance_service.clients.ClassroomClient;
import com.lqm.attendance_service.dtos.AcademicResponseDTO;
import com.lqm.attendance_service.exceptions.ResourceNotFoundException;
import com.lqm.attendance_service.models.Device;
import com.lqm.attendance_service.repositories.DeviceRepository;
import com.lqm.attendance_service.services.DeviceService;
import com.lqm.attendance_service.services.MqttService;
import com.lqm.attendance_service.specifications.DeviceSpecification;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import com.lqm.attendance_service.services.FingerprintService;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class DeviceServiceImpl implements DeviceService {
    private final DeviceRepository deviceRepository;
    private final MqttService mqttService;
    private final ClassroomClient classroomClient;
    private final MessageSource messageSource;

    @Autowired
    @Lazy
    private FingerprintService fingerprintService;

    @Override
    public Device getDeviceById(String deviceId) {
        return deviceRepository.findById(deviceId).orElseThrow(
                () -> new ResourceNotFoundException(
                        messageSource.getMessage("device.notFound", null, Locale.getDefault())));
    }

    @Override
    public Page<Device> getAllDevices(Map<String, String> params, Pageable pageable) {
        Specification<Device> spec = DeviceSpecification.filterByParams(params);

        return deviceRepository.findAll(spec, pageable);
    }

    @Override
    public Device saveDevice(Device device) {
        return deviceRepository.save(device);
    }

    @Override
    public void updateDeviceActiveStatus(String id, boolean active, boolean notifyMqtt) {
        Device device = this.getDeviceById(id);

        device.setIsActive(active);
        deviceRepository.save(device);
        
        if (notifyMqtt) {
            mqttService.togglePower(device.getId(), active);
        }

        log.info("Thiết bị {}: Trạng thái đổi thành {}, notifyMqtt={}", id, active, notifyMqtt);
    }

    @Override
    public void deleteDevice(String deviceId) {
        if (!deviceRepository.existsById(deviceId)) {
            throw new ResourceNotFoundException(
                    messageSource.getMessage("device.notFound", null, Locale.getDefault()));
        }
        
        Device device = this.getDeviceById(deviceId);
        UUID classroomId = device.getClassroomId();
        
        if (classroomId != null) {
            fingerprintService.deleteFingerprintsByClassroomId(classroomId);
            if (Boolean.TRUE.equals(device.getIsActive())) {
                mqttService.clearAllFingerprints(deviceId);
            }
        }
        
        deviceRepository.deleteById(deviceId);
    }

    @Override
    public List<Device> getDevicesWithoutClassroom() {
        return deviceRepository.findAll().stream()
                .filter(device -> device.getClassroomId() == null)
                .toList();
    }

    @Override
    public Device getDeviceByClassroom(UUID classroomId) {
        return deviceRepository.findByClassroomId(classroomId).orElse(null);
    }

    @Override
    public Device assignDeviceToClassroom(Device device) {
        Device dbDevice = this.getDeviceById(device.getId());
        dbDevice.setClassroomId(device.getClassroomId());
        return deviceRepository.save(dbDevice);
    }

    @Override
    public void unassignDeviceFromClassroom(String deviceId) {
        Device device = this.getDeviceById(deviceId);
        
        UUID classroomId = device.getClassroomId();
        if (classroomId != null) {
            fingerprintService.deleteFingerprintsByClassroomId(classroomId);
            if (Boolean.TRUE.equals(device.getIsActive())) {
                mqttService.clearAllFingerprints(deviceId);
            }
        }
        
        device.setClassroomId(null);
        deviceRepository.save(device);
    }

    @Override
    public Map<UUID, String> buildClassroomMap(List<Device> devices) {
        // 1. Lấy tất cả classroomId duy nhất từ danh sách devices
        List<UUID> classroomIds = devices.stream()
                .map(Device::getClassroomId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (classroomIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<AcademicResponseDTO> classroomDetails = classroomClient.getClassroomDetailNames(classroomIds,
                Map.of("page", ""));

        // 3. Chuyển List thành Map để Mapper sử dụng: Map<ID, "Tên Lớp - Khối - Năm">
        return classroomDetails.stream()
                .collect(Collectors.toMap(
                        AcademicResponseDTO::id,
                        AcademicResponseDTO::name));
    }

    @Override
    public boolean existDeviceById(String id) {
        return deviceRepository.existsById(id);
    }

    @Override
    public boolean existDeviceByClassroomId(UUID classroomId) {
        return deviceRepository.existsByClassroomId(classroomId);
    }
}
