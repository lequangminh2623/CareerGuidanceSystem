package com.lqm.admin_service.clients;

import com.lqm.admin_service.dtos.AttendanceListRequest;
import com.lqm.admin_service.dtos.AdminAttendanceResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@FeignClient(name = "api-gateway", path = "/attendance-service/api/internal/admin/attendances", contextId = "attendanceClient")
public interface AttendanceClient {

    @GetMapping
    List<AdminAttendanceResponseDTO> getAttendances(
            @RequestParam UUID classroomId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate attendanceDate);

    @PostMapping
    void saveAttendances(
            @RequestParam UUID classroomId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate attendanceDate,
            @RequestBody AttendanceListRequest request);

    @DeleteMapping
    void deleteAttendances(
            @RequestParam UUID classroomId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate attendanceDate);
}
