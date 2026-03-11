package com.lqm.score_service.controllers;

import com.lqm.score_service.dtos.ScoreListRequest;
import com.lqm.score_service.dtos.ScoreRequestDTO;
import com.lqm.score_service.mappers.ScoreMapper;
import com.lqm.score_service.models.ScoreDetail;
import com.lqm.score_service.services.ScoreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/internal/admin/transcripts")
@RequiredArgsConstructor
public class AdminTranscriptController {

    private final ScoreService scoreService;
    private final ScoreMapper scoreMapper;

    @GetMapping("/{sectionId}")
    public List<ScoreRequestDTO> getScoreRequests(@PathVariable("sectionId") UUID sectionId,
                                                  @RequestParam Map<String, String> params) {
        params.put("sectionId", sectionId.toString());
        Page<ScoreDetail> scoreDetails = scoreService.getScoreDetails(params, Pageable.unpaged());

        return scoreDetails.map(scoreMapper::toScoreRequestDTO).getContent();
    }

    @PostMapping("/{sectionId}")
    public void saveScores(@RequestBody @Valid ScoreListRequest request,
                           @PathVariable("sectionId") UUID sectionId) {
        List<ScoreDetail> scoreDetails = request.getScores().stream()
                .map(dto -> scoreMapper.toEntity(dto, sectionId)).toList();
        scoreService.saveScores(scoreDetails);
    }
}
