package com.lqm.admin_service.clients;

import com.lqm.admin_service.dtos.AcademicResponseDTO;
import com.lqm.admin_service.dtos.YearRequestDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@FeignClient(name = "api-gateway", path = "/academic-service/api/internal/admin/years", contextId = "yearClient")
public interface YearClient {

    @GetMapping
    Page<AcademicResponseDTO> getYears(@RequestParam Map<String, String> params);

    @GetMapping("/{id}/request")
    YearRequestDTO getYearRequestById(@PathVariable UUID id);

    @GetMapping("/{id}/response")
    AcademicResponseDTO getYearResponseById(@PathVariable UUID id);

    @PostMapping
    void saveYear(@RequestBody YearRequestDTO dto);

    @DeleteMapping("/{id}")
    void deleteYearById(@PathVariable UUID id);
}

