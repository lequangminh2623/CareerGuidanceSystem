package com.lqm.attendance_service.dtos;

import lombok.Builder;

@Builder
public record DeviceResponseDTO(
        String id,
        Boolean isActive,
        String classroomName

) {}
