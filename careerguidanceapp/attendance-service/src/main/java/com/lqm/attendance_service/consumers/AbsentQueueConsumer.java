package com.lqm.attendance_service.consumers;

import com.lqm.attendance_service.configs.RabbitMQConfig;
import com.lqm.attendance_service.dtos.AbsentQueueMessage;
import com.lqm.attendance_service.models.Attendance;
import com.lqm.attendance_service.models.AttendanceStatus;
import com.lqm.attendance_service.repositories.AttendanceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class AbsentQueueConsumer {

    private final AttendanceRepository attendanceRepo;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_ABSENT)
    public void processAbsentQueue(AbsentQueueMessage message) {
        log.info("Received AbsentQueueMessage: classroomId={}, students={}",
                message.getClassroomId(), message.getStudentIds().size());
        try {
            UUID classroomId = UUID.fromString(message.getClassroomId());
            LocalDate date = LocalDate.parse(message.getDate());

            List<Attendance> absentAttendances = new ArrayList<>();
            for (String idStr : message.getStudentIds()) {
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
        } catch (Exception e) {
            log.error("Error processing AbsentQueueMessage: {}", e.getMessage(), e);
            throw e;
        }
    }
}
