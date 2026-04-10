package com.lqm.attendance_service.controllers;

import com.lqm.attendance_service.dtos.AttendanceListRequestDTO;
import com.lqm.attendance_service.dtos.AttendanceResponseDTO;
import com.lqm.attendance_service.mappers.AttendanceMapper;
import com.lqm.attendance_service.models.Attendance;
import com.lqm.attendance_service.services.AttendanceService;
import com.lqm.attendance_service.services.FingerprintService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/internal/admin/attendances")
public class AdminAttendanceController {
    private final AttendanceService attendanceService;
    private final AttendanceMapper attendanceMapper;
    private final FingerprintService fingerprintService;

    @GetMapping
    public List<AttendanceResponseDTO> getAttendances(@RequestParam UUID classroomId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate attendanceDate) {
        return attendanceService.getAttendancesByClassroomAndDate(classroomId, attendanceDate);
    }

    @PostMapping
    public void saveAttendances(
            @RequestParam UUID classroomId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate attendanceDate,
            @Valid @RequestBody AttendanceListRequestDTO request) {
        List<Attendance> attendances = request.attendances().stream()
                .map(dto -> attendanceMapper.toEntity(dto, classroomId, attendanceDate))
                .toList();
        attendanceService.saveAttendances(classroomId, attendanceDate, attendances);
    }

    @DeleteMapping
    public void deleteAttendances(
            @RequestParam UUID classroomId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate attendanceDate) {
        attendanceService.deleteAttendancesByClassroomAndDate(classroomId, attendanceDate);
    }

    @DeleteMapping("/classrooms/{classroomId}")
    void deleteAttendancesForClassroom(@PathVariable UUID classroomId, @RequestBody List<UUID> studentIds) {
        attendanceService.deleteAttendancesByClassroomAndStudentIds(classroomId, studentIds);
        fingerprintService.deleteFingerprintsByClassroomAndStudentIds(classroomId, studentIds);
    }
}
