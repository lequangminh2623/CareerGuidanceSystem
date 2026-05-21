package com.lqm.attendance_service.it;

import com.lqm.attendance_service.BaseIntegrationTest;
import com.lqm.attendance_service.models.Device;
import com.lqm.attendance_service.repositories.DeviceRepository;
import com.lqm.attendance_service.specifications.DeviceSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DisplayName("DeviceRepository — Integration Tests")
class DeviceRepositoryIT extends BaseIntegrationTest {

    @Autowired
    private DeviceRepository deviceRepository;

    private UUID classroomId;
    private String deviceId;

    @BeforeEach
    void setUp() {
        deviceRepository.deleteAll();
        deviceRepository.flush();

        classroomId = UUID.randomUUID();
        deviceId = "A1B2C3D4E5F6";
    }

    @Test
    @DisplayName("findByClassroomId — Trả về device được gán cho lớp")
    void findByClassroomId_ReturnsAssignedDevice() {
        Device device = Device.builder()
                .id(deviceId)
                .classroomId(classroomId)
                .isActive(true)
                .build();
        deviceRepository.save(device);
        deviceRepository.flush();

        Optional<Device> result = deviceRepository.findByClassroomId(classroomId);
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(deviceId);

        assertThat(deviceRepository.findByClassroomId(UUID.randomUUID())).isEmpty();
    }

    @Test
    @DisplayName("existsByClassroomId — Kiểm tra xem lớp đã có thiết bị chưa")
    void existsByClassroomId_ReturnsTrueOrFalse() {
        Device device = Device.builder()
                .id(deviceId)
                .classroomId(classroomId)
                .isActive(true)
                .build();
        deviceRepository.save(device);
        deviceRepository.flush();

        assertThat(deviceRepository.existsByClassroomId(classroomId)).isTrue();
        assertThat(deviceRepository.existsByClassroomId(UUID.randomUUID())).isFalse();
    }

    @Test
    @DisplayName("findAll với DeviceSpecification — Tìm kiếm theo kw (id)")
    void findAllWithSpecification_FiltersByKeyword() {
        Device d1 = Device.builder().id("111111111111").classroomId(UUID.randomUUID()).isActive(true).build();
        Device d2 = Device.builder().id("222222222222").classroomId(UUID.randomUUID()).isActive(true).build();
        Device d3 = Device.builder().id("111122223333").classroomId(UUID.randomUUID()).isActive(true).build();

        deviceRepository.saveAll(List.of(d1, d2, d3));
        deviceRepository.flush();

        // Tìm với keyword "1111" -> d1, d3
        Page<Device> page = deviceRepository.findAll(
                DeviceSpecification.filterByParams(Map.of("kw", "1111")),
                PageRequest.of(0, 10)
        );

        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getContent()).extracting(Device::getId).containsExactlyInAnyOrder("111111111111", "111122223333");
    }
}
