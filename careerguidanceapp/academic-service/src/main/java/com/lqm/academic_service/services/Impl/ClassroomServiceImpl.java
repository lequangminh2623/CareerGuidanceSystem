package com.lqm.academic_service.services.Impl;

import com.lqm.academic_service.clients.AttendanceClient;
import com.lqm.academic_service.clients.ScoreAdminClient;
import com.lqm.academic_service.dtos.SyncScoreRequestDTO;
import com.lqm.academic_service.exceptions.BadRequestException;
import com.lqm.academic_service.exceptions.ResourceNotFoundException;
import com.lqm.academic_service.models.*;
import com.lqm.academic_service.repositories.ClassroomRepository;
import com.lqm.academic_service.repositories.SectionRepository;
import com.lqm.academic_service.repositories.StudentClassroomRepository;
import com.lqm.academic_service.services.ClassroomService;
import com.lqm.academic_service.services.CurriculumService;
import com.lqm.academic_service.services.GradeService;
import com.lqm.academic_service.specifications.ClassroomSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.stream.Collectors;

@Transactional
@RequiredArgsConstructor
@Service
public class ClassroomServiceImpl implements ClassroomService {

    private final ClassroomRepository classroomRepo;
    private final MessageSource messageSource;
    private final StudentClassroomRepository studentClassroomRepo;
    private final GradeService gradeService;
    private final SectionRepository sectionRepo;
    private final CurriculumService curriculumService;
    private final ScoreAdminClient scoreAdminClient;
    private final AttendanceClient attendanceClient;

    @Override
    public Page<Classroom> getClassroomsByIds(List<UUID> ids, Map<String, String> params, Pageable pageable) {
        Specification<Classroom> spec = ClassroomSpecification.filterByParams(params)
                .and(ClassroomSpecification.hasIdIn(ids));
        return classroomRepo.findAll(spec, pageable);
    }

    @Override
    public Page<Classroom> getClassrooms(Map<String, String> params, Pageable pageable) {
        Specification<Classroom> spec = ClassroomSpecification.filterByParams(params);
        return classroomRepo.findAll(spec, pageable);
    }

    @Override
    public Classroom saveClassroom(Classroom classroom, UUID gradeId, List<UUID> studentIds) {
        boolean isNew = (classroom.getId() == null);
        if (studentIds == null)
            studentIds = new ArrayList<>();

        Set<UUID> oldStudentIds = isNew || classroom.getStudentClassroomSet() == null
                ? Collections.emptySet()
                : classroom.getStudentClassroomSet().stream()
                        .map(StudentClassroom::getStudentId)
                        .collect(Collectors.toSet());

        List<UUID> newStudentIds = studentIds.stream()
                .filter(id -> !oldStudentIds.contains(id)).toList();

        List<UUID> finalStudentIds = studentIds;
        List<UUID> removedStudentIds = oldStudentIds.stream()
                .filter(id -> !finalStudentIds.contains(id)).toList();

        classroom.setStudentClassroomSet(studentIds);
        classroom.setGrade(gradeService.getGradeById(gradeId));
        Classroom savedClassroom = classroomRepo.save(classroom);

        List<Section> sections;
        if (isNew && savedClassroom.getId() != null) {
            sections = this.initSectionForClassroom(savedClassroom, gradeId);
        } else {
            sections = classroom.getSectionSet() != null ? classroom.getSectionSet().stream().toList()
                    : new ArrayList<>();
        }

        // GỌI ĐỒNG BỘ ĐIỂM 1 LẦN DUY NHẤT NẾU CÓ SỰ THAY ĐỔI
        if (!sections.isEmpty() && (!newStudentIds.isEmpty() || !removedStudentIds.isEmpty())) {
            List<UUID> sectionIds = sections.stream().map(Section::getId).toList();

            SyncScoreRequestDTO syncRequest = new SyncScoreRequestDTO(
                    sectionIds,
                    newStudentIds,
                    removedStudentIds);

            scoreAdminClient.syncScoresForClassroom(syncRequest);
            attendanceClient.deleteAttendancesForClassroom(savedClassroom.getId(), removedStudentIds);
        }

        return savedClassroom;
    }

    private List<Section> initSectionForClassroom(Classroom classroom, UUID gradeId) {
        List<Curriculum> curriculums = curriculumService.getCurriculums(
                Map.of("gradeId", gradeId.toString()),
                Pageable.unpaged()).toList();

        List<Section> sections = curriculums.stream().map(
                c -> Section.builder().classroom(classroom).curriculum(c).scoreStatus(ScoreStatusType.DRAFT).build())
                .toList();

        return sectionRepo.saveAll(sections);
    }

    @Override
    public void deleteClassroom(UUID id) {
        if (studentClassroomRepo.existsByClassroomId(id)) {
            throw new BadRequestException(
                    messageSource.getMessage("classroom.delete.error.hasStudents", null, Locale.getDefault()));
        }
        classroomRepo.deleteById(id);
    }

    @Override
    public Classroom getClassroomWithStudents(UUID classroomId) {
        return classroomRepo.findWithStudentsById(classroomId).orElseThrow(
                () -> new ResourceNotFoundException("classroom.notFound"));
    }

    @Override
    public Classroom getClassroomById(UUID id) {
        return classroomRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException(
                messageSource.getMessage("classroom.notFound", null, Locale.getDefault())));
    }

    @Override
    public boolean existDuplicateClassroom(String name, UUID gradeId, UUID excludeId) {
        return classroomRepo.existsByNameAndGradeIdAndIdNot(name, gradeId, excludeId);
    }

    @Override
    public List<UUID> getStudentsInOtherClassrooms(List<UUID> studentIds, UUID yearId, UUID excludeClassroomId) {
        if (studentIds == null || studentIds.isEmpty()) {
            return Collections.emptyList();
        }
        return studentClassroomRepo.findStudentIdsInOtherClassrooms(
                studentIds,
                yearId,
                excludeClassroomId);
    }

    @Override
    public List<UUID> getNonExistingStudentIds(UUID classroomId, List<UUID> studentIds) {
        Set<UUID> existingIds = new HashSet<>(studentClassroomRepo.findExistingIds(classroomId, studentIds));

        return studentIds.stream()
                .distinct()
                .filter(id -> !existingIds.contains(id))
                .collect(Collectors.toList());
    }

    @Override
    public Page<Classroom> getClassroomsByStudent(UUID studentId, Map<String, String> params, Pageable pageable) {
        Page<StudentClassroom> page = studentClassroomRepo.findByStudentId(studentId, pageable);
        List<UUID> classroomIds = page.getContent().stream()
                .map(sc -> sc.getClassroom().getId())
                .toList();

        return classroomRepo.findAllByIdIn(classroomIds, pageable);
    }

}