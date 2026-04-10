package com.lqm.attendance_service.models;

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
@IdClass(FingerprintId.class)
@Table(name = "fingerprints")
public class Fingerprint implements Serializable {

    @Id
    @Column(name = "fingerprint_index", nullable = false)
    @ToString.Include
    @EqualsAndHashCode.Include
    private Integer fingerprintIndex;

    @Id
    @Column(name = "classroom_id", nullable = false)
    @ToString.Include
    @EqualsAndHashCode.Include
    private UUID classroomId;

    @Column(name = "student_id", nullable = false)
    @ToString.Include
    private UUID studentId;
}