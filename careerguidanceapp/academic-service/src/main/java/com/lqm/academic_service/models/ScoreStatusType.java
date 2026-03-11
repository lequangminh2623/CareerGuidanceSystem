package com.lqm.academic_service.models;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ScoreStatusType {
    LOCKED("Locked"),
    DRAFT("Draft");

    private final String scoreStatusName;

    public static ScoreStatusType fromScoreStatusName(String scoreStatusName) {
        for (ScoreStatusType scoreStatusType : values()) {
            if (scoreStatusType.scoreStatusName.equalsIgnoreCase(scoreStatusName)) {
                return scoreStatusType;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return scoreStatusName;
    }
}
