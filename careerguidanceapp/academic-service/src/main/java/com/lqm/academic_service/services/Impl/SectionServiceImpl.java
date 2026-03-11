package com.lqm.academic_service.services.Impl;

import com.lqm.academic_service.clients.UserClient;
import com.lqm.academic_service.dtos.UserResponseDTO;
import com.lqm.academic_service.exceptions.ResourceNotFoundException;
import com.lqm.academic_service.models.*;
import com.lqm.academic_service.repositories.SectionRepository;
import com.lqm.academic_service.services.ClassroomService;
import com.lqm.academic_service.services.CurriculumService;
import com.lqm.academic_service.services.SectionService;
import com.lqm.academic_service.specifications.SectionSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SectionServiceImpl implements SectionService {

    private final SectionRepository sectionRepo;
    private final MessageSource messageSource;
    private final ClassroomService classroomService;
    private final CurriculumService curriculumService;
    private final UserClient userClient;

    @Override
    public Page<Section> getSections(List<UUID> ids, Map<String, String> params, Pageable pageable) {
        Specification<Section> spec = SectionSpecification.filterByParams(params).and(SectionSpecification.hasIdIn(ids));
        return sectionRepo.findAll(spec, pageable);
    }

    @Override
    public Section getSectionById(UUID id) {
        return sectionRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException(
                messageSource.getMessage("section.notFound", null, Locale.getDefault())
        ));
    }

    @Override
    public void saveSections(Map<UUID, Section> curriculumSectionMap, UUID classroomId) {
        Classroom classroom = classroomService.getClassroomById(classroomId);

        Map<UUID, Curriculum> curriculumEntities = curriculumService.getCurriculums(
                curriculumSectionMap.keySet().stream().toList(),
                Map.of(),
                Pageable.unpaged()
        ).stream().collect(Collectors.toMap(Curriculum::getId, c -> c));

        curriculumSectionMap.forEach((curriculumId, section) -> {
            Curriculum curriculum = curriculumEntities.get(curriculumId);
            section.setCurriculum(curriculum);
            section.setClassroom(classroom);
        });

        sectionRepo.saveAll(curriculumSectionMap.values());
    }

    @Override
    public void changeScoreStatus(UUID id, ScoreStatusType scoreStatusType) {
        Section section = this.getSectionById(id);
        section.setScoreStatus(scoreStatusType);
        sectionRepo.save(section);
    }

    @Override
    public void deleteSection(UUID id) {
        sectionRepo.deleteById(id);
    }

    @Override
    public boolean isLockedSection(UUID id) {
        return ScoreStatusType.LOCKED.equals(this.getSectionById(id).getScoreStatus());
    }

    @Override
    public boolean existTeacherInSection(UUID teacherId, UUID sectionId) {
        return sectionRepo.existsTeacherIdById(teacherId, sectionId);
    }

    @Override
    public boolean existSectionById(UUID sectionId) {
        return sectionRepo.existsById(sectionId);
    }

    @Override
    public Map<UUID, UUID> getExistingCurriculumMap(UUID classroomId, Set<UUID> curriculumIds) {
        if (classroomId == null || curriculumIds == null || curriculumIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Object[]> results = sectionRepo.findCurriculumAndSectionIds(classroomId, curriculumIds);

        Map<UUID, UUID> map = new HashMap<>();
        for (Object[] row : results) {
            UUID currId = (UUID) row[0];
            UUID secId = (UUID) row[1];
            map.put(currId, secId);
        }
        return map;
    }

    @Override
    public Map<UUID, String> buildTeacherMap(List<Section> sections) {

        List<UUID> teacherIds = sections.stream()
                .map(Section::getTeacherId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        if (teacherIds.isEmpty()) {
            return Map.of();
        }

        Page<UserResponseDTO> userPage =
                userClient.getUsers(teacherIds, Map.of());

        return userPage.getContent().stream()
                .collect(Collectors.toMap(
                        UserResponseDTO::id,
                        user -> user.lastName() + " " + user.firstName()
                ));
    }
}
