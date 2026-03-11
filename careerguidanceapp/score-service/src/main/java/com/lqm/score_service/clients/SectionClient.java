package com.lqm.score_service.clients;

import com.lqm.score_service.dtos.SectionResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@FeignClient(name = "api-gateway", path = "/academic-service/api/internal/secure/sections", contextId = "sectionClient")
public interface SectionClient {

    @PostMapping
    Page<SectionResponseDTO> getSections(List<UUID> ids, @RequestParam Map<String, String> params);

    @GetMapping("/{id}")
    SectionResponseDTO getSectionResponseById(@PathVariable("id") UUID id);

    @GetMapping("/{id}/teacher/{teacherId}/check")
    Boolean checkTeacherPermission(@PathVariable("id") UUID id, @PathVariable("teacherId") UUID teacherId);

    @GetMapping("/{id}/locked/check")
    Boolean isLockedTranscript(@PathVariable("id") UUID id);

    @PatchMapping("/{id}/lock")
    void lockTranscript(@PathVariable("id") UUID id);
}
