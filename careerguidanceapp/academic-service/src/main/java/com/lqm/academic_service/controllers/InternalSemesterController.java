package com.lqm.academic_service.controllers;

import com.lqm.academic_service.dtos.AcademicResponseDTO;
import com.lqm.academic_service.dtos.SemesterRequestDTO;
import com.lqm.academic_service.mappers.SemesterMapper;
import com.lqm.academic_service.models.Semester;
import com.lqm.academic_service.services.SemesterService;
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
public class InternalSemesterController {

    private final SemesterService semesterService;
    private final SemesterMapper semesterMapper;

    @GetMapping("/years/{yearId}/semesters")
    public List<AcademicResponseDTO> getSemestersByYearId(@PathVariable UUID yearId,
                                                          @RequestParam Map<String, String> params) {
        return semesterService.getSemestersByYearId(yearId, params)
                .stream()
                .map(semesterMapper::toAcademicResponseDTO)
                .toList();
    }

    @GetMapping("/semesters/{id}")
    public SemesterRequestDTO getSemesterRequestById(@PathVariable UUID id) {
        return semesterMapper.toSemesterRequestDTO(
                semesterService.getSemesterById(id)
        );
    }

    @PostMapping("/semesters")
    public AcademicResponseDTO saveSemester(@RequestBody @Valid SemesterRequestDTO dto) {
        Semester sem = semesterService.saveSemester(semesterMapper.toEntity(dto), dto.yearId());
        return semesterMapper.toAcademicResponseDTO(sem);
    }

    @DeleteMapping("/semesters/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSemesterById(@PathVariable UUID id) {
        semesterService.deleteSemesterById(id);
    }
}

