package com.lqm.academic_service.validators;

import com.lqm.academic_service.clients.UserClient;
import com.lqm.academic_service.dtos.ClassroomRequestDTO;
import com.lqm.academic_service.dtos.UserResponseDTO;
import com.lqm.academic_service.services.ClassroomService;
import com.lqm.academic_service.services.GradeService;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ClassroomRequestDTOValidator implements Validator, SupportsClass {

    private final ClassroomService classroomService;
    private final UserClient userClient;
    private final MessageSource messageSource;
    private final GradeService gradeService;

    @Override
    public Class<?> getSupportedClass() {
        return ClassroomRequestDTO.class;
    }

    @Override
    public boolean supports(@Nonnull Class<?> clazz) {
        return ClassroomRequestDTO.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(@Nonnull Object target, @Nonnull Errors errors) {
        ClassroomRequestDTO classroomRequestDTO = (ClassroomRequestDTO) target;

        if (classroomRequestDTO.name() == null || classroomRequestDTO.name().trim().isEmpty()) {
            errors.rejectValue("name", "classroom.name.notNull");
        }
        if (!errors.hasErrors()) {
            boolean exists = classroomService.existDuplicateClassroom(
                    classroomRequestDTO.name(),
                    classroomRequestDTO.gradeId(),
                    classroomRequestDTO.id()
            );

            if (exists) {
                errors.rejectValue("name", "classroom.unique");
            }
            if (classroomRequestDTO.studentIds() != null) {
                List<String> conflicts = new ArrayList<>();
                List<UserResponseDTO> users = userClient.getUsers(
                        classroomRequestDTO.studentIds(), Map.of("role", "Student")
                ).getContent();

                Map<UUID, UserResponseDTO> userMap = users.stream()
                        .collect(Collectors.toMap(UserResponseDTO::id, u -> u));

                UUID yearId = gradeService.getGradeById(classroomRequestDTO.gradeId()).getYear().getId();

                List<UUID> conflictingStudentIds = classroomService.getStudentsInOtherClassrooms(
                        classroomRequestDTO.studentIds(),
                        yearId,
                        classroomRequestDTO.id()
                );

                for (UUID studentId : conflictingStudentIds) {
                    UserResponseDTO u = userMap.get(studentId);
                    if (u != null) {
                        conflicts.add(u.code() + " - " + u.lastName() + " " + u.firstName());
                    }
                }

                if(!conflicts.isEmpty()) {
                    String msg = messageSource.getMessage("classroom.students.alreadyIn", null, Locale.getDefault())
                            + ": " + String.join(", ", conflicts);

                    errors.rejectValue(
                            "studentIds",
                            "",
                            msg
                    );
                }

            }
        }
    }
}
