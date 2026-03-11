package com.lqm.score_service.clients;

import com.lqm.score_service.dtos.ClassroomDetailsResponseDTO;
import com.lqm.score_service.dtos.UserResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@FeignClient(name = "api-gateway", path = "/academic-service/api/internal/secure/classrooms", contextId = "classroomClient")
public interface ClassroomClient {
    @GetMapping("/{id}")
    ClassroomDetailsResponseDTO getClassroomDetailsResponseById(@PathVariable UUID id);

    @GetMapping("/{classroomId}/students")
    Page<UserResponseDTO> getStudentsInClassroom(@PathVariable UUID classroomId, Map<String, String> params);

    @PostMapping("/{classroomId}/students/non-exist")
    List<UUID> getNonExistingStudentIds(@PathVariable UUID classroomId, @RequestBody List<UUID> studentIds);
}
