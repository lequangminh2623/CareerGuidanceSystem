package com.lqm.attendance_service.controllers;

import com.lqm.attendance_service.dtos.AttendanceConfigDTO;
import com.lqm.attendance_service.models.AttendanceConfig;
import com.lqm.attendance_service.services.AttendanceConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/internal/admin/attendance-config")
public class AdminAttendanceConfigController {

    private final AttendanceConfigService configService;

    @GetMapping
    public AttendanceConfigDTO getConfig() {
        AttendanceConfig config = configService.getConfig();
        return toDTO(config);
    }

    @PutMapping
    public AttendanceConfigDTO updateConfig(@RequestBody AttendanceConfigDTO dto) {
        AttendanceConfig config = configService.updateConfig(dto);
        return toDTO(config);
    }

    private AttendanceConfigDTO toDTO(AttendanceConfig config) {
        return AttendanceConfigDTO.builder()
                .sessionsPerDay(config.getSessionsPerDay())
                .morningStartTime(config.getMorningStartTime())
                .morningEndTime(config.getMorningEndTime())
                .afternoonStartTime(config.getAfternoonStartTime())
                .afternoonEndTime(config.getAfternoonEndTime())
                .build();
    }
}
