package com.lqm.academic_service.services.Impl;

import com.lqm.academic_service.exceptions.ResourceNotFoundException;
import com.lqm.academic_service.models.Subject;
import com.lqm.academic_service.repositories.SubjectRepository;
import com.lqm.academic_service.services.SubjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Transactional
@RequiredArgsConstructor
@Service
public class SubjectServiceImpl implements SubjectService {

    private final SubjectRepository subjectRepo;
    private final MessageSource messageSource;

    @Override
    public Page<Subject> getSubjects(Map<String, String> params, Pageable pageable) {
        String kw = (params != null) ? params.get("kw") : null;
        return subjectRepo.findAllByKeyword(kw, pageable);
    }

    @Override
    public Subject saveSubject(Subject subject) {
        return subjectRepo.save(subject);
    }

    @Override
    public Subject getSubjectById(UUID id) {
        return subjectRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        messageSource.getMessage("subject.notFound", null, Locale.getDefault()))
                );
    }

    @Override
    public Subject getSubjectByName(String name) {
        return subjectRepo.findByName(name);
    }

    @Override
    public void deleteSubjectById(UUID id) {
        subjectRepo.deleteById(id);
    }

    @Override
    public boolean existDuplicateSubject(String name, UUID excludeId) {
        return subjectRepo.existsByNameAndIdNot(name, excludeId);
    }

}
