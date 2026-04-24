package com.lqm.attendance_service.services;

import com.lqm.attendance_service.dtos.AttendanceResponseDTO;
import com.lqm.attendance_service.dtos.AttendanceSummaryDTO;
import com.lqm.attendance_service.models.Attendance;
import com.lqm.attendance_service.models.AttendanceStatus;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface AttendanceService {
    List<AttendanceResponseDTO> getAttendancesByClassroomAndDate(UUID classroomId, LocalDate attendanceDate);

    void saveAttendances(UUID classroomId, LocalDate attendanceDate, List<Attendance> attendances);

    void deleteAttendancesByClassroomAndDate(UUID classroomId, LocalDate attendanceDate);

    AttendanceStatus recordAttendance(UUID studentId, UUID classroomId);

    List<AttendanceResponseDTO> getStudentAttendanceByClassroom(UUID studentId, UUID classroomId);

    AttendanceSummaryDTO getAttendanceSummary(UUID studentId);

    void deleteAttendancesByClassroomAndStudentIds(UUID classroomId, List<UUID> studentIds);
}
