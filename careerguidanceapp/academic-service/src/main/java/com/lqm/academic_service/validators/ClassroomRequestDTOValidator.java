package com.lqm.academic_service.validators;

import com.lqm.academic_service.clients.UserClient;
import com.lqm.academic_service.dtos.ClassroomRequestDTO;
import com.lqm.academic_service.dtos.UserResponseDTO;
import com.lqm.academic_service.services.ClassroomService;
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

        if (classroomRequestDTO.gradeId() == null) {
            errors.rejectValue("gradeId", "classroom.grade.notNull");
        }
        if (classroomRequestDTO.name() == null || classroomRequestDTO.name().trim().isEmpty()) {
            errors.rejectValue("name", "classroom.name.notNull");
        }
        if (!errors.hasErrors()) {
            boolean exists = classroomService.existsDuplicateClassroom(
                    classroomRequestDTO.name(),
                    classroomRequestDTO.gradeId(),
                    classroomRequestDTO.id()
            );

            if (exists) {
                errors.rejectValue("name", "classroom.unique");
            }
            if (classroomRequestDTO.studentIds() != null) {
                List<String> conflicts = new ArrayList<>();
                List<UserResponseDTO> users = userClient.getUsers(classroomRequestDTO.studentIds(), Map.of("role", "STUDENT")).getContent();

                Map<UUID, UserResponseDTO> userMap = users.stream()
                        .collect(Collectors.toMap(UserResponseDTO::id, u -> u));

                for (UUID studentId : classroomRequestDTO.studentIds()) {

                    if (classroomService.existsStudentInOtherClassroom(
                            studentId,
                            classroomRequestDTO.gradeId(),
                            classroomRequestDTO.id()
                    )) {
                        UserResponseDTO u = userMap.get(studentId);
                        conflicts.add(u.lastName() + " " + u.firstName());
                    }
                }

                if(!conflicts.isEmpty()) {
                    String msg = messageSource.getMessage("classroom.students.alreadyIn", null, Locale.getDefault())
                            + " " + String.join(", ", conflicts);

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
