package com.lqm.score_service.controllers;

import com.lqm.score_service.dtos.SyncScoreRequestDTO;
import com.lqm.score_service.services.ScoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/internal/admin/scores")
@RequiredArgsConstructor
public class AdminScoreController {

    private final ScoreService scoreService;

    @DeleteMapping("/students/{studentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteScoreDetail(@PathVariable("studentId") UUID studentId, @RequestBody List<UUID> sectionIds) {
        scoreService.deleteScoreDetails(studentId, sectionIds);
    }

    @PostMapping("/sync")
    void syncScoresForClassroom(@RequestBody SyncScoreRequestDTO request) {
        scoreService.syncScoresForClassroom(request);
    }
}
