package com.lqm.admin_service.clients;

import com.lqm.admin_service.dtos.CurriculumRequestDTO;
import com.lqm.admin_service.dtos.CurriculumResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@FeignClient(name = "api-gateway", path = "/academic-service/api/internal/admin/curriculums", contextId = "curriculumClient")
public interface CurriculumClient {

    @GetMapping("")
    Page<CurriculumResponseDTO> getCurriculums(@RequestParam Map<String, String> params);

    @GetMapping("/{id}")
    CurriculumRequestDTO getCurriculumRequestById(@PathVariable UUID id);

    @PostMapping
    void saveCurriculum(@RequestBody CurriculumRequestDTO curriculumRequestDTO);

    @DeleteMapping("/{id}")
    void deleteCurriculum(@PathVariable UUID id);
}
