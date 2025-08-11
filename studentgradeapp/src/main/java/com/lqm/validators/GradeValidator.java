/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.lqm.validators;

import com.lqm.models.ExtraGrade;
import com.lqm.models.GradeDetail;
import com.lqm.services.GradeDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 *
 * @author Le Quang Minh
 */
@Component
public class GradeValidator implements Validator {

    @Autowired
    private GradeDetailService gradeDetailService;

    @Override
    public boolean supports(Class<?> clazz) {
        return GradeDetail.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        GradeDetail detail = (GradeDetail) target;

        if (detail.getStudent() == null || detail.getStudent().getId() == null) {
            errors.rejectValue("student", "grade.student.notNull");
        }

        if (detail.getCourse() == null || detail.getCourse().getId() == null) {
            errors.rejectValue("course", "grade.course.notNull");
        }

        if (detail.getSemester() == null || detail.getSemester().getId() == null) {
            errors.rejectValue("semester", "grade.semester.notNull");
        }
        
       if (detail.getExtraGradeSet() != null) {
            for (ExtraGrade extra : detail.getExtraGradeSet()) {
                if (extra.getGradeIndex() != null && detail.getId() != null) {
                    boolean exists = gradeDetailService.existsByGradeDetailIdAndGradeIndex(
                        detail.getId(),
                        extra.getGradeIndex(),
                        extra.getId()
                    );

                    if (exists) {
                        errors.rejectValue("extraGradeSet", "gradeDetail.extraGrade.unique");
                    }
                }
            }
        }
       
        if (!errors.hasErrors()) {
            boolean exists = gradeDetailService.existsByStudentAndCourseAndSemester(
                    detail.getStudent().getId(),
                    detail.getCourse().getId(),
                    detail.getSemester().getId(),
                    detail.getId()
            );

            if (exists) {
                errors.reject("gradeDetail", "grade.unique");
            }
        }
    }
}
