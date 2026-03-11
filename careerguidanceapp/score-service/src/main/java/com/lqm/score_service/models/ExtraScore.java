package com.lqm.score_service.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Setter
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
@Table(name = "extra_scores")
public class ExtraScore implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    @ToString.Include
    private UUID id;

    @Max(10)
    @Min(0)
    @Column(name = "score")
    @ToString.Include
    private Double score;

    @Max(2)
    @Min(0)
    @Column(name = "score_index", nullable = false)
    @ToString.Include
    @EqualsAndHashCode.Include
    private Integer scoreIndex;

    @JsonIgnore
    @ManyToOne(optional = false)
    @JoinColumn(name = "score_detail_id", referencedColumnName = "id")
    @EqualsAndHashCode.Include
    private ScoreDetail scoreDetail;
}
