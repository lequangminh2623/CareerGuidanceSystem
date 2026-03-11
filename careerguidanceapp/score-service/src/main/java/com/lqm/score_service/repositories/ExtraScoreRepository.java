package com.lqm.score_service.repositories;

import com.lqm.score_service.models.ExtraScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ExtraScoreRepository  extends JpaRepository<ExtraScore, UUID> {

}
