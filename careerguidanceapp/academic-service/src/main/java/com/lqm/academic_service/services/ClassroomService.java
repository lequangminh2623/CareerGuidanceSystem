package com.lqm.academic_service.services;

import com.lqm.academic_service.models.Classroom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface ClassroomService {

    Page<Classroom> getClassroomsByIds(List<UUID> ids, Map<String, String> params, Pageable pageable);

    Page<Classroom> getClassrooms(Map<String, String> params, Pageable pageable);

    Classroom saveClassroom(Classroom classroom, UUID gradeId, List<UUID> studentIds);

    void deleteClassroom(UUID id);

    Classroom getClassroomWithStudents(UUID id);

    Classroom getClassroomById(UUID id);

    boolean existDuplicateClassroom(String name, UUID semesterId, UUID excludeId);

    List<UUID> getStudentsInOtherClassrooms(List<UUID> studentIds, UUID yearId, UUID excludeClassroomId);

    List<UUID> getNonExistingStudentIds(UUID classroomId, List<UUID> studentIds);

    Page<Classroom> getClassroomsByStudent(UUID studentId, Map<String, String> params, Pageable pageable);

}
