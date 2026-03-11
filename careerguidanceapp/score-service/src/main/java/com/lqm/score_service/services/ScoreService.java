package com.lqm.score_service.services;

import com.lqm.score_service.dtos.SyncScoreRequestDTO;
import com.lqm.score_service.models.ScoreDetail;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface ScoreService {

    Page<ScoreDetail> getScoreDetails(Map<String, String> params, Pageable pageable);

    ScoreDetail getScoreDetailByTranscriptIdAndStudentId(UUID sectionId, UUID studentId);

    void deleteScoreDetails(UUID studentId, List<UUID> transcriptIds);

    boolean isTranscriptFullyGraded(UUID sectionId);

    void syncScoresForClassroom(SyncScoreRequestDTO request);

    List<ScoreDetail> saveScores(List<ScoreDetail> scoreRequests);

    void importGradesFromCsv(UUID transcriptId, MultipartFile file) throws IOException;

    byte[] generateScoreCsv(UUID sectionId);

    byte[] generateScorePdf(UUID sectionId);

}
