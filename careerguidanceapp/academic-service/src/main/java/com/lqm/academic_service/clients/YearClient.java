package com.lqm.academic_service.clients;

import com.lqm.academic_service.dtos.AcademicResponseDTO;
import com.lqm.academic_service.dtos.YearRequestDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@FeignClient(name = "academic-service", path = "/api/internal/secure/years", contextId = "yearClient")
public interface YearClient {

    @GetMapping
    Page<AcademicResponseDTO> getYears(@RequestParam Map<String, String> params, Pageable pageable);

    @GetMapping("/{id}/request")
    YearRequestDTO getYearRequestById(@PathVariable UUID id);

    @GetMapping("/{id}/response")
    AcademicResponseDTO getYearResponseById(@PathVariable UUID id);

    @PostMapping
    AcademicResponseDTO saveYear(@RequestBody YearRequestDTO dto);

    @DeleteMapping("/{id}")
    void deleteYearById(@PathVariable UUID id);
}

