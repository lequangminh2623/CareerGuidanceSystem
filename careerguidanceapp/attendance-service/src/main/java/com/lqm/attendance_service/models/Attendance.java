package com.lqm.attendance_service.models;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Setter
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
@Table(name = "attendances")
public class Attendance implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    @ToString.Include
    @EqualsAndHashCode.Include
    private UUID id;

    @Column(name = "classroom_id", nullable = false)
    @ToString.Include
    private UUID classroomId;

    @Column(name = "student_id", nullable = false)
    @ToString.Include
    private UUID studentId;

    @Column(name = "attendance_date", nullable = false)
    @ToString.Include
    private LocalDate attendanceDate;

    @Column(name = "check_in_time")
    @ToString.Include
    private LocalTime checkInTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AttendanceStatus status;
}