package com.lqm.academic_service.models;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@Entity
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
@Table(name = "sections")
public class Section implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    @ToString.Include
    @EqualsAndHashCode.Include
    private UUID id;

    @Column(name = "teacher_id")
    @ToString.Include
    private UUID teacherId;

    @Column(name = "score_status", nullable = false)
    @Enumerated(EnumType.STRING)
    @ToString.Include
    private ScoreStatusType scoreStatus;

    @ManyToOne(optional = false)
    @JoinColumn(name = "classroom_id", referencedColumnName = "id")
    @ToString.Include
    private Classroom classroom;

    @ManyToOne(optional = false)
    @JoinColumn(name = "curriculum_id", referencedColumnName = "id")
    @ToString.Include
    private Curriculum curriculum;

}
