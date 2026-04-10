package com.lqm.academic_service.services;

import com.lqm.academic_service.models.Classroom;
import com.lqm.academic_service.models.StudentClassroom;

import java.util.List;
import java.util.UUID;

public interface StudentClassroomService {
    List<StudentClassroom> getStudentClassroomsByClassroomId(UUID classroomId);

    Boolean existStudentInClassroom(UUID studentId, UUID classroomId);

    List<Classroom> getClassroomsByStudentId(UUID studentId);
}
