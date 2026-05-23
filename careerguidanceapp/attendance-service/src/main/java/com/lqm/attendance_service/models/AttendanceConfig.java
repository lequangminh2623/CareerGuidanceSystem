package com.lqm.attendance_service.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Setter
@Getter
@Table(name = "attendance_config")
public class AttendanceConfig {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @Column(name = "sessions_per_day", nullable = false)
    private int sessionsPerDay; // 1 or 2

    @Column(name = "morning_start_time", nullable = false)
    private LocalTime morningStartTime;

    @Column(name = "morning_end_time", nullable = false)
    private LocalTime morningEndTime;

    @Column(name = "afternoon_start_time", nullable = false)
    private LocalTime afternoonStartTime;

    @Column(name = "afternoon_end_time", nullable = false)
    private LocalTime afternoonEndTime;
}
