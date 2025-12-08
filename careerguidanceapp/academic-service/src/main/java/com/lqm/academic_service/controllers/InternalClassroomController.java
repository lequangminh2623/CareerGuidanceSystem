package com.lqm.academic_service.controllers;

import com.lqm.academic_service.dtos.ClassroomRequestDTO;
import com.lqm.academic_service.dtos.ClassroomResponseDTO;
import com.lqm.academic_service.mappers.ClassroomMapper;
import com.lqm.academic_service.models.Classroom;
import com.lqm.academic_service.services.ClassroomService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/internal/secure/classrooms")
public class InternalClassroomController {

    private final ClassroomService classroomService;
    private final ClassroomMapper classroomMapper;

    @GetMapping
    public Page<ClassroomResponseDTO> getClassrooms(
            @RequestParam Map<String, String> params,
            Pageable pageable) {
        return classroomService.getClassrooms(params, pageable)
                .map(classroomMapper::toClassroomResponseDTO);
    }

    @GetMapping("/{id}")
    public ClassroomRequestDTO getClassroomRequestById(@PathVariable UUID id) {
        Classroom classroom = classroomService.getClassroomWithStudents(id);
        return classroomMapper.toClassroomRequestDTO(classroom);
    }

    @PostMapping
    public ClassroomResponseDTO saveClassroom(@RequestBody ClassroomRequestDTO dto) {
        UUID id = dto.id();
        Classroom classroom = id != null
                ? classroomService.getClassroomWithStudents(id)
                : new Classroom();

        classroomMapper.updateEntity(classroom, dto);
        Classroom savedClassroom = classroomService.saveClassroom(classroom, dto.gradeId(), dto.studentIds());

        return classroomMapper.toClassroomResponseDTO(savedClassroom);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteClassroom(@PathVariable("id") UUID id) {
        classroomService.deleteClassroom(id);
    }

    @DeleteMapping("/{classroomId}/students/{studentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeStudent(
            @PathVariable UUID classroomId,
            @PathVariable UUID studentId) {
        classroomService.removeStudentFromClassroom(classroomId, studentId);
    }
}

