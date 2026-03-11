package com.lqm.academic_service.services.Impl;

import com.lqm.academic_service.models.StudentClassroom;
import com.lqm.academic_service.repositories.StudentClassroomRepository;
import com.lqm.academic_service.services.StudentClassroomService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class StudentClassroomServiceImpl implements StudentClassroomService {

    private final StudentClassroomRepository studentClassroomRepo;

    @Override
    public List<StudentClassroom> getStudentClassroomsByClassroomId(UUID classroomId) {
        return studentClassroomRepo.findByClassroomId(classroomId);
    }

    @Override
    public Boolean existStudentInClassroom(UUID studentId, UUID classroomId) {
        return studentClassroomRepo.existsByStudentIdAndClassroomId(studentId, classroomId);
    }
}
