package com.lqm.academic_service.controllers;

import com.lqm.academic_service.dtos.ChangeStatusRequestDTO;
import com.lqm.academic_service.dtos.SectionListRequest;
import com.lqm.academic_service.dtos.SectionRequestDTO;
import com.lqm.academic_service.dtos.SectionResponseDTO;
import com.lqm.academic_service.mappers.SectionMapper;
import com.lqm.academic_service.models.*;
import com.lqm.academic_service.services.SectionService;
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
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/internal/admin/sections")
public class AdminSectionController {

    private final WebAppValidator webAppValidator;
    private final PageableUtil pageableUtil;
    private final SectionService sectionService;
    private final SectionMapper sectionMapper;

    @InitBinder("sectionListRequest")
    public void initBinder(WebDataBinder binder) {
        binder.setValidator(webAppValidator);
    }

    @GetMapping("/request")
    public Page<SectionRequestDTO> getSectionRequests(@RequestParam Map<String, String> params) {
        Pageable pageable = pageableUtil.getPageable(
                params.getOrDefault("page", "1"),
                PageSize.SECTION_PAGE_SIZE,
                List.of()
        );

        return sectionService.getSections(List.of(), params, pageable).map(sectionMapper::toSectionRequestDTO);
    }

    @PostMapping("/response")
    Page<SectionResponseDTO> getSectionResponses(@RequestBody List<UUID> ids, @RequestParam Map<String, String> params) {
        Pageable pageable = pageableUtil.getPageable(
                params.getOrDefault("page", "1"),
                PageSize.SECTION_PAGE_SIZE,
                List.of()
        );
        Page<Section> sectionPage = sectionService.getSections(ids, params, pageable);
        Map<UUID, String> teacherMap = sectionService.buildTeacherMap(sectionPage.getContent());

        return sectionPage.map(s -> sectionMapper.toSectionResponseDTO(s, teacherMap));
    }

    @GetMapping("/{id}")
    SectionResponseDTO getSectionResponseById(@PathVariable("id") UUID id) {
        Section section = sectionService.getSectionById(id);
        Map<UUID, String> teacherMap = sectionService.buildTeacherMap(List.of(section));

        return sectionMapper.toSectionResponseDTO(section, teacherMap);
    }

    @PostMapping
    public void saveSections(@RequestBody @Valid SectionListRequest sectionListRequest,
                             @RequestParam Map<String, String> params) {
        UUID classroomId = UUID.fromString(params.get("classroomId"));
        Map<UUID, Section> curriculumSectionMap = sectionListRequest.sections().stream().collect(
                Collectors.toMap(SectionRequestDTO::curriculumId, sectionMapper::toEntity)
        );
        sectionService.saveSections(curriculumSectionMap, classroomId);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSection(@PathVariable("id") UUID id) {
        sectionService.deleteSection(id);
    }

    @PatchMapping("/{id}/change-status")
    void changeTranscriptStatus(@PathVariable("id") UUID id, @Valid @RequestBody ChangeStatusRequestDTO request) {
        sectionService.changeScoreStatus(id, ScoreStatusType.fromScoreStatusName(request.status()));
    }

}
