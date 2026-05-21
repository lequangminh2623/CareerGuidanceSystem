package com.lqm.attendance_service.it;

import com.lqm.attendance_service.BaseIntegrationTest;
import com.lqm.attendance_service.clients.ClassroomClient;
import com.lqm.attendance_service.clients.UserClient;
import com.lqm.attendance_service.models.Attendance;
import com.lqm.attendance_service.models.AttendanceStatus;
import com.lqm.attendance_service.models.Device;
import com.lqm.attendance_service.models.Fingerprint;
import com.lqm.attendance_service.repositories.AttendanceRepository;
import com.lqm.attendance_service.repositories.DeviceRepository;
import com.lqm.attendance_service.repositories.FingerprintRepository;
import com.lqm.attendance_service.services.AttendanceScheduler;
import com.lqm.attendance_service.services.MqttService;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("AttendanceScheduler — Integration Tests")
class AttendanceSchedulerIT extends BaseIntegrationTest {

    @MockitoBean
    private UserClient userClient;

    @MockitoBean
    private ClassroomClient classroomClient;

    @MockitoBean
    private MqttClient mqttClient;

    @MockitoBean
    private MqttService mqttService;

    @Autowired
    private AttendanceScheduler attendanceScheduler;

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private FingerprintRepository fingerprintRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    private UUID classroomId;
    private UUID studentPresent;
    private UUID studentAbsent;
    private String deviceId;

    @BeforeEach
    void setUp() {
        attendanceRepository.deleteAll();
        attendanceRepository.flush();
        fingerprintRepository.deleteAll();
        fingerprintRepository.flush();
        deviceRepository.deleteAll();
        deviceRepository.flush();

        classroomId = UUID.randomUUID();
        studentPresent = UUID.randomUUID();
        studentAbsent = UUID.randomUUID();
        deviceId = "A1B2C3D4E5F6";
    }

    @Test
    @DisplayName("activateDevicesAt6AM — Kích hoạt tất cả thiết bị")
    void activateDevicesAt6AM_ActivatesAllDevices() {
        Device device = Device.builder()
                .id(deviceId)
                .classroomId(classroomId)
                .isActive(false)
                .build();
        deviceRepository.save(device);
        deviceRepository.flush();

        attendanceScheduler.activateDevicesAt6AM();

        Device updated = deviceRepository.findById(deviceId).orElseThrow();
        assertThat(updated.getIsActive()).isTrue();
    }

    @Test
    @DisplayName("processEndOfDayAt5PM — Đẩy học sinh vắng vào Queue và Tắt thiết bị")
    void processEndOfDayAt5PM_ProcessesEndOfDay() throws Exception {
        // 1. Tạo thiết bị đang hoạt động
        Device device = Device.builder()
                .id(deviceId)
                .classroomId(classroomId)
                .isActive(true)
                .build();
        deviceRepository.save(device);
        deviceRepository.flush();

        // 2. Đăng ký vân tay cho 2 học sinh trong lớp
        Fingerprint fp1 = Fingerprint.builder()
                .fingerprintIndex(1)
                .classroomId(classroomId)
                .studentId(studentPresent)
                .build();
        Fingerprint fp2 = Fingerprint.builder()
                .fingerprintIndex(2)
                .classroomId(classroomId)
                .studentId(studentAbsent)
                .build();
        fingerprintRepository.saveAll(List.of(fp1, fp2));
        fingerprintRepository.flush();

        // 3. Cho studentPresent đi học (status = PRESENT)
        Attendance presentAtt = Attendance.builder()
                .studentId(studentPresent)
                .classroomId(classroomId)
                .attendanceDate(LocalDate.now())
                .status(AttendanceStatus.PRESENT)
                .build();
        attendanceRepository.save(presentAtt);
        attendanceRepository.flush();

        // 4. Chạy Scheduler xử lý cuối ngày
        attendanceScheduler.processEndOfDayAt5PM();

        // 5. Kiểm tra thiết bị bị tắt
        Device updatedDevice = deviceRepository.findById(deviceId).orElseThrow();
        assertThat(updatedDevice.getIsActive()).isFalse();

        // 6. Đợi Consumer xử lý và ghi nhận ABSENT cho studentAbsent vào database
        boolean processed = false;
        for (int i = 0; i < 20; i++) {
            Thread.sleep(200);
            List<Attendance> absentList = attendanceRepository.findByStudentId(studentAbsent);
            if (!absentList.isEmpty()) {
                processed = true;
                break;
            }
        }

        assertThat(processed).isTrue();
        List<Attendance> absentRecords = attendanceRepository.findByStudentId(studentAbsent);
        assertThat(absentRecords).hasSize(1);
        assertThat(absentRecords.get(0).getStatus()).isEqualTo(AttendanceStatus.ABSENT);
    }
}
