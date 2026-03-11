package com.lqm.score_service.dtos;

import com.lqm.score_service.annotations.ValidExtraScores;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScoreRequestDTO {

    @NotNull
    private UUID studentId;

    @DecimalMin(value = "0.0")
    @DecimalMax(value = "10.0")
    private Double midtermScore;

    @DecimalMin(value = "0.0")
    @DecimalMax(value = "10.0")
    private Double finalScore;

    @ValidExtraScores
    @Builder.Default
    private List<Double> extraScores = new ArrayList<>();
}