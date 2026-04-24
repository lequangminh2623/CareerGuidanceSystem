package com.lqm.attendance_service.services.Impl;

import com.lqm.attendance_service.dtos.AttendanceResponseDTO;
import com.lqm.attendance_service.dtos.AttendanceSummaryDTO;
import com.lqm.attendance_service.mappers.AttendanceMapper;
import com.lqm.attendance_service.models.Attendance;
import com.lqm.attendance_service.repositories.AttendanceRepository;
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
    public AttendanceStatus recordAttendance(UUID studentId, UUID classroomId) {
        LocalDate today = LocalDate.now(java.time.ZoneId.of("Asia/Ho_Chi_Minh"));
        LocalTime now = LocalTime.now(java.time.ZoneId.of("Asia/Ho_Chi_Minh"));

        var existing = attendanceRepo.findByStudentIdAndAttendanceDate(studentId, today);
        if (existing.isPresent()) {
            return existing.get().getStatus();
        }

        AttendanceStatus status;
        if (now.isBefore(LocalTime.of(7, 0))) {
            status = AttendanceStatus.PRESENT;
        } else if (now.isBefore(LocalTime.of(17, 0))) {
            status = AttendanceStatus.LATE;
        } else {
            status = AttendanceStatus.ABSENT;
        }

        Attendance attendance = Attendance.builder()
                .studentId(studentId)
                .classroomId(classroomId)
                .attendanceDate(today)
                .checkInTime(now)
                .status(status)
                .build();
        attendanceRepo.save(attendance);
        return status;
    }

    @Override
    public List<AttendanceResponseDTO> getStudentAttendanceByClassroom(UUID studentId, UUID classroomId) {
        return attendanceRepo.findByStudentIdAndClassroomId(studentId, classroomId)
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

}
