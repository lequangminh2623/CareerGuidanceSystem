package com.lqm.attendance_service.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Setter
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
@Table(name = "devices")
public class Device {
    @Id
    @Column(name = "id", updatable = false, nullable = false)
    @EqualsAndHashCode.Include
    @ToString.Include
    private String id; // Ví dụ: "1A2B3C4D5E6F"

    @Column(name = "classroom_id", unique = true)
    @ToString.Include
    private UUID classroomId;

    @Column(name = "is_active", nullable = false)
    @ToString.Include
    private Boolean isActive;
}