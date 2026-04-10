package com.lqm.attendance_service.dtos;

import lombok.Builder;

@Builder
public record AttendanceSummaryDTO(
        long presentCount,
        long lateCount,
        long absentCount
) {}
