package com.lqm.academic_service.clients;

import com.lqm.academic_service.dtos.AcademicResponseDTO;
import com.lqm.academic_service.dtos.SubjectRequestDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@FeignClient(name = "academic-service", path = "/api/internal/secure/subjects", contextId = "subjectClient")
public interface SubjectClient {

    @GetMapping
    Page<AcademicResponseDTO> getSubjects(
            @RequestParam Map<String, String> params,
            Pageable pageable
    );

    @GetMapping("/{id}")
    SubjectRequestDTO getSubjectRequestById(@PathVariable("id") UUID id);

    @PostMapping
    void saveSubject(@RequestBody SubjectRequestDTO dto);

    @DeleteMapping("/{id}")
    void deleteSubjectById(@PathVariable("id") UUID id);
}

