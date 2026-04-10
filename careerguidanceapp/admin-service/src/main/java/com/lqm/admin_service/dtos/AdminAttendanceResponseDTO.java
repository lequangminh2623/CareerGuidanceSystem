package com.lqm.admin_service.dtos;

import lombok.Builder;

import java.time.LocalTime;
import java.util.UUID;

@Builder
public record AdminAttendanceResponseDTO(
        UUID id,
        UUID studentId,
        LocalTime checkInTime,
        String status
) {}

