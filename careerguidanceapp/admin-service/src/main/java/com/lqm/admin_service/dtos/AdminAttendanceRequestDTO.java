package com.lqm.admin_service.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.UUID;

import com.lqm.admin_service.annotations.EnumValue;
import com.lqm.admin_service.models.AttendanceStatus;

@Builder
public record AdminAttendanceRequestDTO(
                @NotNull(message = "{attendance.studentId.notNull}") UUID studentId,

                @NotNull(message = "{attendance.status.notNull}") @EnumValue(enumClass = AttendanceStatus.class, message = "{attendance.status.invalid}") String status) {
}
