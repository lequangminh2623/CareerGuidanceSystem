package com.lqm.admin_service.clients;

import com.lqm.admin_service.dtos.AcademicResponseDTO;
import com.lqm.admin_service.dtos.GradeDetailsResponseDTO;
import com.lqm.admin_service.dtos.GradeRequestDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@FeignClient(name = "api-gateway", path = "/academic-service/api/internal/admin/grades", contextId = "gradeClient")
public interface GradeClient {

    @GetMapping("/by-year/{yearId}")
    List<AcademicResponseDTO> getGradesByYearId(@PathVariable UUID yearId, @RequestParam Map<String, String> params);

    @GetMapping("/details")
    List<GradeDetailsResponseDTO> getGradesDetails(@RequestParam Map<String, String> params);

    @GetMapping("/{id}")
    GradeRequestDTO getGradeRequestById(@PathVariable UUID id);

    @PostMapping
    void saveGrade(@RequestBody GradeRequestDTO dto);

    @DeleteMapping("/{id}")
    void deleteGradeById(@PathVariable UUID id);
}
