package com.lqm.academic_service.controllers;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.lqm.academic_service.clients.UserClient;
import com.lqm.academic_service.dtos.SectionResponseDTO;
import com.lqm.academic_service.dtos.UserResponseDTO;
import com.lqm.academic_service.mappers.SectionMapper;

import com.lqm.academic_service.models.Section;
import com.lqm.academic_service.services.SectionService;
import com.lqm.academic_service.utils.PageSize;
import com.lqm.academic_service.utils.PageableUtil;

import jakarta.ws.rs.ForbiddenException;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/secure/sections")
@RequiredArgsConstructor
public class ApiSectionController {

    private final UserClient userClient;
    private final SectionService sectionService;
    private final PageableUtil pageableUtil;
    private final SectionMapper sectionMapper;
    private final MessageSource messageSource;

    @GetMapping
    public ResponseEntity<?> getTranscripts(@RequestParam Map<String, String> params) {
        UserResponseDTO teacher = userClient.getCurrentUser();
        params.put("teacherId", teacher.id().toString());
        Pageable pageable = pageableUtil.getPageable(
                params.getOrDefault("page", "1"),
                PageSize.SECTION_PAGE_SIZE,
                List.of());
        Page<Section> sectionPage = sectionService.getSections(params, pageable);
        Map<UUID, String> teacherMap = sectionService.buildTeacherMap(sectionPage.getContent());

        Page<SectionResponseDTO> sectionDTOPage = sectionPage
                .map(s -> sectionMapper.toSectionResponseDTO(s, teacherMap));

        return ResponseEntity.ok(Map.of("transcripts", sectionDTOPage));
    }

    @PreAuthorize("@securityService.hasPermission(#sectionId, 'SECTION', 'MANAGE_SCORES')")
    @PatchMapping("/{sectionId}/lock")
    public ResponseEntity<?> lockTranscript(@PathVariable("sectionId") UUID sectionId) {
        try {
            sectionService.lockSection(sectionId);
            return ResponseEntity.ok(messageSource.getMessage("success", null, Locale.getDefault()));
        } catch (RuntimeException e) {
            throw new ForbiddenException(e.getMessage());
        }
    }
}
