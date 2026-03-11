package com.lqm.admin_service.dtos;

import jakarta.validation.Valid;

import java.util.List;

import lombok.*;
import java.util.ArrayList;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScoreListRequest {

        @Valid
        @Builder.Default
        private List<ScoreRequestDTO> scores = new ArrayList<>();
}