package com.lqm.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

/**
 * DTO for career orientation data
 * 
 * @author Le Quang Minh
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrientationDTO {

    // Fields from frontend request
    @JsonProperty("part_time_job")
    private Integer partTimeJob;

    @JsonProperty("extracurricular_activities")
    private Integer extracurricularActivities;

    @JsonProperty("absence_days")
    private Integer absenceDays;

    @JsonProperty("weekly_self_study_hours")
    private Integer weeklySelfStudyHours;

    // Fields for Python service response
    @JsonProperty("career_orientation")
    private String careerOrientation;

    private Double mathScore;
    private Double historyScore;
    private Double physicsScore;
    private Double chemistryScore;
    private Double biologyScore;
    private Double englishScore;
    private Double geographyScore;

    // Constructor for request data only
    public OrientationDTO(Integer partTimeJob, Integer extracurricularActivities, 
                         Integer absenceDays, Integer weeklySelfStudyHours) {
        this.partTimeJob = partTimeJob;
        this.extracurricularActivities = extracurricularActivities;
        this.absenceDays = absenceDays;
        this.weeklySelfStudyHours = weeklySelfStudyHours;
    }

    public OrientationDTO(
            Double mathScore,
            Double historyScore,
            Double physicsScore,
            Double chemistryScore,
            Double biologyScore,
            Double englishScore,
            Double geographyScore
    ) {
        this.mathScore = mathScore;
        this.historyScore = historyScore;
        this.physicsScore = physicsScore;
        this.chemistryScore = chemistryScore;
        this.biologyScore = biologyScore;
        this.englishScore = englishScore;
        this.geographyScore = geographyScore;

    }

}
