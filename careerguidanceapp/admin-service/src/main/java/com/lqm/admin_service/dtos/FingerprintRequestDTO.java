package com.lqm.admin_service.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.UUID;

@Builder
public record FingerprintRequestDTO(
                Integer fingerprintIndex,
                @NotNull(message = "{fingerprint.classroomId.notNull}") UUID classroomId,
                @NotNull(message = "{fingerprint.studentId.notNull}") UUID studentId,
                @NotNull(message = "{fingerprint.studentName.notNull}") String studentName) {
}