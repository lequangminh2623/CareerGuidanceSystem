package com.lqm.score_service.controllers;

import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lqm.score_service.services.ScoreService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/internal/secure/transcripts")
@RequiredArgsConstructor
public class InternalTranscriptController {

    private final ScoreService scoreService;

    @GetMapping("/{id}/fully-scored/check")
    Boolean isTranscriptFullyScored(@PathVariable("id") UUID id) {
        return scoreService.isTranscriptFullyScored(id);
    }

}
