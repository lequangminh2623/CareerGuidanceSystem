/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.lqm.score_service.dtos;

import lombok.Builder;

import java.util.List;
import java.util.UUID;

@Builder
public record StudentScoreResponseDTO(
        UUID id,
        Double midtermScore,
        Double finalScore,
        List<Double> extraScores,
        String subjectName,
        String classroomName,
        String semesterName,
        String yearName
) {}
