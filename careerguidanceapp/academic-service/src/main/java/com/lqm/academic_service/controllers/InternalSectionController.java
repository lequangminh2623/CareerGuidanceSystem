package com.lqm.academic_service.controllers;

import com.lqm.academic_service.dtos.SectionResponseDTO;
import com.lqm.academic_service.mappers.SectionMapper;
import com.lqm.academic_service.models.ScoreStatusType;
import com.lqm.academic_service.models.Section;
import com.lqm.academic_service.services.SectionService;
import com.lqm.academic_service.services.StudentClassroomService;
import com.lqm.academic_service.utils.PageSize;
import com.lqm.academic_service.utils.PageableUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/internal/secure/sections")
@RequiredArgsConstructor
public class InternalSectionController {

    private final SectionService sectionService;
    private final PageableUtil pageableUtil;
    private final SectionMapper sectionMapper;
    private final StudentClassroomService studentClassroomService;

    @PostMapping
    Page<SectionResponseDTO> getSections(@RequestBody List<UUID> ids, @RequestParam Map<String, String> params) {
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

    @GetMapping("/{id}/teacher/{teacherId}/check")
    Boolean checkTeacherPermission(@PathVariable("id") UUID id, @PathVariable("teacherId") UUID teacherId) {
        return sectionService.existTeacherInSection(teacherId, id);
    }

    @GetMapping("/{id}/user/{userId}/check")
    Boolean checkUserPermission(@PathVariable("id") UUID id, @PathVariable("userId") UUID userId) {
        Section section = sectionService.getSectionById(id);
        return sectionService.existTeacherInSection(userId, id)
                || studentClassroomService.existStudentInClassroom(userId, section.getClassroom().getId());
    }

    @GetMapping("/{id}/locked/check")
    Boolean isLockedTranscript(@PathVariable("id") UUID id) {
        return sectionService.isLockedSection(id);
    }

    @PatchMapping("/{id}/lock")
    void lockTranscript(@PathVariable("id") UUID id) {
        sectionService.changeScoreStatus(id, ScoreStatusType.LOCKED);
    }

    @GetMapping("/{id}/check")
    Boolean checkSectionExist(@PathVariable("id") UUID id) {
        return sectionService.existSectionById(id);
    }

}
