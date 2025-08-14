package com.lqm.validators;

import com.lqm.models.AcademicYear;
import com.lqm.services.AcademicYearService;
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
public class AcademicYearValidator implements Validator, SupportsClass {

    @Autowired
    private AcademicYearService academicYearService;

    @Override
    public Class<?> getSupportedClass() {
        return AcademicYear.class;
    }

    @Override
    public boolean supports(@NotNull Class<?> clazz) {
        return AcademicYear.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(@NotNull Object target, @NotNull Errors errors) {
        AcademicYear year = (AcademicYear) target;

        if (year.getYear() == null || year.getYear().trim().isEmpty()) {
            errors.rejectValue("year", "academicYear.year.notNull");
        }

        if (!errors.hasFieldErrors()) {
            boolean existYearByYear = academicYearService.existAcademicYearByYear(year.getYear(), year.getId());

            if (existYearByYear) {
                errors.rejectValue("year", "academicYear.year.unique");
            }
        }
    }
}
