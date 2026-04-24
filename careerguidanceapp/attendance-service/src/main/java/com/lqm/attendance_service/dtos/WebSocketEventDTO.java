package com.lqm.attendance_service.dtos;

import lombok.Builder;

import java.time.LocalDateTime;

/**
 * DTO generic cho WebSocket event broadcast.
 * @param eventType Loại sự kiện: DEVICE_DISCOVERED, ATTENDANCE_RECORDED
 * @param data      Dữ liệu chi tiết của sự kiện
 * @param timestamp Thời điểm phát sinh sự kiện
 */
@Builder
public record WebSocketEventDTO<T>(
        String eventType,
        T data,
        LocalDateTime timestamp) {
}
