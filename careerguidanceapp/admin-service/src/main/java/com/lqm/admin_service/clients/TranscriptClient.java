package com.lqm.admin_service.clients;

import com.lqm.admin_service.dtos.ScoreListRequest;
import com.lqm.admin_service.dtos.ScoreRequestDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@FeignClient(name = "api-gateway", path = "/score-service/api/internal/admin/transcripts", contextId = "scoreClient")
public interface TranscriptClient {

    @GetMapping("/{sectionId}")
    List<ScoreRequestDTO> getScoreRequests(@PathVariable UUID sectionId,
                                           @RequestParam Map<String, String> params);

    @PostMapping("/{sectionId}")
     void saveScores(@RequestBody ScoreListRequest request,
                     @PathVariable("sectionId") UUID sectionId);
}
