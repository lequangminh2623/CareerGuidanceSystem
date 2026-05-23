package com.lqm.admin_service.clients;

import com.lqm.admin_service.dtos.AttendanceConfigDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "api-gateway", path = "/attendance-service/api/internal/admin/attendance-config", contextId = "attendanceConfigClient")
public interface AttendanceConfigClient {

    @GetMapping
    AttendanceConfigDTO getConfig();

    @PutMapping
    AttendanceConfigDTO updateConfig(@RequestBody AttendanceConfigDTO dto);
}
