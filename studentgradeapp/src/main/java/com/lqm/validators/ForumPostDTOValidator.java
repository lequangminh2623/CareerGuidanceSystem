/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.lqm.validators;

import com.lqm.dtos.ForumPostDTO;
import com.lqm.services.ClassroomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 *
 * @author Le Quang Minh
 */
@Component
public class ForumPostDTOValidator implements Validator {

    @Autowired
    private ClassroomService classroomService;

    @Override
    public boolean supports(Class<?> clazz) {
        return ForumPostDTO.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        ForumPostDTO post = (ForumPostDTO) target;

        if (post.getTitle() == null || post.getTitle().trim().isEmpty()) {
            errors.rejectValue("title", "forumPost.title.notNull");
        }

        if (post.getContent() == null || post.getContent().trim().isEmpty()) {
            errors.rejectValue("content", "forumPost.content.notNull");
        }

    }
}
