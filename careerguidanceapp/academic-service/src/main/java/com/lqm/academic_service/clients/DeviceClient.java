package com.lqm.academic_service.clients;

import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "api-gateway", path = "/attendance-service/api/internal/admin/devices", contextId = "deviceClient")
public interface DeviceClient {
    @PostMapping("/classrooms/{classroomId}/unassign")
    void unassignDeviceByClassroomId(@PathVariable UUID classroomId);
}
