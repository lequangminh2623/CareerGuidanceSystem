package com.lqm.attendance_service.services.Impl;

import com.lqm.attendance_service.configs.RedisConfig;
import com.lqm.attendance_service.dtos.AbsentQueueMessage;
import com.lqm.attendance_service.models.Attendance;
import com.lqm.attendance_service.models.AttendanceStatus;
import com.lqm.attendance_service.repositories.AttendanceRepository;
import com.lqm.attendance_service.services.RedisAttendanceWorker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class RedisAttendanceWorkerImpl implements RedisAttendanceWorker {

    private final RedisTemplate<String, Object> redisTemplate;
    private final AttendanceRepository attendanceRepo;

    @Override
    @Async
    @Scheduled(fixedDelay = 5000) // Kiểm tra hàng đợi mỗi 5 giây
    public void processAbsentQueue() {
        Object data = redisTemplate.opsForList().rightPop(RedisConfig.ABSENT_QUEUE, Duration.ofSeconds(1));
        if (data == null) return;

        try {
            if (data instanceof AbsentQueueMessage msg) {
                processMessage(msg.getStudentIds(), UUID.fromString(msg.getClassroomId()), LocalDate.parse(msg.getDate()));
            } else if (data instanceof Map<?, ?> map) {
                Object studentIdsData = map.get("studentIds");
                UUID classroomId = UUID.fromString((String) map.get("classroomId"));
                LocalDate date = LocalDate.parse((String) map.get("date"));
                if (studentIdsData instanceof java.util.List<?> studentIdStrings) {
                    processMessage(studentIdStrings.stream().map(Object::toString).toList(), classroomId, date);
                }
            }
        } catch (Exception e) {
            log.error("Worker: Lỗi xử lý message từ Redis: {}", e.getMessage());
        }
    }

    private void processMessage(java.util.List<String> studentIdStrings, UUID classroomId, LocalDate date) {
        java.util.List<Attendance> absentAttendances = new java.util.ArrayList<>();
        for (String idStr : studentIdStrings) {
            UUID studentId = UUID.fromString(idStr);
            absentAttendances.add(Attendance.builder()
                    .studentId(studentId)
                    .classroomId(classroomId)
                    .attendanceDate(date)
                    .status(AttendanceStatus.ABSENT)
                    .build());
        }

        if (!absentAttendances.isEmpty()) {
            attendanceRepo.saveAll(absentAttendances);
            log.info("Worker: Ghi nhận ABSENT cho {} học sinh tại lớp {} ngày {}",
                    absentAttendances.size(), classroomId, date);
        }
    }
}
