package com.lqm.validators;

import com.lqm.models.Course;
import com.lqm.services.CourseService;
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
public class CourseValidator implements Validator, SupportsClass {

    @Autowired
    private CourseService courseService;

    @Override
    public Class<?> getSupportedClass() {
        return Course.class;
    }

    @Override
    public boolean supports(@NotNull Class<?> clazz) {
        return Course.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(@NotNull Object target, @NotNull Errors errors) {
        Course course = (Course) target;

        if (course.getName() == null || course.getName().trim().isEmpty()) {
            errors.rejectValue("name", "course.name.notNull");
        }

        if (course.getCredit() == 0) {
            errors.rejectValue("credit", "course.credit.notNull");
        }

        if (!errors.hasFieldErrors()) {
            boolean existCourseByName = courseService.existCourseByName(course.getName(), course.getId());

            if (existCourseByName) {
                errors.rejectValue("name", "course.name.unique");
            }
        }
    }
}
