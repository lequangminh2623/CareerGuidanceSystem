package com.lqm.attendance_service.services.Impl;

import com.lqm.attendance_service.configs.RedisConfig;
import com.lqm.attendance_service.models.Device;
import com.lqm.attendance_service.repositories.AttendanceRepository;
import com.lqm.attendance_service.repositories.DeviceRepository;
import com.lqm.attendance_service.repositories.FingerprintRepository;
import com.lqm.attendance_service.services.AttendanceScheduler;
import com.lqm.attendance_service.services.DeviceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import com.lqm.attendance_service.dtos.AbsentQueueMessage;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class AttendanceSchedulerImpl implements AttendanceScheduler {

    private final DeviceRepository deviceRepo;
    private final DeviceService deviceService;
    private final FingerprintRepository fingerprintRepo;
    private final AttendanceRepository attendanceRepo;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String ZONE_VN = "Asia/Ho_Chi_Minh";

    @Override
    @Scheduled(cron = "0 0 21 * * MON-FRI", zone = ZONE_VN)
    public void activateDevicesAt6AM() {
        log.info("Scheduler: Đang kích hoạt tất cả thiết bị lúc 06:00 sáng");
        List<Device> devices = deviceRepo.findAll();
        for (Device device : devices) {
            deviceService.updateDeviceActiveStatus(device.getId(), true, true);
        }
    }

    @Override
    @Scheduled(cron = "0 00 17 * * MON-FRI", zone = ZONE_VN)
    public void processEndOfDayAt5PM() {

        log.info("Scheduler: Đang xử lý cuối ngày lúc 17:00 chiều");
        LocalDate today = LocalDate.now(ZoneId.of(ZONE_VN));
        List<Object[]> allFingerprints = fingerprintRepo.findAllFingerprintMappings();
        Set<UUID> presentStudentIds = attendanceRepo.findPresentStudentIdsByDate(today);
        Map<UUID, List<UUID>> absentByClass = allFingerprints.stream()
                .filter(obj -> !presentStudentIds.contains((UUID) obj[0]))
                .collect(Collectors.groupingBy(
                        obj -> (UUID) obj[1],
                        Collectors.mapping(obj -> (UUID) obj[0], Collectors.toList())));

        absentByClass.forEach((classroomId, absentStudentIds) -> {
            if (!absentStudentIds.isEmpty()) {
                // Đẩy vào Redis Queue để xử lý ABSENT bất đồng bộ theo lô
                AbsentQueueMessage message = AbsentQueueMessage.builder()
                        .studentIds(absentStudentIds.stream().map(UUID::toString).toList())
                        .classroomId(classroomId.toString())
                        .date(today.toString())
                        .build();
                redisTemplate.opsForList().leftPush(RedisConfig.ABSENT_QUEUE, message);
                log.info("Scheduler: Đẩy vào queue {} học sinh ABSENT lớp {}", absentStudentIds.size(), classroomId);
            }
        });

        List<Device> devices = deviceRepo.findAll();
        for (Device device : devices) {
            deviceService.updateDeviceActiveStatus(device.getId(), false, true);
        }
    }
}
