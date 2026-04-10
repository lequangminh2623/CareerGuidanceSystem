package com.lqm.admin_service.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;

import java.util.UUID;

@Builder
public record DeviceRequestDTO(
                @NotBlank(message = "{device.id.notNull}") @Pattern(regexp = "^[0-9A-F]{12}$", message = "{device.id.invalid}") String id,

                @NotNull(message = "{device.classroomId.notNull}") UUID classroomId) {
}