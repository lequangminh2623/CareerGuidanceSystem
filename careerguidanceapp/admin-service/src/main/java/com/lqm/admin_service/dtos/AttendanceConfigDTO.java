package com.lqm.admin_service.dtos;

import lombok.Builder;

import java.time.LocalTime;

@Builder
public record AttendanceConfigDTO(
        int sessionsPerDay,
        LocalTime morningStartTime,
        LocalTime morningEndTime,
        LocalTime afternoonStartTime,
        LocalTime afternoonEndTime) {
}
