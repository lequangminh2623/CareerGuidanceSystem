package com.lqm.admin_service.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Controller xử lý SSE cho kết quả đăng ký vân tay.
 * Browser subscribe SSE, attendance-service gọi callback khi MQTT enroll hoàn thành.
 */
@RestController
@Slf4j
public class EnrollSseController {

    // Key: classroomId, Value: SseEmitter đang chờ kết quả
    private final Map<UUID, SseEmitter> emitters = new ConcurrentHashMap<>();

    /**
     * Browser subscribe SSE để nhận kết quả enroll.
     * Timeout 5 phút (đủ thời gian học sinh đặt tay lên cảm biến).
     */
    @GetMapping(value = "/classrooms/{classroomId}/fingerprints/enroll/sse",
                produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribeEnrollResult(@PathVariable UUID classroomId) {
        SseEmitter emitter = new SseEmitter(5 * 60 * 1000L); // 5 phút

        // Xóa emitter cũ nếu có (tránh leak)
        SseEmitter old = emitters.put(classroomId, emitter);
        if (old != null) {
            old.complete();
        }

        emitter.onCompletion(() -> emitters.remove(classroomId, emitter));
        emitter.onTimeout(() -> {
            emitters.remove(classroomId, emitter);
            emitter.complete();
        });

        log.info("Browser subscribed SSE cho classroom {}", classroomId);
        return emitter;
    }

    /**
     * Attendance-service gọi endpoint này sau khi xử lý MQTT enroll.
     * Body: {"success": true/false, "message": "..."}
     */
    @PostMapping("/internal/enroll-result/{classroomId}")
    public void receiveEnrollResult(
            @PathVariable UUID classroomId,
            @RequestBody Map<String, Object> result) {

        SseEmitter emitter = emitters.remove(classroomId);
        if (emitter == null) {
            log.warn("Nhận enroll result cho classroom {} nhưng không có browser đang chờ.", classroomId);
            return;
        }

        try {
            emitter.send(SseEmitter.event()
                    .name("enroll-result")
                    .data(result));
            emitter.complete();
            log.info("Đã push SSE enroll result cho classroom {}: {}", classroomId, result);
        } catch (IOException e) {
            log.error("Lỗi khi push SSE cho classroom {}: {}", classroomId, e.getMessage());
            emitter.completeWithError(e);
        }
    }
}
