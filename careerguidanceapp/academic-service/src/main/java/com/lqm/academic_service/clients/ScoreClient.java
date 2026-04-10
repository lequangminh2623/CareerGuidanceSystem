package com.lqm.academic_service.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "api-gateway", path = "/score-service/api/internal/secure/transcripts", contextId = "scoreClient")
public interface ScoreClient {

    @GetMapping("/{id}/fully-scored/check")
    Boolean isTranscriptFullyScored(@PathVariable("id") UUID id);

}
