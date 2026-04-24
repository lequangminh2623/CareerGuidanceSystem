package com.lqm.academic_service.controllers;

import com.lqm.academic_service.dtos.AcademicResponseDTO;
import com.lqm.academic_service.dtos.GradeDetailsResponseDTO;
import com.lqm.academic_service.dtos.GradeRequestDTO;
import com.lqm.academic_service.mappers.GradeMapper;
import com.lqm.academic_service.models.Year;
import com.lqm.academic_service.services.GradeService;
import com.lqm.academic_service.services.YearService;
import com.lqm.academic_service.validators.WebAppValidator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/internal/admin/grades")
@RequiredArgsConstructor
public class AdminGradeController {

    private final GradeService gradeService;
    private final GradeMapper gradeMapper;
    private final YearService yearService;

    private final WebAppValidator webAppValidator;

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.setValidator(webAppValidator);
    }

    @GetMapping("/by-year/{yearId}")
    public List<AcademicResponseDTO> getGradesByYearId(@PathVariable UUID yearId,
                                                       @RequestParam Map<String, String> params) {
        return gradeService.getGradesByYearId(yearId, params)
                .stream()
                .map(gradeMapper::toAcademicResponseDTO)
                .toList();
    }

    @GetMapping("/details")
    public List<GradeDetailsResponseDTO> getGradesDetails(@RequestParam Map<String, String> params) {
        return gradeService.getGrades(params)
                .stream()
                .map(gradeMapper::toGradeDetailsResponseDTO)
                .toList();
    }

    @GetMapping("/{id}")
    public GradeRequestDTO getGradeRequestById(@PathVariable UUID id) {
        return gradeMapper.toGradeRequestDTO(gradeService.getGradeById(id));
    }

    @PostMapping
    public void saveGrade(@RequestBody @Valid GradeRequestDTO dto) {
        Year year = yearService.getYearById(dto.yearId());
        gradeService.saveGrade(gradeMapper.toEntity(dto), year);}

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteGradeById(@PathVariable UUID id) {
        gradeService.deleteGradeById(id);
    }
}

