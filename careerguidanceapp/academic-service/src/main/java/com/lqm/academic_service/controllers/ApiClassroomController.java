package com.lqm.academic_service.controllers;

import com.lqm.academic_service.clients.UserClient;
import com.lqm.academic_service.dtos.AcademicResponseDTO;
import com.lqm.academic_service.dtos.UserResponseDTO;
import com.lqm.academic_service.mappers.ClassroomMapper;
import com.lqm.academic_service.services.StudentClassroomService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/secure/classrooms")
@RequiredArgsConstructor
public class ApiClassroomController {

    private final StudentClassroomService studentClassroomService;
    private final ClassroomMapper classroomMapper;
    private final UserClient userClient;

    @GetMapping("/current-student")
    public List<AcademicResponseDTO> getStudentClassrooms() {
        UserResponseDTO currentUser = userClient.getCurrentUser();
        return studentClassroomService.getClassroomsByStudentId(currentUser.id()).stream()
                .map(classroomMapper::toClassroomDetailNameDTO)
                .toList();
    }
}
