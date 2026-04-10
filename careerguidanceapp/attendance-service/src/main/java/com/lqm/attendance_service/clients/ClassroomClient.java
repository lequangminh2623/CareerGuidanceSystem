package com.lqm.attendance_service.clients;

import com.lqm.attendance_service.dtos.AcademicResponseDTO;
import com.lqm.attendance_service.dtos.ClassroomDetailsResponseDTO;
import com.lqm.attendance_service.dtos.UserResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@FeignClient(name = "api-gateway", path = "/academic-service/api/internal/secure/classrooms", contextId = "classroomClient")
public interface ClassroomClient {
    @GetMapping("/{id}")
    ClassroomDetailsResponseDTO getClassroomDetailsResponseById(@PathVariable UUID id);

    @PostMapping("/detail-name")
    List<AcademicResponseDTO> getClassroomDetailNames(@RequestBody List<UUID> classroomIds,
            @RequestParam Map<String, String> params);

    @GetMapping("/{classroomId}/students")
    Page<UserResponseDTO> getStudentsInClassroom(@PathVariable UUID classroomId, Map<String, String> params);

    @PostMapping("/{classroomId}/students/non-exist")
    List<UUID> getNonExistingStudentIds(@PathVariable("classroomId") UUID classroomId,
            @RequestBody List<UUID> studentIds);

    @GetMapping("/{classroomId}/student-ids")
    List<UUID> getStudentIdsInClassroom(@PathVariable("classroomId") UUID classroomId);

}
