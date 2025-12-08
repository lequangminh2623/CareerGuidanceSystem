package com.lqm.academic_service.controllers;

import com.lqm.academic_service.dtos.AcademicResponseDTO;
import com.lqm.academic_service.dtos.GradeDetailsResponseDTO;
import com.lqm.academic_service.dtos.GradeRequestDTO;
import com.lqm.academic_service.mappers.GradeMapper;
import com.lqm.academic_service.models.Grade;
import com.lqm.academic_service.services.GradeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/internal/secure")
@RequiredArgsConstructor
public class InternalGradeController {

    private final GradeService gradeService;
    private final GradeMapper gradeMapper;

    @GetMapping("/years/{yearId}/grades")
    public List<AcademicResponseDTO> getGradesByYearId(@PathVariable UUID yearId,
                                                       @RequestParam Map<String, String> params) {
        return gradeService.getGradesByYearId(yearId, params)
                .stream()
                .map(gradeMapper::toAcademicResponseDTO)
                .toList();
    }

    @GetMapping("/grades/details")
    public List<GradeDetailsResponseDTO> getGradesDetails(@RequestParam Map<String, String> params) {
        return gradeService.getGrades(params)
                .stream()
                .map(gradeMapper::toGradeDetailsResponseDTO)
                .toList();
    }

    @GetMapping("/grades/{id}")
    public GradeRequestDTO getGradeRequestById(@PathVariable UUID id) {
        return gradeMapper.toGradeRequestDTO(gradeService.getGradeById(id));
    }

    @PostMapping("/grades")
    public AcademicResponseDTO saveGrade(@RequestBody @Valid GradeRequestDTO dto) {
        Grade grade = gradeService.saveGrade(gradeMapper.toEntity(dto), dto.yearId());
        return gradeMapper.toAcademicResponseDTO(grade);
    }

    @DeleteMapping("/grades/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteGradeById(@PathVariable UUID id) {
        gradeService.deleteGradeById(id);
    }
}

