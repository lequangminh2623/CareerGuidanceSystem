package com.lqm.attendance_service.it;

import com.lqm.attendance_service.BaseIntegrationTest;
import com.lqm.attendance_service.clients.ClassroomClient;
import com.lqm.attendance_service.clients.UserClient;
import com.lqm.attendance_service.configs.RabbitMQConfig;
import com.lqm.attendance_service.dtos.AbsentQueueMessage;
import com.lqm.attendance_service.events.ClassroomDeletedEvent;
import com.lqm.attendance_service.events.StudentsRemovedEvent;
import com.lqm.attendance_service.models.Attendance;
import com.lqm.attendance_service.models.AttendanceStatus;
import com.lqm.attendance_service.models.Device;
import com.lqm.attendance_service.models.Fingerprint;
import com.lqm.attendance_service.repositories.AttendanceRepository;
import com.lqm.attendance_service.repositories.DeviceRepository;
import com.lqm.attendance_service.repositories.FingerprintRepository;
import com.lqm.attendance_service.services.MqttService;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("AttendanceEventConsumer — Integration Tests")
class AttendanceEventConsumerIT extends BaseIntegrationTest {

    @MockitoBean
    private UserClient userClient;

    @MockitoBean
    private ClassroomClient classroomClient;

    @MockitoBean
    private MqttClient mqttClient;

    @MockitoBean
    private MqttService mqttService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private FingerprintRepository fingerprintRepository;

    @Autowired
    private DeviceRepository deviceRepository;

    private UUID classroomId;
    private UUID student1;
    private UUID student2;
    private String deviceId;
    private LocalDate today;

    @BeforeEach
    void setUp() {
        attendanceRepository.deleteAll();
        attendanceRepository.flush();
        fingerprintRepository.deleteAll();
        fingerprintRepository.flush();
        deviceRepository.deleteAll();
        deviceRepository.flush();

        classroomId = UUID.randomUUID();
        student1 = UUID.randomUUID();
        student2 = UUID.randomUUID();
        deviceId = "123456ABCDEF";
        today = LocalDate.now();
    }

    @Test
    @DisplayName("StudentsRemovedEvent — Xoá attendance và fingerprint của các student bị xoá")
    void handleStudentsRemoved_DeletesAttendancesAndFingerprints() throws Exception {
        // Save initial records
        Attendance att1 = Attendance.builder()
                .classroomId(classroomId)
                .studentId(student1)
                .attendanceDate(today)
                .status(AttendanceStatus.PRESENT)
                .build();
        Attendance att2 = Attendance.builder()
                .classroomId(classroomId)
                .studentId(student2)
                .attendanceDate(today)
                .status(AttendanceStatus.PRESENT)
                .build();
        attendanceRepository.saveAll(List.of(att1, att2));
        attendanceRepository.flush();

        Fingerprint fp1 = Fingerprint.builder()
                .fingerprintIndex(1)
                .classroomId(classroomId)
                .studentId(student1)
                .build();
        Fingerprint fp2 = Fingerprint.builder()
                .fingerprintIndex(2)
                .classroomId(classroomId)
                .studentId(student2)
                .build();
        fingerprintRepository.saveAll(List.of(fp1, fp2));
        fingerprintRepository.flush();

        // Publish event: remove student1
        StudentsRemovedEvent event = new StudentsRemovedEvent(classroomId, List.of(student1));
        rabbitTemplate.convertAndSend(RabbitMQConfig.ACADEMIC_EVENTS_EXCHANGE, RabbitMQConfig.RK_STUDENTS_REMOVED, event);

        // Await asynchronously
        boolean deleted = false;
        for (int i = 0; i < 20; i++) {
            Thread.sleep(200);
            if (attendanceRepository.findByStudentId(student1).isEmpty() &&
                fingerprintRepository.findByStudentIdAndClassroomId(student1, classroomId).isEmpty()) {
                deleted = true;
                break;
            }
        }

        assertThat(deleted).isTrue();
        // Verify student2 still has records
        assertThat(attendanceRepository.findByStudentId(student2)).hasSize(1);
        assertThat(fingerprintRepository.findByStudentIdAndClassroomId(student2, classroomId)).isPresent();
    }

    @Test
    @DisplayName("ClassroomDeletedEvent — Bỏ gán thiết bị khi lớp học bị xoá")
    void handleClassroomDeleted_UnassignsDevice() throws Exception {
        Device device = Device.builder()
                .id(deviceId)
                .classroomId(classroomId)
                .isActive(true)
                .build();
        deviceRepository.save(device);
        deviceRepository.flush();

        ClassroomDeletedEvent event = new ClassroomDeletedEvent(classroomId, List.of());
        rabbitTemplate.convertAndSend(RabbitMQConfig.ACADEMIC_EVENTS_EXCHANGE, RabbitMQConfig.RK_CLASSROOM_DELETED, event);

        boolean unassigned = false;
        for (int i = 0; i < 20; i++) {
            Thread.sleep(200);
            Optional<Device> updated = deviceRepository.findById(deviceId);
            if (updated.isPresent() && updated.get().getClassroomId() == null) {
                unassigned = true;
                break;
            }
        }

        assertThat(unassigned).isTrue();
    }

    @Test
    @DisplayName("AbsentQueueMessage — Ghi nhận vắng mặt cho danh sách học sinh")
    void processAbsentQueue_SavesAbsentAttendances() throws Exception {
        AbsentQueueMessage message = AbsentQueueMessage.builder()
                .classroomId(classroomId.toString())
                .studentIds(List.of(student1.toString(), student2.toString()))
                .date(today.toString())
                .build();

        rabbitTemplate.convertAndSend(RabbitMQConfig.ATTENDANCE_EXCHANGE, "", message);

        boolean saved = false;
        for (int i = 0; i < 20; i++) {
            Thread.sleep(200);
            List<Attendance> attendances = attendanceRepository.findByClassroomIdAndAttendanceDate(classroomId, today);
            if (attendances.size() == 2) {
                saved = true;
                break;
            }
        }

        assertThat(saved).isTrue();
        List<Attendance> attendances = attendanceRepository.findByClassroomIdAndAttendanceDate(classroomId, today);
        assertThat(attendances).extracting(Attendance::getStatus).containsOnly(AttendanceStatus.ABSENT);
    }
}
