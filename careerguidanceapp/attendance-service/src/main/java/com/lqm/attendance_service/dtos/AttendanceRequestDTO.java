package com.lqm.attendance_service.dtos;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;

@Builder
public record AttendanceRequestDTO(
                @NotBlank(message = "{device.id.notNull}") @Pattern(regexp = "^[0-9A-F]{12}$", message = "{device.id.invalid}") String deviceId,

                @NotNull(message = "{fingerprint.index.notNull}") @Min(value = 1, message = "{fingerprint.index.invalid}") @Max(value = 255, message = "{fingerprint.index.invalid}") Integer fingerprintIndex) {
}
