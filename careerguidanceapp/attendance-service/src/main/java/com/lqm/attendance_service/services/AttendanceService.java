package com.lqm.attendance_service.services;

import com.lqm.attendance_service.dtos.AttendanceRecordResult;
import com.lqm.attendance_service.dtos.AttendanceResponseDTO;
import com.lqm.attendance_service.dtos.AttendanceSummaryDTO;
import com.lqm.attendance_service.models.Attendance;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface AttendanceService {
    List<AttendanceResponseDTO> getAttendancesByClassroomAndDate(UUID classroomId, LocalDate attendanceDate);

    void saveAttendances(UUID classroomId, LocalDate attendanceDate, List<Attendance> attendances);

    void deleteAttendancesByClassroomAndDate(UUID classroomId, LocalDate attendanceDate);

    AttendanceRecordResult recordAttendance(UUID studentId, UUID classroomId);

    List<AttendanceResponseDTO> getStudentAttendanceByClassroom(UUID studentId, UUID classroomId);

    List<AttendanceResponseDTO> getStudentAttendanceByClassroomAndDate(UUID studentId, UUID classroomId, LocalDate date);

    AttendanceSummaryDTO getAttendanceSummary(UUID studentId);

    void deleteAttendancesByClassroomAndStudentIds(UUID classroomId, List<UUID> studentIds);
}
