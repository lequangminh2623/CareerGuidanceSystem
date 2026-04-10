package com.lqm.attendance_service.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AbsentQueueMessage implements Serializable {
    private List<String> studentIds;
    private String classroomId;
    private String date;
}
