package com.lqm.academic_service.controllers;

import com.lqm.academic_service.clients.UserClient;
import com.lqm.academic_service.dtos.ClassroomDetailsResponseDTO;
import com.lqm.academic_service.dtos.UserResponseDTO;
import com.lqm.academic_service.mappers.ClassroomMapper;
import com.lqm.academic_service.models.Classroom;
import com.lqm.academic_service.models.StudentClassroom;
import com.lqm.academic_service.services.ClassroomService;
import com.lqm.academic_service.services.StudentClassroomService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/internal/secure/classrooms")
@RequiredArgsConstructor
public class InternalClassroomController {

    private final ClassroomService classroomService;
    private final StudentClassroomService studentClassroomService;
    private final ClassroomMapper classroomMapper;
    private final UserClient userClient;

    @GetMapping("/{id}")
    public ClassroomDetailsResponseDTO getClassroomDetailsResponseById(@PathVariable UUID id) {
        Classroom classroom = classroomService.getClassroomWithStudents(id);
        return classroomMapper.toClassroomDetailsResponseDTO(classroom);
    }

    @GetMapping("/{classroomId}/students")
    public Page<UserResponseDTO> getStudentsInClassroom(@PathVariable UUID classroomId,
                                                        @RequestParam Map<String, String> params) {
        params.put("page", "");
        return userClient.getUsers(studentClassroomService.getStudentClassroomsByClassroomId(classroomId).stream()
                .map(StudentClassroom::getStudentId).toList(), params);
    }

    @PostMapping("/{classroomId}/students/non-exist")
    public List<UUID> getNonExistingStudentIds(
            @PathVariable("classroomId") UUID classroomId,
            @RequestBody List<UUID> studentIds) {
        return classroomService.getNonExistingStudentIds(classroomId, studentIds);
    }
}
