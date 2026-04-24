package com.lqm.academic_service.controllers;

import com.lqm.academic_service.dtos.AcademicResponseDTO;
import com.lqm.academic_service.dtos.SemesterRequestDTO;
import com.lqm.academic_service.mappers.SemesterMapper;
import com.lqm.academic_service.models.Year;
import com.lqm.academic_service.services.SemesterService;
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
@RequestMapping("/api/internal/admin/semesters")
@RequiredArgsConstructor
public class AdminSemesterController {

    private final SemesterService semesterService;
    private final SemesterMapper semesterMapper;
    private final YearService yearService;
    private final WebAppValidator webAppValidator;

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.setValidator(webAppValidator);
    }

    @GetMapping("/by-year/{yearId}")
    public List<AcademicResponseDTO> getSemestersByYearId(@PathVariable UUID yearId,
                                                          @RequestParam Map<String, String> params) {
        return semesterService.getSemestersByYearId(yearId, params)
                .stream()
                .map(semesterMapper::toAcademicResponseDTO)
                .toList();
    }

    @GetMapping("/{id}")
    public SemesterRequestDTO getSemesterRequestById(@PathVariable UUID id) {
        return semesterMapper.toSemesterRequestDTO(
                semesterService.getSemesterById(id)
        );
    }

    @PostMapping
    public void saveSemester(@RequestBody @Valid SemesterRequestDTO dto) {
        Year year = yearService.getYearById(dto.yearId());
        semesterService.saveSemester(semesterMapper.toEntity(dto), year);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSemesterById(@PathVariable UUID id) {
        semesterService.deleteSemesterById(id);
    }
}

