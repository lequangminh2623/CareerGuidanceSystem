package com.lqm.admin_service.clients;

import com.lqm.admin_service.dtos.DeviceRequestDTO;
import com.lqm.admin_service.dtos.DeviceResponseDTO;

import jakarta.validation.Valid;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@FeignClient(name = "api-gateway", path = "/attendance-service/api/internal/admin/devices", contextId = "deviceClient")
public interface DeviceClient {

    @GetMapping
    Page<DeviceResponseDTO> getDevices(@RequestParam Map<String, String> params);

    @PatchMapping("/{id}")
    void updateDeviceActiveStatus(@PathVariable("id") String id, @RequestParam boolean active);

    @DeleteMapping("/{id}")
    void deleteDevice(@PathVariable("id") String id);

    @GetMapping("/available")
    List<DeviceResponseDTO> getAvailableDevices();

    @GetMapping("/classrooms/{classroomId}")
    DeviceResponseDTO getDeviceByClassroom(@PathVariable UUID classroomId);

    @PostMapping("/assignments")
    void assignDeviceToClassroom(@RequestBody @Valid DeviceRequestDTO assignDevice);

    @PostMapping("/{deviceId}/unassignment")
    void unassignDeviceFromClassroom(@PathVariable String deviceId);
}
