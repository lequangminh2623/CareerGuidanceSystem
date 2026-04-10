package com.lqm.admin_service.clients;

import com.lqm.admin_service.dtos.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@FeignClient(name = "api-gateway", path = "/academic-service/api/internal/admin/classrooms", contextId = "classroomClient")
public interface ClassroomClient {

    @GetMapping
    Page<ClassroomResponseDTO> getClassrooms(@RequestParam Map<String, String> params);

    @GetMapping("/{id}/request")
    ClassroomRequestDTO getClassroomRequestById(@PathVariable UUID id);

    @GetMapping("/{id}/response")
    ClassroomResponseDTO getClassroomResponseById(@PathVariable UUID id);

    @PostMapping
    void saveClassroom(@RequestBody ClassroomRequestDTO dto);

    @DeleteMapping("/{id}")
    void deleteClassroom(@PathVariable UUID id);

    @GetMapping("/{id}/students")
    Page<UserResponseDTO> getStudentsInClassroom(@PathVariable UUID id, @RequestParam Map<String, String> params);

}

