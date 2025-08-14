/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.lqm.validators;

import com.lqm.models.User;
import com.lqm.services.StudentService;
import com.lqm.services.UserService;
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
public class UserValidator implements Validator, SupportsClass {

    @Autowired
    private UserService userService;
    
    @Autowired
    private StudentService studentService;

    @Override
    public Class<?> getSupportedClass() {
        return User.class;
    }

    @Override
    public boolean supports(@NotNull Class<?> clazz) {
        return User.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(@NotNull Object target, @NotNull Errors errors) {
        User user = (User) target;

        if (user.getFirstName() == null || user.getFirstName().trim().isEmpty()) {
            errors.rejectValue("firstName", "user.firstName.notNull");
        }

        if (user.getLastName() == null || user.getLastName().trim().isEmpty()) {
            errors.rejectValue("lastName", "user.lastName.notNull");
        }

        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            errors.rejectValue("email", "user.email.notNull");
        }


        if (!errors.hasFieldErrors("email")) {
            boolean existEmail = userService.existsByEmail(
                    user.getEmail(),
                    user.getId()
            );
            if (existEmail) {
                errors.rejectValue("email", "user.email.unique");
            }
        }
        
        if ("ROLE_STUDENT".equals(user.getRole())) {
            if (user.getStudent() != null && (user.getStudent().getCode() == null || user.getStudent().getCode().trim().isEmpty())) {
                errors.rejectValue("student.code", "user.student.code.notNull");
            }

            if (user.getStudent() != null && user.getStudent().getCode() != null) {
                boolean existCode = studentService.existsByStudentCode(user.getStudent().getCode(), user.getId());
                if (existCode) {
                    errors.rejectValue("student.code", "user.student.code.unique");
                }
            }
        }
    }
}
