package com.lqm.score_service.controllers;

import com.lqm.score_service.clients.ClassroomClient;
import com.lqm.score_service.clients.SectionClient;
import com.lqm.score_service.clients.UserClient;
import com.lqm.score_service.dtos.ScoreRequestDTO;
import com.lqm.score_service.dtos.SectionResponseDTO;
import com.lqm.score_service.dtos.UserResponseDTO;
import com.lqm.score_service.exceptions.ForbiddenException;
import com.lqm.score_service.mappers.ScoreMapper;
import com.lqm.score_service.models.ScoreDetail;
import com.lqm.score_service.services.ScoreService;
import com.lqm.score_service.validators.WebAppValidator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/secure/transcripts")
@CrossOrigin
@RequiredArgsConstructor
public class ApiTranscriptController {

    private final SectionClient sectionClient;
    private final UserClient userClient;
    private final ScoreService scoreService;
    private final ClassroomClient classroomClient;
    private final MessageSource messageSource;
    private final WebAppValidator webAppValidator;
    private final ScoreMapper scoreMapper;

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.setValidator(webAppValidator);
    }

    private boolean isUnauthorized(UUID sectionId) {
        UserResponseDTO teacher = userClient.getCurrentUser();

        return !sectionClient.checkTeacherPermission(teacher.id(), sectionId);
    }

    @GetMapping
    public ResponseEntity<?> getTranscripts(@RequestParam Map<String, String> params) {
        UserResponseDTO teacher = userClient.getCurrentUser();
        params.put("teacherId", teacher.id().toString());
        Page<SectionResponseDTO> sectionPage = sectionClient.getSections(List.of(), params);

        return ResponseEntity.ok(Map.of("transcripts", sectionPage));
    }

    @GetMapping("/{sectionId}/grades")
    public ResponseEntity<?> getTranscriptDetails(@PathVariable("sectionId") UUID sectionId,
                                                  @RequestParam Map<String, String> params) {
        if (isUnauthorized(sectionId)) throw new ForbiddenException(
                messageSource.getMessage("forbidden", null, Locale.getDefault())
        );

        SectionResponseDTO sectionResponseDTO = sectionClient.getSectionResponseById(sectionId);
        params.put("sectionId", sectionId.toString());
        List<ScoreDetail> scores = scoreService.getScoreDetails(params, Pageable.unpaged()).getContent();
        Map<UUID, UserResponseDTO> userMap = classroomClient.getStudentsInClassroom(
                sectionResponseDTO.classroomId(), Map.of()
                ).getContent()
                .stream()
                .collect(Collectors.toMap(UserResponseDTO::id, Function.identity()));

        return ResponseEntity.ok(Map.of(
                "section", sectionResponseDTO,
                "scores", scores.stream().map(scoreMapper::toScoreRequestDTO).toList(),
                "students", userMap
        ));
    }

    @PatchMapping("/{sectionId}/lock")
    public ResponseEntity<?> lockTranscript(@PathVariable("sectionId") UUID sectionId) {
        if (isUnauthorized(sectionId)) throw new ForbiddenException(
                messageSource.getMessage("forbidden", null, Locale.getDefault())
        );
        if (sectionClient.isLockedTranscript(sectionId)) throw new ForbiddenException(
                messageSource.getMessage("transcript.locked", null, Locale.getDefault())
        );
        if (!scoreService.isTranscriptFullyGraded(sectionId)) throw new ForbiddenException(
                messageSource.getMessage("transcript.notFullyGraded", null, Locale.getDefault())
        );
        sectionClient.lockTranscript(sectionId);

        return ResponseEntity.ok(messageSource.getMessage("success", null, Locale.getDefault()));
    }

    @PostMapping("/{sectionId}/grades")
    public ResponseEntity<String> saveGrades(@PathVariable("sectionId") UUID sectionId,
                                             @RequestBody @Valid List<ScoreRequestDTO> scoreRequests) {
        if (isUnauthorized(sectionId)) throw new ForbiddenException(
                messageSource.getMessage("forbidden", null, Locale.getDefault())
        );
        if (sectionClient.isLockedTranscript(sectionId)) throw new ForbiddenException(
                messageSource.getMessage("transcript.locked", null, Locale.getDefault())
        );
        try {
            List<ScoreDetail> scoreDetails = scoreRequests.stream().map(
                    sr -> scoreMapper.toEntity(sr, sectionId)
            ).toList();
            scoreService.saveScores(scoreDetails);
            return ResponseEntity.ok(
                    messageSource.getMessage("success", null, Locale.getDefault())
            );
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @PostMapping("/{sectionId}/grades/import")
    public ResponseEntity<String> importCsv(@PathVariable("sectionId") UUID sectionId,
                                            @RequestParam("file") MultipartFile file) {
        if (isUnauthorized(sectionId)) throw new ForbiddenException(
                messageSource.getMessage("forbidden", null, Locale.getDefault())
        );
        if (sectionClient.isLockedTranscript(sectionId)) throw new ForbiddenException(
                messageSource.getMessage("transcript.locked", null, Locale.getDefault())
        );
        try {
            scoreService.importGradesFromCsv(sectionId, file);
            return ResponseEntity.ok(messageSource.getMessage("success", null, Locale.getDefault()));
        } catch (IOException | IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi: " + e.getMessage());
        }
    }

    @GetMapping("/{sectionId}/grades/export/csv")
    public ResponseEntity<byte[]> exportCsv(@PathVariable("sectionId") UUID sectionId) {
        if (isUnauthorized(sectionId)) throw new ForbiddenException(
                messageSource.getMessage("forbidden", null, Locale.getDefault())
        );
        if (sectionClient.isLockedTranscript(sectionId)) throw new ForbiddenException(
                messageSource.getMessage("transcript.locked", null, Locale.getDefault())
        );
        byte[] csvData = scoreService.generateScoreCsv(sectionId);
        String fileName = "scores_" + sectionId + ".csv";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .header(HttpHeaders.CONTENT_TYPE, "text/csv; charset=UTF-8")
                .contentLength(csvData.length)
                .body(csvData);
    }

    @GetMapping("/{sectionId}/grades/export/pdf")
    public ResponseEntity<byte[]> exportPdf(@PathVariable("sectionId") UUID sectionId) {
        if (isUnauthorized(sectionId)) throw new ForbiddenException(
                messageSource.getMessage("forbidden", null, Locale.getDefault())
        );
        if (sectionClient.isLockedTranscript(sectionId)) throw new ForbiddenException(
                messageSource.getMessage("transcript.locked", null, Locale.getDefault())
        );
        byte[] pdfBytes = scoreService.generateScorePdf(sectionId);
        String fileName = "scores_" + sectionId + ".pdf";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(pdfBytes.length)
                .body(pdfBytes);
    }

}
