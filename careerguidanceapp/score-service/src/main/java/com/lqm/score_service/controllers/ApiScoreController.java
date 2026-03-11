package com.lqm.score_service.controllers;

import com.lqm.score_service.clients.SectionClient;
import com.lqm.score_service.clients.UserClient;
import com.lqm.score_service.dtos.SectionResponseDTO;
import com.lqm.score_service.dtos.StudentScoreResponseDTO;
import com.lqm.score_service.dtos.UserResponseDTO;
import com.lqm.score_service.mappers.ScoreMapper;
import com.lqm.score_service.models.ScoreDetail;
import com.lqm.score_service.services.ScoreService;
import jakarta.ws.rs.ForbiddenException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/secure/scores")
@CrossOrigin
@RequiredArgsConstructor
public class ApiScoreController {
    
    private final UserClient userClient;
    private final ScoreService scoreService;
    private final SectionClient sectionClient;
    private final ScoreMapper scoreMapper;
    private final MessageSource messageSource;

    @GetMapping("/currentStudent")
    public ResponseEntity<?> getStudentScores( @RequestParam Map<String, String> params) {
        UserResponseDTO currentUser = userClient.getCurrentUser();

        if (currentUser.code() == null) {
            throw new ForbiddenException(
                    messageSource.getMessage("forbidden", null, Locale.getDefault())
            );
        }

        params.put("studentId", currentUser.id().toString());
        List<ScoreDetail> scoreDetails = scoreService.getScoreDetails(params, Pageable.unpaged()).getContent();
        List<UUID> sectionIds = scoreDetails.stream().map(ScoreDetail::getSectionId).toList();
        Page<SectionResponseDTO> sections = sectionClient.getSections(sectionIds, params);
        Map<UUID, String> subjectNameMap = sections.getContent().stream()
                .collect(Collectors.toMap(SectionResponseDTO::id, SectionResponseDTO::subjectName));
        List<StudentScoreResponseDTO> dtoList = scoreDetails.stream().map(
                sd -> scoreMapper.toStudentScoreResponseDTO(sd, subjectNameMap.get(sd.getSectionId()))
        ).toList();

        return ResponseEntity.ok(dtoList);
    }
}
