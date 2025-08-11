/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.lqm.validators;

import com.lqm.models.ForumPost;
import com.lqm.models.ForumReply;
import com.lqm.services.ClassroomService;
import com.lqm.services.ForumPostService;
import com.lqm.services.ForumReplyService;
import java.util.Objects;
import java.util.Optional;
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
public class ForumReplyValidator implements Validator {

    @Autowired
    private ClassroomService classroomService;

    @Autowired
    private ForumPostService forumPostService;

    @Autowired
    private ForumReplyService forumReplyService;

    @Override
    public boolean supports(Class<?> clazz) {
        return ForumReply.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(@NotNull Object target, @NotNull Errors errors) {
        ForumReply reply = (ForumReply) target;

        // 1. Kiểm tra nội dung
        if (reply.getContent() == null || reply.getContent().trim().isEmpty()) {
            errors.rejectValue("content", "forumReply.content.notNull");
        }

        // 2. Kiểm tra user
        if (reply.getUser() == null || reply.getUser().getId() == null) {
            errors.rejectValue("user", "forumReply.user.notNull");
        }

        // 3. Kiểm tra forumPost
        if (reply.getForumPost() == null || reply.getForumPost().getId() == null) {
            errors.rejectValue("forumPost", "forumReply.forumPost.notNull");
        }

        // 4. Nếu không có lỗi cơ bản mới kiểm tra sâu hơn
        if (!errors.hasFieldErrors()) {
            Optional<ForumPost> postOpt = forumPostService.getForumPostById(reply.getForumPost().getId());

            if (postOpt.isPresent()) {
                ForumPost post = postOpt.get();

                // 4.1 Kiểm tra người dùng có thuộc lớp không
                boolean existUserInClassroom = classroomService.existUserInClassroom(
                        reply.getUser().getId(),
                        post.getClassroom().getId()
                );

                if (!existUserInClassroom) {
                    errors.rejectValue("user", "forumReply.notInClassroom");
                    errors.rejectValue("forumPost", "forumReply.notInClassroom");
                }

                // 4.2 Kiểm tra reply cha (nếu có) có đúng là của post này không
                if (reply.getParent() != null && reply.getParent().getId() != null) {
                    ForumReply parentReply = forumReplyService.getReplyById(reply.getParent().getId());

                    if (parentReply == null || !Objects.equals(parentReply.getForumPost().getId(), post.getId())) {
                        errors.rejectValue("forumPost", "forumReply.notInPost");
                        errors.rejectValue("parent", "forumReply.notInPost");
                    }
                }

            } else {
                errors.rejectValue("forumPost", "forumReply.forumPost.invalid");
            }
        }
    }

}
