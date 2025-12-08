package com.lqm.academic_service.controllers;

import com.lqm.academic_service.dtos.AcademicResponseDTO;
import com.lqm.academic_service.dtos.SubjectRequestDTO;
import com.lqm.academic_service.mappers.SubjectMapper;
import com.lqm.academic_service.models.Subject;
import com.lqm.academic_service.services.SubjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/internal/secure/subjects")
public class InternalSubjectController {

    private final SubjectService subjectService;
    private final SubjectMapper subjectMapper;
    private final MessageSource messageSource;

    @GetMapping
    public Page<AcademicResponseDTO> getSubjects(
            @RequestParam Map<String, String> params,
            Pageable pageable
    ) {
        return subjectService.getSubjects(params, pageable)
                .map(subjectMapper::toAcademicResponseDTO);
    }

    @GetMapping("/{id}")
    public SubjectRequestDTO getSubjectRequestById(@PathVariable("id") UUID id) {
        return subjectMapper.toSubjectRequestDTO(
                subjectService.getSubjectById(id)
        );
    }

    @PostMapping
    public AcademicResponseDTO saveSubject(@RequestBody SubjectRequestDTO dto) {
         Subject subject = subjectService.saveSubject(subjectMapper.toEntity(dto));
         return subjectMapper.toAcademicResponseDTO(subject);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSubjectById(@PathVariable("id") UUID id) {
            subjectService.deleteSubjectById(id);
    }
}

