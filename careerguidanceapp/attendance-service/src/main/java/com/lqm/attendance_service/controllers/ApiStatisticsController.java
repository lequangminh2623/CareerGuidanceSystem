package com.lqm.attendance_service.controllers;

import com.lqm.attendance_service.clients.UserClient;
import com.lqm.attendance_service.dtos.AttendanceSummaryDTO;
import com.lqm.attendance_service.dtos.UserResponseDTO;
import com.lqm.attendance_service.services.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/secure/statistics")
@RequiredArgsConstructor
public class ApiStatisticsController {
    private final AttendanceService attendanceService;
    private final UserClient userClient;

    @GetMapping("/attendance")
    public ResponseEntity<AttendanceSummaryDTO> getAttendanceSummary() {
        UserResponseDTO currentUser = userClient.getCurrentUser();
        return ResponseEntity.ok(attendanceService.getAttendanceSummary(currentUser.id()));
    }
}
