package com.lqm.score_service.controllers;

import com.lqm.score_service.clients.UserClient;
import com.lqm.score_service.dtos.*;
import com.lqm.score_service.services.StatisticsService;
import jakarta.ws.rs.ForbiddenException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/api/secure/statistics")
@RequiredArgsConstructor
public class ApiStatisticsController {

    private final UserClient userClient;
    private final StatisticsService statisticsService;
    private final MessageSource messageSource;

    @GetMapping("/student")
    public ResponseEntity<StudentStatisticsResponseDTO> getStudentStatistics() {
        UserResponseDTO currentUser = userClient.getCurrentUser();
        if (currentUser.code() == null) {
            throw new ForbiddenException(
                    messageSource.getMessage("forbidden", null, Locale.getDefault()));
        }
        return ResponseEntity.ok(statisticsService.getStudentStatistics(currentUser.id()));
    }

    @GetMapping("/teacher/sections")
    public ResponseEntity<List<TeacherSectionAvgDTO>> getTeacherSectionStatistics(
            @RequestParam(value = "yearName", required = false) String yearName) {
        UserResponseDTO currentUser = userClient.getCurrentUser();
        return ResponseEntity.ok(statisticsService.getTeacherSectionStatistics(currentUser.id(), yearName));
    }

    @GetMapping("/teacher/grades")
    public ResponseEntity<List<TeacherGradeStatisticsDTO>> getTeacherGradeStatistics(
            @RequestParam(value = "subjectName", required = false) String subjectName) {
        UserResponseDTO currentUser = userClient.getCurrentUser();
        return ResponseEntity.ok(statisticsService.getTeacherGradeStatistics(currentUser.id(), subjectName));
    }

    @GetMapping("/subjects")
    public ResponseEntity<List<SubjectResponseDTO>> getAllSubjects() {
        return ResponseEntity.ok(statisticsService.getAllSubjects());
    }
}
