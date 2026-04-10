package com.lqm.attendance_service.dtos;

import jakarta.validation.Valid;
import lombok.Builder;

import java.util.List;

@Builder
public record AttendanceListRequestDTO(
        @Valid
        List<AdminAttendanceRequestDTO> attendances
) {}
