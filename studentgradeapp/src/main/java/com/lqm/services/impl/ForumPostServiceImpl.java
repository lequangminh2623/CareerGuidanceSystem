package com.lqm.services.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.lqm.models.ForumPost;
import com.lqm.repositories.ClassroomRepository;
import com.lqm.repositories.ForumPostRepository;
import com.lqm.services.ForumPostService;
import com.lqm.specifications.ForumPostSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;


@Service
@Transactional
public class ForumPostServiceImpl implements ForumPostService {

    @Autowired
    private ForumPostRepository forumPostRepository;

    @Autowired
    private Cloudinary cloudinary;

    @Autowired
    private ClassroomRepository classroomRepository;

    @Override
    public Page<ForumPost> getForumPosts(Map<String, String> params, Pageable pageable) {
        Specification<ForumPost> spec = ForumPostSpecification.filterByParams(params);
        return forumPostRepository.findAll(spec, pageable);
    }

    @Override
    public Optional<ForumPost> getForumPostById(int id) {
        return forumPostRepository.findById(id);
    }

    @Override
    public ForumPost saveForumPost(ForumPost forumPost) {
        ForumPost updatedForumPost = forumPost;
        if(forumPost.getId() != null)
            updatedForumPost = getForumPostById(forumPost.getId()).orElse(forumPost);

        if(forumPost.getContent() != null) updatedForumPost.setContent(forumPost.getContent());
        if(forumPost.getFile() != null) updatedForumPost.setFile(forumPost.getFile());
        if(forumPost.getClassroom() != null) updatedForumPost.setClassroom(forumPost.getClassroom());
        if(forumPost.getUser() != null) updatedForumPost.setUser(forumPost.getUser());
        if(forumPost.getTitle() != null) updatedForumPost.setTitle(forumPost.getTitle());

        if (forumPost.getId() == null)
            updatedForumPost.setCreatedDate(new Date());
        else
            updatedForumPost.setUpdatedDate(new Date());

        if (forumPost.getFile() != null && !forumPost.getFile().isEmpty()) {
            try {
                Map<?, ?> res = cloudinary.uploader().upload(
                        forumPost.getFile().getBytes(),
                        ObjectUtils.asMap("resource_type", "auto", "folder", "GradeManagement")
                );
                updatedForumPost.setImage(res.get("secure_url").toString());
            } catch (IOException ex) {
                Logger.getLogger(ForumPostServiceImpl.class.getName())
                        .log(Level.SEVERE, null, ex);
            }
        }
        return forumPostRepository.save(updatedForumPost);
    }

    @Override
    public void deleteForumPostById(int id) {
        forumPostRepository.deleteById(id);
    }

    @Override
    public boolean checkForumPostPermission(int userId, int classroomId) {
        return classroomRepository.existsByTeacherOrStudent(userId, classroomId);
    }

    @Override
    public boolean checkOwnerForumPostPermission(int userId, int forumPostId) {
        return forumPostRepository.findById(forumPostId)
                .filter(post -> post.getUser().getId() == userId)
                .isPresent();
    }

    @Override
    public boolean isPostStillEditable(int forumPostId) {
        Optional<ForumPost> opt = forumPostRepository.findById(forumPostId);
        if (opt.isEmpty()) {
            return true;
        }
        long minutes = (new Date().getTime() - opt.get().getCreatedDate().getTime()) / 60000;
        return minutes < 30;
    }

}
