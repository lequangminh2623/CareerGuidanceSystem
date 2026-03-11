package com.lqm.academic_service.controllers;

import com.lqm.academic_service.dtos.AcademicResponseDTO;
import com.lqm.academic_service.dtos.YearRequestDTO;
import com.lqm.academic_service.mappers.YearMapper;
import com.lqm.academic_service.models.Year;
import com.lqm.academic_service.services.YearService;
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
@RequestMapping("/api/internal/admin/years")
@RequiredArgsConstructor
public class AdminYearController {

    private final YearService yearService;
    private final YearMapper yearMapper;
    private final PageableUtil pageableUtil;
    private final WebAppValidator webAppValidator;

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.setValidator(webAppValidator);
    }

    @GetMapping
    public Page<AcademicResponseDTO> getYears(@RequestParam Map<String, String> params) {
        Pageable pageable = pageableUtil.getPageable(
            params.getOrDefault("page", "1"),
            PageSize.YEAR_PAGE_SIZE,
            List.of("name:desc")
        );

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
    public void saveYear(@RequestBody @Valid YearRequestDTO dto) {
        yearService.saveYear(yearMapper.toEntity(dto));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteYearById(@PathVariable UUID id) {
        yearService.deleteYearById(id);
    }
}

