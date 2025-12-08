package com.lqm.academic_service.clients;

import com.lqm.academic_service.dtos.ClassroomRequestDTO;
import com.lqm.academic_service.dtos.ClassroomResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@FeignClient(name = "academic-service", path = "/api/internal/secure/classrooms", contextId = "classroomClient")
public interface ClassroomClient {

    @GetMapping
    Page<ClassroomResponseDTO> getClassrooms(
            @RequestParam Map<String, String> params,
            Pageable pageable
    );

    @GetMapping("/{id}")
    ClassroomRequestDTO getClassroomRequestById(@PathVariable("id") UUID id);

    @PostMapping
    ClassroomResponseDTO saveClassroom(@RequestBody ClassroomRequestDTO dto);

    @DeleteMapping("/{id}")
    void deleteClassroom(@PathVariable("id") UUID id);

    @DeleteMapping("/{classroomId}/students/{studentId}")
    void removeStudent(
            @PathVariable UUID classroomId,
            @PathVariable UUID studentId
    );
}

