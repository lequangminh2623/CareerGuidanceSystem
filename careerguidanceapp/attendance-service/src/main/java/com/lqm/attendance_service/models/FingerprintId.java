package com.lqm.attendance_service.models;

import java.io.Serializable;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FingerprintId implements Serializable {
    private Integer fingerprintIndex;
    private UUID classroomId;
}