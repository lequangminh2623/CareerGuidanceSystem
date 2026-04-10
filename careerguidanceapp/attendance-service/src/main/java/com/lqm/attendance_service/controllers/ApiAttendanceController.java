package com.lqm.attendance_service.controllers;

import com.lqm.attendance_service.clients.UserClient;
import com.lqm.attendance_service.dtos.AttendanceResponseDTO;
import com.lqm.attendance_service.dtos.UserResponseDTO;
import com.lqm.attendance_service.services.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/secure/classrooms/{classroomId}/attendances")
@RequiredArgsConstructor
public class ApiAttendanceController {

    private final AttendanceService attendanceService;
    private final UserClient userClient;

    @GetMapping
    public List<AttendanceResponseDTO> getStudentAttendanceByClassroom(@PathVariable("classroomId") UUID classroomId) {
        UserResponseDTO currentUser = userClient.getCurrentUser();
        return attendanceService.getStudentAttendanceByClassroom(currentUser.id(), classroomId);
    }
}
