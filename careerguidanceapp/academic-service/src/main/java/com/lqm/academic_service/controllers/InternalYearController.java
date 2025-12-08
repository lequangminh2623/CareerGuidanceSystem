package com.lqm.academic_service.controllers;

import com.lqm.academic_service.dtos.AcademicResponseDTO;
import com.lqm.academic_service.dtos.YearRequestDTO;
import com.lqm.academic_service.mappers.YearMapper;
import com.lqm.academic_service.models.Year;
import com.lqm.academic_service.services.YearService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/internal/secure/years")
@RequiredArgsConstructor
public class InternalYearController {

    private final YearService yearService;
    private final YearMapper yearMapper;

    @GetMapping
    public Page<AcademicResponseDTO> getYears(@RequestParam Map<String, String> params, Pageable pageable) {
        return yearService.getYears(params, pageable)
                .map(yearMapper::toAcademicResponseDTO);
    }

    @GetMapping("/{id}/request")
    public YearRequestDTO getYearRequestById(@PathVariable UUID id) {
        return yearMapper.toYearRequestDTO(yearService.getYearById(id));
    }

    @GetMapping("/{id}/response")
    AcademicResponseDTO getYearResponseById(@PathVariable UUID id) {
        return yearMapper.toAcademicResponseDTO(yearService.getYearById(id));
    }

    @PostMapping
    public AcademicResponseDTO saveYear(@RequestBody @Valid YearRequestDTO dto) {
        Year year = yearService.saveYear(yearMapper.toEntity(dto));
        return yearMapper.toAcademicResponseDTO(year);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteYearById(@PathVariable UUID id) {
        yearService.deleteYearById(id);
    }
}

