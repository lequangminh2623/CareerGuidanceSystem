package com.lqm.attendance_service.dtos;

import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Builder
public record AttendanceResponseDTO(
                UUID id,
                UUID studentId,
                LocalDate attendanceDate,
                LocalTime checkInTime,
                String status) {
}
