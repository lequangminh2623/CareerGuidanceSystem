package com.lqm.academic_service.services.Impl;

import com.lqm.academic_service.exceptions.BadRequestException;
import com.lqm.academic_service.exceptions.ResourceNotFoundException;
import com.lqm.academic_service.models.Classroom;
import com.lqm.academic_service.models.StudentClassroom;
import com.lqm.academic_service.repositories.ClassroomRepository;
import com.lqm.academic_service.repositories.StudentClassroomRepository;
import com.lqm.academic_service.services.ClassroomService;
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

@Transactional
@RequiredArgsConstructor
@Service
public class ClassroomServiceImpl implements ClassroomService {

    private final ClassroomRepository classroomRepo;
    private final MessageSource messageSource;
    private final StudentClassroomRepository studentClassroomRepo;
    private final GradeService gradeService;

    @Override
    public Page<Classroom> getClassrooms(Map<String, String> params, Pageable pageable) {
        Specification<Classroom> spec = ClassroomSpecification.filterByParams(params);
        return classroomRepo.findAll(spec, pageable);
    }

    @Override
    public Classroom saveClassroom(Classroom classroom, UUID gradeId, List<UUID> studentIds) {
        classroom.setStudentClassroomSet(studentIds);
        classroom.setGrade(gradeService.getGradeById(gradeId));
        Classroom savedClassroom = classroomRepo.save(classroom);
        return this.getClassroomById(savedClassroom.getId());
    }

    @Override
    public Classroom getClassroomById(UUID id) {
        return classroomRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        messageSource.getMessage("classroom.notFound", null, Locale.getDefault()))
                );
    }

    @Override
    public void deleteClassroom(UUID id) {
        if (studentClassroomRepo.existsByClassroomId(id)) {
            throw new BadRequestException(
                    messageSource.getMessage("classroom.delete.error.hasStudents", null, Locale.getDefault())
            );
        }
        classroomRepo.deleteById(id);
    }

    @Override
    public Classroom getClassroomWithStudents(UUID classroomId) {
        return classroomRepo.findWithStudentsById(classroomId).orElseThrow(
                () -> new ResourceNotFoundException("classroom.notFound")
        );
    }

    @Override
    public boolean existsDuplicateClassroom(String name, UUID gradeId, UUID excludeId) {
        return classroomRepo.existsByNameAndGradeIdAndIdNot(name, gradeId, excludeId);
    }

    @Override
    public void removeStudentFromClassroom(UUID classroomId, UUID studentId) {
        studentClassroomRepo.deleteByClassroomIdAndStudentId(classroomId, studentId);
    }

    @Override
    public boolean existsStudentInOtherClassroom(UUID studentId, UUID gradeId, UUID excludeClassroomId) {
        return studentClassroomRepo
                .existsByStudentIdAndClassroom_Grade_IdAndClassroom_IdNot(studentId, gradeId, excludeClassroomId);
    }

    @Override
    public boolean existStudentInClassroom(UUID studentId, UUID classroomId) {
        return studentClassroomRepo.existsByStudentIdAndClassroomId(studentId, classroomId);
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