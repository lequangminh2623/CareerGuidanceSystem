package com.lqm.validators;

import com.lqm.models.Semester;
import com.lqm.services.SemesterService;
import java.util.Arrays;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
/**
 *
 * @author Le Quang Minh
 */
@Component
public class SemesterValidator implements Validator, SupportsClass {

    @Autowired
    private SemesterService semesterService;

    @Override
    public Class<?> getSupportedClass() {
        return Semester.class;
    }

    @Override
    public boolean supports(@NotNull Class<?> clazz) {
        return Semester.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(@NotNull Object target, @NotNull Errors errors) {
        Semester semester = (Semester) target;

        String[] types = {"FIRST_TERM", "SECOND_TERM", "THIRD_TERM"};

        if (semester.getSemesterType() == null || semester.getSemesterType().trim().isEmpty()) {
            errors.rejectValue("semesterType", "semester.semesterType.notNull");

        } else {
            boolean notInArray = Arrays.stream(types).noneMatch(s -> s.equals(semester.getSemesterType()));
            if (notInArray) {
                errors.rejectValue("semesterType", "semester.semesterType.invalid");
            }
        }

        if (!errors.hasFieldErrors()) {
            boolean existSemesterByTypeAndAcademicYearId = semesterService.existSemesterByTypeAndAcademicYearId(semester.getSemesterType(),
                    semester.getId(), semester.getAcademicYear().getId());

            if (existSemesterByTypeAndAcademicYearId) {
                errors.rejectValue("semesterType", "semester.semesterType.unique");
            }
        }
    }
}
