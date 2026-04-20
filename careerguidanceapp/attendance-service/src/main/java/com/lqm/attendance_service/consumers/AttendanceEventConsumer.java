package com.lqm.attendance_service.consumers;

import com.lqm.attendance_service.configs.RabbitMQConfig;
import com.lqm.attendance_service.events.ClassroomDeletedEvent;
import com.lqm.attendance_service.events.StudentsRemovedEvent;
import com.lqm.attendance_service.services.AttendanceService;
import com.lqm.attendance_service.services.DeviceService;
import com.lqm.attendance_service.services.FingerprintService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AttendanceEventConsumer {

    private final AttendanceService attendanceService;
    private final FingerprintService fingerprintService;
    private final DeviceService deviceService;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_STUDENTS_REMOVED)
    public void handleStudentsRemoved(StudentsRemovedEvent event) {
        log.info("Received StudentsRemovedEvent: classroomId={}, removedStudents={}",
                event.classroomId(), event.removedStudentIds().size());
        try {
            attendanceService.deleteAttendancesByClassroomAndStudentIds(event.classroomId(), event.removedStudentIds());
            fingerprintService.deleteFingerprintsByClassroomAndStudentIds(event.classroomId(), event.removedStudentIds());
            log.info("Successfully processed StudentsRemovedEvent for classroom {}", event.classroomId());
        } catch (Exception e) {
            log.error("Error processing StudentsRemovedEvent: {}", e.getMessage(), e);
            throw e;
        }
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_CLASSROOM_DELETED)
    public void handleClassroomDeleted(ClassroomDeletedEvent event) {
        log.info("Received ClassroomDeletedEvent: classroomId={}", event.classroomId());
        try {
            deviceService.unassignDeviceByClassroomId(event.classroomId());
            log.info("Successfully processed ClassroomDeletedEvent: unassigned device for classroom {}", event.classroomId());
        } catch (Exception e) {
            log.error("Error processing ClassroomDeletedEvent: {}", e.getMessage(), e);
            throw e;
        }
    }
}
