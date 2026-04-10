package com.lqm.academic_service.controllers;

import com.lqm.academic_service.clients.UserClient;
import com.lqm.academic_service.dtos.ClassroomRequestDTO;
import com.lqm.academic_service.dtos.ClassroomResponseDTO;
import com.lqm.academic_service.dtos.UserResponseDTO;
import com.lqm.academic_service.mappers.ClassroomMapper;
import com.lqm.academic_service.models.Classroom;
import com.lqm.academic_service.models.StudentClassroom;
import com.lqm.academic_service.services.ClassroomService;
import com.lqm.academic_service.services.StudentClassroomService;
import com.lqm.academic_service.utils.PageSize;
import com.lqm.academic_service.utils.PageableUtil;
import com.lqm.academic_service.validators.WebAppValidator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/internal/admin/classrooms")
public class AdminClassroomController {

    private final ClassroomService classroomService;
    private final ClassroomMapper classroomMapper;
    private final PageableUtil pageableUtil;

    private final WebAppValidator webAppValidator;
    private final UserClient userClient;
    private final StudentClassroomService studentClassroomService;

    @InitBinder()
    public void initBinder(WebDataBinder binder) {
        binder.setValidator(webAppValidator);
    }

    @GetMapping
    public Page<ClassroomResponseDTO> getClassrooms(@RequestParam Map<String, String> params) {
        Pageable pageable = pageableUtil.getPageable(
                params.getOrDefault("page", "1"),
                PageSize.CLASSROOM_PAGE_SIZE,
                List.of()
        );

        return classroomService.getClassrooms(List.of(), params, pageable).map(classroomMapper::toClassroomResponseDTO);
    }

    @GetMapping("/{id}/request")
    public ClassroomRequestDTO getClassroomRequestById(@PathVariable UUID id) {
        Classroom classroom = classroomService.getClassroomWithStudents(id);
        return classroomMapper.toClassroomRequestDTO(classroom);
    }

    @GetMapping("/{id}/response")
    public ClassroomResponseDTO getClassroomResponseById(@PathVariable UUID id) {
        Classroom classroom = classroomService.getClassroomById(id);
        return classroomMapper.toClassroomResponseDTO(classroom);
    }

    @PostMapping
    public void saveClassroom(@RequestBody @Valid ClassroomRequestDTO dto) {
        UUID id = dto.id();
        Classroom classroom = id != null
                ? classroomService.getClassroomWithStudents(id)
                : new Classroom();
        classroomMapper.updateEntity(classroom, dto);
        classroomService.saveClassroom(classroom, dto.gradeId(), dto.studentIds());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteClassroom(@PathVariable("id") UUID id) {
        classroomService.deleteClassroom(id);
    }

    @GetMapping("/{classroomId}/students")
    public Page<UserResponseDTO> getStudentsInClassroom(@PathVariable UUID classroomId,
                                                        @RequestParam Map<String, String> params) {
        params.put("page", "");
        return userClient.getUsers(studentClassroomService.getStudentClassroomsByClassroomId(classroomId).stream()
                .map(StudentClassroom::getStudentId).toList(), params);
    }
}

