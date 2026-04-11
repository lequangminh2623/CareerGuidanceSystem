package com.lqm.academic_service.controllers;

import com.lqm.academic_service.dtos.CurriculumRequestDTO;
import com.lqm.academic_service.dtos.CurriculumResponseDTO;
import com.lqm.academic_service.mappers.CurriculumMapper;
import com.lqm.academic_service.models.Curriculum;
import com.lqm.academic_service.models.Grade;
import com.lqm.academic_service.models.Semester;
import com.lqm.academic_service.models.Subject;
import com.lqm.academic_service.services.CurriculumService;
import com.lqm.academic_service.services.GradeService;
import com.lqm.academic_service.services.SemesterService;
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
@RequestMapping("/api/internal/admin/curriculums")
public class AdminCurriculumController {

    private final CurriculumService curriculumService;
    private final CurriculumMapper curriculumMapper;
    private final GradeService gradeService;
    private final SemesterService semesterService;
    private final SubjectService subjectService;
    private final PageableUtil pageableUtil;

    private final WebAppValidator webAppValidator;

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.setValidator(webAppValidator);
    }

    @GetMapping
    public Page<CurriculumResponseDTO> getCurriculums(@RequestParam Map<String, String> params) {
        Pageable pageable = pageableUtil.getPageable(
                params.getOrDefault("page", "1"),
                PageSize.CURRICULUM_PAGE_SIZE,
                List.of()
        );

        return curriculumService.getCurriculums(params, pageable).map(curriculumMapper::toCurriculumResponseDTO);
    }

    @GetMapping("/{id}")
    public CurriculumRequestDTO getCurriculumRequestById(@PathVariable UUID id) {
        Curriculum curriculum = curriculumService.getCurriculumById(id);
        return curriculumMapper.toCurriculumRequestDTO(curriculum);
    }

    @PostMapping
    public void saveCurriculum(@RequestBody @Valid CurriculumRequestDTO dto) {
        Grade grade = gradeService.getGradeById(dto.gradeId());
        Semester semester = semesterService.getSemesterById(dto.semesterId());
        Subject subject = subjectService.getSubjectById(dto.subjectId());
        curriculumService.saveCurriculum(curriculumMapper.toEntity(dto), grade, semester, subject);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCurriculum(@PathVariable("id") UUID id) {
        curriculumService.deleteCurriculum(id);
    }
}
