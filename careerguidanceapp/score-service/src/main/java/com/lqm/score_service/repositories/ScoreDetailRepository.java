package com.lqm.score_service.repositories;

import com.lqm.score_service.models.ScoreDetail;
import jakarta.annotation.Nonnull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ScoreDetailRepository extends JpaRepository<ScoreDetail, UUID>, JpaSpecificationExecutor<ScoreDetail> {

    @EntityGraph(value = "extraScoreSet", type = EntityGraph.EntityGraphType.FETCH)
    @Nonnull
    Page<ScoreDetail> findAll(@Nonnull Specification<ScoreDetail> spec, @Nonnull Pageable pageable);

    @EntityGraph(value = "extraScoreSet", type = EntityGraph.EntityGraphType.FETCH)
    Optional<ScoreDetail> findBySectionIdAndStudentId(
            @Param("transcriptId") UUID transcriptId,
            @Param("studentId") UUID studentId
    );

    @Query("SELECT COUNT(sd) FROM ScoreDetail sd " +
            "LEFT JOIN sd.extraScoreSet es " +
            "WHERE sd.sectionId = :sectionId " +
            "AND (sd.midtermScore IS NULL " +
            "     OR sd.finalScore IS NULL " +
            "     OR (es IS NOT NULL AND es.score IS NULL))")
    int countIncompleteScores(@Param("sectionId") String sectionId);

    void deleteAllByStudentIdAndSectionIdIn(UUID studentId, List<UUID> sectionIds);

    ScoreDetail findFirstBySectionId(UUID sectionId);

    List<ScoreDetail> findBySectionId(UUID sectionId);

    void deleteAllBySectionIdInAndStudentIdIn(List<UUID> sectionIds, List<UUID> studentIds);
}