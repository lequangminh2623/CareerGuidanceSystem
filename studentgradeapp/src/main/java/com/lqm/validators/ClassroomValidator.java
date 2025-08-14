/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.lqm.validators;

import com.lqm.models.Classroom;
import com.lqm.services.ClassroomService;
import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 *
 * @author Le Quang Minh
 */
@Component
public class ClassroomValidator implements Validator, SupportsClass {

    @Autowired
    private ClassroomService classroomService;

    @Override
    public Class<?> getSupportedClass() {
        return Classroom.class;
    }

    @Override
    public boolean supports(@NotNull Class<?> clazz) {
        return Classroom.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(@NotNull Object target, @NotNull Errors errors) {
        Classroom classroom = (Classroom) target;
        System.out.println("Running classroom validator...");

        if (classroom.getName() == null || classroom.getName().trim().isEmpty()) {
            errors.rejectValue("name", "classroom.name.notNull");
        }

        if (classroom.getSemester() == null || classroom.getSemester().getId() == null) {
            errors.rejectValue("semester", "classroom.semester.notNull");
        }

        if (classroom.getCourse() == null || classroom.getCourse().getId() == null) {
            errors.rejectValue("course", "classroom.course.notNull");
        }

        if (classroom.getLecturer() == null || classroom.getLecturer().getId() == null) {
            errors.rejectValue("lecturer", "classroom.lecturer.notNull");
        }
        if (!errors.hasErrors()) {
            boolean exists = classroomService.existsDuplicateClassroom(
                    classroom.getName(),
                    classroom.getSemester().getId(),
                    classroom.getCourse().getId(),
                    classroom.getId()
            );

            if (exists) {
                errors.rejectValue("name", "classroom.unique");
            }
            if (classroom.getStudentSet() != null) {
                List<String> conflicts = new ArrayList<>();

                for (var student : classroom.getStudentSet()) {
                    boolean alreadyInClass = classroomService.existsStudentInOtherClassroom(
                            student.getId(),
                            classroom.getSemester().getId(),
                            classroom.getCourse().getId(),
                            classroom.getId()
                    );

                    if (alreadyInClass) {
                        String fullName = student.getUser().getLastName() + " " + student.getUser().getFirstName();
                        conflicts.add(fullName);
                    }
                }

                if (!conflicts.isEmpty()) {
                    String msg = "Các sinh viên đã tham gia lớp khác: " + String.join(", ", conflicts);
                    errors.rejectValue("studentSet", null, msg);
                }
            }
        }
    }
}
