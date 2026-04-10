package com.lqm.attendance_service.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;

@Builder
public record DeviceStatusDTO(
        @NotBlank(message = "{device.id.notNull}") @Pattern(regexp = "^[0-9A-F]{12}$", message = "{device.id.invalid}") String id,

        @NotNull(message = "{device.status.notNull}") Boolean isActive) {
}
