package com.lqm.score_service.controllers;

import com.lqm.score_service.clients.ClassroomClient;
import com.lqm.score_service.clients.SectionClient;
import com.lqm.score_service.dtos.ScoreListRequestDTO;
import com.lqm.score_service.dtos.SectionResponseDTO;
import com.lqm.score_service.dtos.UserResponseDTO;
import com.lqm.score_service.exceptions.ForbiddenException;
import com.lqm.score_service.mappers.ScoreMapper;
import com.lqm.score_service.models.ScoreDetail;
import com.lqm.score_service.services.ScoreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/secure/transcripts")
@RequiredArgsConstructor
public class ApiTranscriptController {

        private final SectionClient sectionClient;
        private final ScoreService scoreService;
        private final ClassroomClient classroomClient;
        private final MessageSource messageSource;
        private final ScoreMapper scoreMapper;

        @PreAuthorize("@securityService.hasPermission(#sectionId, 'SECTION', 'MANAGE_SCORES')")
        @GetMapping("/{sectionId}/scores")
        public ResponseEntity<?> getTranscriptDetails(@PathVariable("sectionId") UUID sectionId,
                        @RequestParam Map<String, String> params) {

                SectionResponseDTO sectionResponseDTO = sectionClient.getSectionResponseById(sectionId);
                params.put("sectionId", sectionId.toString());
                List<ScoreDetail> scores = scoreService.getScoreDetails(params, Pageable.unpaged()).getContent();
                Map<UUID, UserResponseDTO> userMap = classroomClient.getStudentsInClassroom(
                                sectionResponseDTO.classroomId(), Map.of()).getContent()
                                .stream()
                                .collect(Collectors.toMap(UserResponseDTO::id, Function.identity()));

                return ResponseEntity.ok(Map.of(
                                "section", sectionResponseDTO,
                                "scores", scores.stream().map(scoreMapper::toScoreRequestDTO).toList(),
                                "students", userMap));
        }

        @PreAuthorize("@securityService.hasPermission(#sectionId, 'SECTION', 'MANAGE_SCORES')")
        @PostMapping("/{sectionId}/scores")
        public ResponseEntity<String> saveScores(@PathVariable("sectionId") UUID sectionId,
                        @RequestBody @Valid ScoreListRequestDTO scoreRequests) {
                if (sectionClient.isLockedTranscript(sectionId))
                        throw new ForbiddenException(
                                        messageSource.getMessage("transcript.locked", null, Locale.getDefault()));
                try {
                        List<ScoreDetail> scoreDetails = scoreRequests.getScores().stream().map(
                                        sr -> scoreMapper.toEntity(sr, sectionId)).toList();
                        scoreService.saveScores(scoreDetails);
                        return ResponseEntity.ok(
                                        messageSource.getMessage("success", null, Locale.getDefault()));
                } catch (IllegalArgumentException e) {
                        throw new RuntimeException(e.getMessage());
                }
        }

        @PreAuthorize("@securityService.hasPermission(#sectionId, 'SECTION', 'MANAGE_SCORES')")
        @PostMapping("/{sectionId}/scores/import")
        public ResponseEntity<String> importCsv(@PathVariable("sectionId") UUID sectionId,
                        @RequestParam("file") MultipartFile file) {
                if (sectionClient.isLockedTranscript(sectionId))
                        throw new ForbiddenException(
                                        messageSource.getMessage("transcript.locked", null, Locale.getDefault()));
                try {
                        scoreService.importScoresFromCsv(sectionId, file);
                        return ResponseEntity.ok(messageSource.getMessage("success", null, Locale.getDefault()));
                } catch (IOException | IllegalArgumentException e) {
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi: " + e.getMessage());
                }
        }

        @PreAuthorize("@securityService.hasPermission(#sectionId, 'SECTION', 'MANAGE_SCORES')")
        @GetMapping("/{sectionId}/scores/export/csv")
        public ResponseEntity<byte[]> exportCsv(@PathVariable("sectionId") UUID sectionId) {
                if (!sectionClient.isLockedTranscript(sectionId))
                        throw new ForbiddenException(
                                        messageSource.getMessage("transcript.locked", null, Locale.getDefault()));
                byte[] csvData = scoreService.generateScoreCsv(sectionId);
                String fileName = "scores_" + sectionId + ".csv";

                return ResponseEntity.ok()
                                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                                .header(HttpHeaders.CONTENT_TYPE, "text/csv; charset=UTF-8")
                                .contentLength(csvData.length)
                                .body(csvData);
        }

        @PreAuthorize("@securityService.hasPermission(#sectionId, 'SECTION', 'MANAGE_SCORES')")
        @GetMapping("/{sectionId}/scores/export/pdf")
        public ResponseEntity<byte[]> exportPdf(@PathVariable("sectionId") UUID sectionId) {
                if (!sectionClient.isLockedTranscript(sectionId))
                        throw new ForbiddenException(
                                        messageSource.getMessage("transcript.locked", null, Locale.getDefault()));
                byte[] pdfBytes = scoreService.generateScorePdf(sectionId);
                String fileName = "scores_" + sectionId + ".pdf";

                return ResponseEntity.ok()
                                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                                .contentType(MediaType.APPLICATION_PDF)
                                .contentLength(pdfBytes.length)
                                .body(pdfBytes);
        }

}
