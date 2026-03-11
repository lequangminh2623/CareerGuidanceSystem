package com.lqm.admin_service.clients;

import com.lqm.admin_service.dtos.AcademicResponseDTO;
import com.lqm.admin_service.dtos.SubjectRequestDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@FeignClient(name = "api-gateway", path = "/academic-service/api/internal/admin/subjects", contextId = "subjectClient")
public interface SubjectClient {

    @GetMapping
    Page<AcademicResponseDTO> getSubjects(@RequestParam Map<String, String> params);

    @GetMapping("/{id}")
    SubjectRequestDTO getSubjectRequestById(@PathVariable("id") UUID id);

    @PostMapping
    void saveSubject(@RequestBody SubjectRequestDTO dto);

    @DeleteMapping("/{id}")
    void deleteSubjectById(@PathVariable("id") UUID id);
}

