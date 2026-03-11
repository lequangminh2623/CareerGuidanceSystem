package com.lqm.admin_service.clients;

import com.lqm.admin_service.dtos.AcademicResponseDTO;
import com.lqm.admin_service.dtos.SemesterRequestDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@FeignClient(name = "api-gateway", path = "/academic-service/api/internal/admin", contextId = "semesterClient")
public interface SemesterClient {

    @GetMapping("/years/{yearId}/semesters")
    List<AcademicResponseDTO> getSemestersByYearId(@PathVariable UUID yearId,
                                                   @RequestParam Map<String, String> params);

    @GetMapping("/semesters/{id}")
    SemesterRequestDTO getSemesterRequestById(@PathVariable UUID id);

    @PostMapping("/semesters")
    void saveSemester(@RequestBody SemesterRequestDTO dto);

    @DeleteMapping("/semesters/{id}")
    void deleteSemesterById(@PathVariable UUID id);
}
