package com.lqm.academic_service.controllers;

import com.lqm.academic_service.dtos.AcademicResponseDTO;
import com.lqm.academic_service.dtos.SubjectRequestDTO;
import com.lqm.academic_service.mappers.SubjectMapper;
import com.lqm.academic_service.models.Subject;
import com.lqm.academic_service.services.SubjectService;
import com.lqm.academic_service.utils.PageSize;
import com.lqm.academic_service.utils.PageableUtil;
import com.lqm.academic_service.validators.WebAppValidator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/internal/admin/subjects")
public class AdminSubjectController {

    private final SubjectService subjectService;
    private final SubjectMapper subjectMapper;
    private final PageableUtil pageableUtil;
    private final WebAppValidator webAppValidator;

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.setValidator(webAppValidator);
    }

    @GetMapping
    public Page<AcademicResponseDTO> getSubjects(@RequestParam Map<String, String> params) {
        Pageable pageable = pageableUtil.getPageable(
                params.getOrDefault("page", "1"),
                PageSize.SUBJECT_PAGE_SIZE,
                List.of("name:asc")
        );

        return subjectService.getSubjects(params, pageable).map(subjectMapper::toAcademicResponseDTO);
    }

    @GetMapping("/{id}")
    public SubjectRequestDTO getSubjectRequestById(@PathVariable("id") UUID id) {
        return subjectMapper.toSubjectRequestDTO(
                subjectService.getSubjectById(id)
        );
    }

    @PostMapping
    public void saveSubject(@RequestBody @Valid SubjectRequestDTO dto) {
         subjectService.saveSubject(subjectMapper.toEntity(dto));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSubjectById(@PathVariable("id") UUID id) {
            subjectService.deleteSubjectById(id);
    }
}

