package com.lqm.academic_service.models;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Setter
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
@Table(name = "students_classrooms",
        uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "classroom_id"}))
public class StudentClassroom implements Serializable {

    @Id
    @GeneratedValue
    @Column(name = "id", updatable = false, nullable = false)
    @ToString.Include
    private UUID id;

    @Column(name = "student_id", nullable = false)
    @EqualsAndHashCode.Include
    @ToString.Include
    private UUID studentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classroom_id", nullable = false)
    @EqualsAndHashCode.Include
    @ToString.Include
    private Classroom classroom;
}
