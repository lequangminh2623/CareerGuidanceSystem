/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.lqm.validators;

import com.lqm.dtos.UserDTO;
import com.lqm.services.StudentService;
import com.lqm.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 *
 * @author Le Quang Minh
 */
@Component
public class UserDTOValidator implements Validator {

    @Autowired
    private UserService userService;

    @Autowired
    private StudentService studentService;

    @Override
    public boolean supports(Class<?> clazz) {
        return UserDTO.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        UserDTO user = (UserDTO) target;

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
                    null
            );
            if (existEmail) {
                errors.rejectValue("email", "user.email.unique");
            }
        }

        if (user.getCode() == null || user.getCode().trim().isEmpty()) {
            errors.rejectValue("code", "user.student.code.notNull");
        } else {
            boolean existCode = studentService.existsByStudentCode(user.getCode(), null);
            if (existCode) {
                errors.rejectValue("code", "user.student.code.unique");
            }
        }

        if (user.getFile() == null || user.getFile().isEmpty()) {
            errors.rejectValue("avatar", "user.avatar.notNull");
        }

    }
}
