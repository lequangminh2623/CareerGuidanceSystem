package com.lqm.attendance_service.controllers;

import com.lqm.attendance_service.dtos.DeviceRequestDTO;
import com.lqm.attendance_service.dtos.DeviceResponseDTO;
import com.lqm.attendance_service.dtos.DeviceStatusDTO;
import com.lqm.attendance_service.mappers.DeviceMapper;
import com.lqm.attendance_service.models.Device;
import com.lqm.attendance_service.services.DeviceService;
import com.lqm.attendance_service.utils.PageSize;
import com.lqm.attendance_service.utils.PageableUtil;
import com.lqm.attendance_service.validators.WebAppValidator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/internal/admin/devices")
public class AdminDeviceController {
    private final DeviceService deviceService;
    private final PageableUtil pageableUtil;
    private final DeviceMapper deviceMapper;
    private final WebAppValidator webAppValidator;

    @InitBinder("assignDevice")
    public void initBinder(WebDataBinder binder) {
        binder.setValidator(webAppValidator);
    }

    // Hiển thị danh sách devices
    @GetMapping
    public Page<DeviceResponseDTO> listDevices(@RequestParam Map<String, String> params) {
        Pageable pageable = pageableUtil.getPageable(
                params.getOrDefault("page", "1"),
                PageSize.DEVICE_PAGE_SIZE,
                List.of()
        );
        Page<Device> devices = deviceService.getAllDevices(params, pageable);
        Map<UUID, String> classroomMap = deviceService.buildClassroomMap(devices.getContent());

        return devices.map(d -> deviceMapper.toDeviceResponseDTO(d, classroomMap));
    }

    // Toggle active status
    @PatchMapping("/{id}")
    public void updateDeviceActiveStatus(
            @PathVariable String id,
            @RequestParam boolean active) {
        deviceService.updateDeviceActiveStatus(id, active, true);
    }

    // Xóa device
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDevice(@PathVariable String id) {
        deviceService.deleteDevice(id);
    }

    // Lấy danh sách device chưa được gán
    @GetMapping("/available")
    public List<DeviceStatusDTO> getAvailableDevices() {
        return deviceService.getDevicesWithoutClassroom().stream()
                .map(deviceMapper::toDeviceStatusDTO)
                .toList();
    }

    // Lấy device theo classroom
    @GetMapping("/classrooms/{classroomId}")
    public DeviceStatusDTO getDeviceByClassroom(@PathVariable UUID classroomId) {
        Device device = deviceService.getDeviceByClassroom(classroomId);
        return deviceMapper.toDeviceStatusDTO(device);
    }

    // Gán device vào lớp
    @PostMapping("/assign")
    public void assignDeviceToClassroom(@RequestBody @Valid DeviceRequestDTO assignDevice) {
        deviceService.assignDeviceToClassroom(deviceMapper.toEntity(assignDevice));
    }

    // Bỏ gán device từ lớp
    @PostMapping("/{deviceId}/unassign")
    public void unassignDeviceFromClassroom(@PathVariable String deviceId) {
        deviceService.unassignDeviceFromClassroom(deviceId);
    }
}
