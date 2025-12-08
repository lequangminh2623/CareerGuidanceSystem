package com.lqm.academic_service.services;

import com.lqm.academic_service.models.Subject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;
import java.util.UUID;

public interface SubjectService {

    Page<Subject> getSubjects(Map<String, String> params, Pageable pageable);

    Subject saveSubject(Subject subject);

    Subject getSubjectById(UUID id);

    void deleteSubjectById(UUID id);

    boolean existDuplicateSubject(String name, UUID excludeId);

}
