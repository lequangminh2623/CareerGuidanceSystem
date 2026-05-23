package com.lqm.attendance_service.dtos;

import com.lqm.attendance_service.models.AttendanceSession;
import com.lqm.attendance_service.models.AttendanceStatus;
import lombok.Builder;

/**
 * Kết quả ghi nhận điểm danh.
 * @param status Trạng thái điểm danh (PRESENT, LATE, ABSENT)
 * @param session Buổi điểm danh (MORNING, AFTERNOON)
 * @param isNew true nếu đây là lần điểm danh đầu tiên trong buổi này
 */
@Builder
public record AttendanceRecordResult(
        AttendanceStatus status,
        AttendanceSession session,
        boolean isNew) {
}
