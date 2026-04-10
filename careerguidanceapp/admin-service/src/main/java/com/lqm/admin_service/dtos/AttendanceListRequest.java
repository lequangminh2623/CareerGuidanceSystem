package com.lqm.admin_service.dtos;

import jakarta.validation.Valid;
import lombok.Builder;

import java.util.List;

@Builder
public record AttendanceListRequest(
        @Valid
        List<AdminAttendanceRequestDTO> attendances
) {}

