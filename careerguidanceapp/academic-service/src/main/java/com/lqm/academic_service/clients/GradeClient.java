package com.lqm.academic_service.clients;

import com.lqm.academic_service.dtos.AcademicResponseDTO;
import com.lqm.academic_service.dtos.GradeDetailsResponseDTO;
import com.lqm.academic_service.dtos.GradeRequestDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@FeignClient(name = "academic-service", path = "/api/internal/secure", contextId = "gradeClient")
public interface GradeClient {

    @GetMapping("/years/{yearId}/grades")
    List<AcademicResponseDTO> getGradesByYearId(
            @PathVariable UUID yearId,
            @RequestParam Map<String, String> params
    );

    @GetMapping("/grades/details")
    List<GradeDetailsResponseDTO> getGradesDetails(@RequestParam Map<String, String> params);

    @GetMapping("/grades/{id}")
    GradeRequestDTO getGradeRequestById(@PathVariable UUID id);

    @PostMapping("/grades")
    AcademicResponseDTO saveGrade(@RequestBody GradeRequestDTO dto);

    @DeleteMapping("/grades/{id}")
    void deleteGradeById(@PathVariable UUID id);
}
