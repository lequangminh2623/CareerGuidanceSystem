package com.lqm.admin_service.clients;

import com.lqm.admin_service.dtos.ChangeStatusRequestDTO;
import com.lqm.admin_service.dtos.SectionListRequest;
import com.lqm.admin_service.dtos.SectionRequestDTO;
import com.lqm.admin_service.dtos.SectionResponseDTO;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@FeignClient(name = "api-gateway",path = "/academic-service/api/internal/admin/sections",contextId = "sectionClient")
public interface SectionClient {

    @GetMapping("/requests")
    Page<SectionRequestDTO> getSectionRequests(@RequestParam Map<String, String> params);

    @PostMapping("/batch")
    Page<SectionResponseDTO> getSectionResponses(@RequestBody List<UUID> ids, @RequestParam Map<String, String> params);

    @PostMapping
    void saveSections(@RequestBody @Valid SectionListRequest request, @RequestParam Map<String, String> params);

    @PostMapping("/single")
    SectionResponseDTO saveSingleSection(@RequestBody @Valid SectionRequestDTO request, @RequestParam Map<String, String> params);

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteSection(@PathVariable("id") UUID id);

    @GetMapping("/{id}")
    SectionResponseDTO getSectionResponseById(@PathVariable("id") UUID id);

    @PatchMapping("/{id}/change-status")
    void changeTranscriptStatus(@PathVariable("id") UUID id, @Valid @RequestBody ChangeStatusRequestDTO request);

}

