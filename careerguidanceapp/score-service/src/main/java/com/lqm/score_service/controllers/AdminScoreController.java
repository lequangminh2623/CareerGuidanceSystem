package com.lqm.score_service.controllers;

import com.lqm.score_service.dtos.SyncScoreRequestDTO;
import com.lqm.score_service.services.ScoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/internal/admin/scores")
@RequiredArgsConstructor
public class AdminScoreController {

    private final ScoreService scoreService;

    @PostMapping("/sync")
    void syncScoresForClassroom(@RequestBody SyncScoreRequestDTO request) {
        scoreService.syncScoresForClassroom(request);
    }
}
