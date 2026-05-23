package com.lqm.attendance_service.services.Impl;

import com.lqm.attendance_service.dtos.AttendanceConfigDTO;
import com.lqm.attendance_service.models.AttendanceConfig;
import com.lqm.attendance_service.repositories.AttendanceConfigRepository;
import com.lqm.attendance_service.services.AttendanceConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;

@Service
@RequiredArgsConstructor
public class AttendanceConfigServiceImpl implements AttendanceConfigService {

    private static final Long CONFIG_ID = 1L;

    private final AttendanceConfigRepository configRepo;

    @Override
    public AttendanceConfig getConfig() {
        return configRepo.findById(CONFIG_ID)
                .orElseGet(this::createDefaultConfig);
    }

    @Override
    @Transactional
    public AttendanceConfig updateConfig(AttendanceConfigDTO dto) {
        validateConfig(dto);

        AttendanceConfig config = configRepo.findById(CONFIG_ID)
                .orElseGet(this::createDefaultConfig);

        config.setSessionsPerDay(dto.sessionsPerDay());
        config.setMorningStartTime(dto.morningStartTime());
        config.setMorningEndTime(dto.morningEndTime());
        config.setAfternoonStartTime(dto.afternoonStartTime());
        config.setAfternoonEndTime(dto.afternoonEndTime());

        return configRepo.save(config);
    }

    private AttendanceConfig createDefaultConfig() {
        AttendanceConfig config = AttendanceConfig.builder()
                .id(CONFIG_ID)
                .sessionsPerDay(1)
                .morningStartTime(LocalTime.of(7, 0))
                .morningEndTime(LocalTime.of(11, 30))
                .afternoonStartTime(LocalTime.of(13, 0))
                .afternoonEndTime(LocalTime.of(17, 0))
                .build();
        return configRepo.save(config);
    }

    private void validateConfig(AttendanceConfigDTO dto) {
        if (dto.sessionsPerDay() < 1 || dto.sessionsPerDay() > 2) {
            throw new IllegalArgumentException("Số buổi điểm danh phải là 1 hoặc 2");
        }

        LocalTime morningStart = dto.morningStartTime();
        LocalTime morningEnd = dto.morningEndTime();
        if (morningStart.isBefore(LocalTime.of(7, 0)) || morningStart.isAfter(LocalTime.of(8, 0))) {
            throw new IllegalArgumentException("Giờ vào buổi sáng phải từ 07:00 đến 08:00");
        }
        if (morningEnd.isBefore(LocalTime.of(10, 30))) {
            throw new IllegalArgumentException("Giờ kết thúc buổi sáng không được trước 10:30");
        }

        if (dto.sessionsPerDay() == 2) {
            LocalTime afternoonStart = dto.afternoonStartTime();
            LocalTime afternoonEnd = dto.afternoonEndTime();
            if (afternoonStart.isBefore(LocalTime.of(13, 0)) || afternoonStart.isAfter(LocalTime.of(14, 0))) {
                throw new IllegalArgumentException("Giờ vào buổi chiều phải từ 13:00 đến 14:00");
            }
            if (afternoonEnd.isBefore(LocalTime.of(16, 0)) || afternoonEnd.isAfter(LocalTime.of(17, 0))) {
                throw new IllegalArgumentException("Giờ kết thúc buổi chiều phải từ 16:00 đến 17:00");
            }
        }
    }
}
