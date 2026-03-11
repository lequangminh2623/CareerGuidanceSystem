package com.lqm.score_service.mappers;

import com.lqm.score_service.dtos.ScoreRequestDTO;
import com.lqm.score_service.dtos.StudentScoreResponseDTO;
import com.lqm.score_service.models.ExtraScore;
import com.lqm.score_service.models.ScoreDetail;
import org.mapstruct.*;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface ScoreMapper {

    StudentScoreResponseDTO toStudentScoreResponseDTO(ScoreDetail entity, String studentNames);

    @Mapping(target = "extraScores", source = "extraScoreSet", qualifiedByName = "extraScoreToDouble")
    ScoreRequestDTO toScoreRequestDTO(ScoreDetail entity);

    @Named("extraScoreToDouble")
    default Double mapExtraScoreToDouble(ExtraScore extraScore) {
        return extraScore != null ? extraScore.getScore() : null;
    }

    @Mapping(target = "extraScoreSet", source = "dto.extraScores", qualifiedByName = "mapExtraScoresWithIndex")
    ScoreDetail toEntity(ScoreRequestDTO dto, UUID sectionId);

    @Named("mapExtraScoresWithIndex")
    default Set<ExtraScore> mapExtraScoresWithIndex(List<Double> scores) {
        Set<ExtraScore> result = new LinkedHashSet<>();
        for (int i = 0; i < scores.size(); i++) {
            Double val = scores.get(i);

            ExtraScore es = ExtraScore.builder()
                    .score(val)
                    .scoreIndex(i)
                    .build();

            result.add(es);
        }
        return result;
    }
}
