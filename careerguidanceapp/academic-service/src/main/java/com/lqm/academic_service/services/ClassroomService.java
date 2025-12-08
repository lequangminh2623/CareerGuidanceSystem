package com.lqm.academic_service.services;

import com.lqm.academic_service.models.Classroom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface ClassroomService {

    Page<Classroom> getClassrooms(Map<String, String> params, Pageable pageable);

    Classroom saveClassroom(Classroom classroom, UUID gradeId, List<UUID> studentIds);

    Classroom getClassroomById(UUID id);

    void deleteClassroom(UUID id);

    Classroom getClassroomWithStudents(UUID id);

    void removeStudentFromClassroom(UUID id, UUID studentId);

    boolean existsDuplicateClassroom(String name, UUID semesterId, UUID excludeId);

    boolean existsStudentInOtherClassroom(UUID studentId, UUID gradeId, UUID excludeClassroomId);

    boolean existStudentInClassroom(UUID studentId, UUID id);

    Page<Classroom> getClassroomsByStudent(UUID studentId, Map<String, String> params, Pageable pageable);

}
