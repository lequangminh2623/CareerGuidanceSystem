package com.lqm.attendance_service.services.Impl;

import com.lqm.attendance_service.dtos.AttendanceRecordResult;
import com.lqm.attendance_service.dtos.AttendanceResponseDTO;
import com.lqm.attendance_service.dtos.AttendanceSummaryDTO;
import com.lqm.attendance_service.mappers.AttendanceMapper;
import com.lqm.attendance_service.models.Attendance;
import com.lqm.attendance_service.models.AttendanceConfig;
import com.lqm.attendance_service.models.AttendanceSession;
import com.lqm.attendance_service.repositories.AttendanceRepository;
import com.lqm.attendance_service.services.AttendanceConfigService;
import com.lqm.attendance_service.services.AttendanceService;
import com.lqm.attendance_service.models.AttendanceStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class AttendanceServiceImpl implements AttendanceService {

    private final AttendanceRepository attendanceRepo;
    private final AttendanceMapper attendanceMapper;
    private final AttendanceConfigService configService;

    @Override
    public List<AttendanceResponseDTO> getAttendancesByClassroomAndDate(UUID classroomId, LocalDate attendanceDate) {
        return attendanceRepo.findByClassroomIdAndAttendanceDate(classroomId, attendanceDate)
                .stream()
                .map(attendanceMapper::toAttendanceResponseDTO)
                .toList();
    }

    @Override
    @Transactional
    public void saveAttendances(UUID classroomId, LocalDate attendanceDate, List<Attendance> attendances) {
        List<Attendance> existingAttendances = attendanceRepo.findByClassroomIdAndAttendanceDate(classroomId,
                attendanceDate);

        for (Attendance newAttendance : attendances) {
            existingAttendances.stream()
                    .filter(a -> a.getStudentId().equals(newAttendance.getStudentId()))
                    .findFirst()
                    .ifPresentOrElse(
                            existing -> {
                                existing.setStatus(newAttendance.getStatus());
                                existing.setCheckInTime(newAttendance.getCheckInTime());
                            },
                            () -> existingAttendances.add(newAttendance));
        }
        attendanceRepo.saveAll(existingAttendances);
    }

    @Override
    @Transactional
    public void deleteAttendancesByClassroomAndDate(UUID classroomId, LocalDate attendanceDate) {
        attendanceRepo.deleteByClassroomIdAndAttendanceDate(classroomId, attendanceDate);
    }

    @Override
    @Transactional
    public AttendanceRecordResult recordAttendance(UUID studentId, UUID classroomId) {
        LocalDate today = LocalDate.now(java.time.ZoneId.of("Asia/Ho_Chi_Minh"));
        LocalTime now = LocalTime.now(java.time.ZoneId.of("Asia/Ho_Chi_Minh"));

        AttendanceConfig config = configService.getConfig();
        AttendanceSession session = determineSession(now, config);

        if (session == null) {
            // Ngoài giờ điểm danh — không ghi nhận
            return AttendanceRecordResult.builder()
                    .status(AttendanceStatus.ABSENT)
                    .session(null)
                    .isNew(false)
                    .build();
        }

        // Kiểm tra đã điểm danh buổi này chưa
        var existing = attendanceRepo.findByStudentIdAndAttendanceDateAndSession(studentId, today, session);
        if (existing.isPresent()) {
            return AttendanceRecordResult.builder()
                    .status(existing.get().getStatus())
                    .session(session)
                    .isNew(false)
                    .build();
        }

        AttendanceStatus status = determineStatus(now, session, config);

        Attendance attendance = Attendance.builder()
                .studentId(studentId)
                .classroomId(classroomId)
                .attendanceDate(today)
                .checkInTime(now)
                .status(status)
                .session(session)
                .build();
        attendanceRepo.save(attendance);
        return AttendanceRecordResult.builder()
                .status(status)
                .session(session)
                .isNew(true)
                .build();
    }

    @Override
    public List<AttendanceResponseDTO> getStudentAttendanceByClassroom(UUID studentId, UUID classroomId) {
        return attendanceRepo.findByStudentIdAndClassroomId(studentId, classroomId)
                .stream()
                .map(attendanceMapper::toAttendanceResponseDTO)
                .toList();
    }

    @Override
    public List<AttendanceResponseDTO> getStudentAttendanceByClassroomAndDate(UUID studentId, UUID classroomId, LocalDate date) {
        return attendanceRepo.findByStudentIdAndClassroomIdAndAttendanceDate(studentId, classroomId, date)
                .stream()
                .map(attendanceMapper::toAttendanceResponseDTO)
                .toList();
    }

    @Override
    public AttendanceSummaryDTO getAttendanceSummary(UUID studentId) {
        List<Attendance> attendances = attendanceRepo.findByStudentId(studentId);

        long presentCount = attendances.stream()
                .filter(a -> a.getStatus() == AttendanceStatus.PRESENT)
                .count();
        long lateCount = attendances.stream()
                .filter(a -> a.getStatus() == AttendanceStatus.LATE)
                .count();
        long absentCount = attendances.stream()
                .filter(a -> a.getStatus() == AttendanceStatus.ABSENT)
                .count();

        return AttendanceSummaryDTO.builder()
                .presentCount(presentCount)
                .lateCount(lateCount)
                .absentCount(absentCount)
                .build();
    }

    @Override
    @Transactional
    public void deleteAttendancesByClassroomAndStudentIds(UUID classroomId, List<UUID> studentIds) {
        attendanceRepo.deleteByClassroomIdAndStudentIdIn(classroomId, studentIds);
    }

    /**
     * Xác định buổi điểm danh dựa trên giờ hiện tại và cấu hình.
     * Trả về null nếu ngoài khung giờ điểm danh.
     */
    private AttendanceSession determineSession(LocalTime now, AttendanceConfig config) {
        // Khung giờ buổi sáng: từ (giờ bắt đầu - 1 tiếng) đến giờ kết thúc
        LocalTime morningCheckInStart = config.getMorningStartTime().minusHours(1);
        if ((now.isAfter(morningCheckInStart) || now.equals(morningCheckInStart)) && !now.isAfter(config.getMorningEndTime())) {
            return AttendanceSession.MORNING;
        }

        // Khung giờ buổi chiều: từ (giờ bắt đầu chiều - 1 tiếng) đến giờ kết thúc chiều (nếu cấu hình 2 buổi)
        if (config.getSessionsPerDay() == 2) {
            LocalTime afternoonCheckInStart = config.getAfternoonStartTime().minusHours(1);
            if ((now.isAfter(afternoonCheckInStart) || now.equals(afternoonCheckInStart)) && !now.isAfter(config.getAfternoonEndTime())) {
                return AttendanceSession.AFTERNOON;
            }
        }

        return null;
    }

    /**
     * Xác định trạng thái điểm danh dựa trên giờ check-in và cấu hình buổi.
     * - Check-in trước giờ bắt đầu buổi → PRESENT
     * - Check-in sau giờ bắt đầu nhưng trước giờ kết thúc → LATE
     */
    private AttendanceStatus determineStatus(LocalTime now, AttendanceSession session, AttendanceConfig config) {
        if (session == AttendanceSession.MORNING) {
            return now.isBefore(config.getMorningStartTime()) || now.equals(config.getMorningStartTime())
                    ? AttendanceStatus.PRESENT
                    : AttendanceStatus.LATE;
        } else {
            return now.isBefore(config.getAfternoonStartTime()) || now.equals(config.getAfternoonStartTime())
                    ? AttendanceStatus.PRESENT
                    : AttendanceStatus.LATE;
        }
    }

}
