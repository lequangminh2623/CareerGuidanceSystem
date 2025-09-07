package com.lqm.services.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.lqm.models.ForumReply;
import com.lqm.repositories.ForumReplyRepository;
import com.lqm.services.ForumReplyService;
import java.io.IOException;
import java.util.Date;

import com.lqm.specifications.ReplySpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class ForumReplyServiceImpl implements ForumReplyService {

    @Autowired
    private ForumReplyRepository replyRepo;

    @Autowired
    private Cloudinary cloudinary;

    @Override
    public Page<ForumReply> getTopLevelReplies(int forumPostId, String keyword, Pageable pageable) {
        // Pass null for keyword to ignore filter
        String kw = (keyword == null || keyword.isBlank()) ? null : keyword;
        return replyRepo.findTopLevelReplies(forumPostId, kw, pageable);
    }

    @Override
    public Page<ForumReply> getChildReplies(int forumPostId, int parentId, Pageable pageable) {
        return replyRepo.findChildReplies(forumPostId, parentId, pageable);
    }

    @Override
    public Page<ForumReply> getAllReplies(Map<String, String> params, Pageable pageable) {
        return replyRepo.findAll(
                ReplySpecification.filterByParams(params),
                pageable
        );
    }

    @Override
    public ForumReply getReplyById(int id) {
        return replyRepo.findById(id)
                .orElse(null);
    }

    @Override
    public ForumReply saveReply(ForumReply reply) {
        ForumReply updatedReply = reply;

        if (reply.getId() == null) {
            updatedReply.setCreatedDate(new Date());
        } else {
            updatedReply = getReplyById(reply.getId());
            updatedReply.setUpdatedDate(new Date());
        }
        if(reply.getContent() != null) updatedReply.setContent(reply.getContent());
        if(reply.getUser() != null) updatedReply.setUser(reply.getUser());
        if(reply.getForumPost() != null) updatedReply.setForumPost(reply.getForumPost());
        if(reply.getParent() == null);
        else if(reply.getParent().getId() != null) updatedReply.setParent(reply.getParent());
        else updatedReply.setParent(null);

        // Handle file upload
        if (reply.getFile() != null && !reply.getFile().isEmpty()) {
            try {
                Map uploadResult = cloudinary.uploader().upload(
                        reply.getFile().getBytes(),
                        ObjectUtils.asMap("resource_type", "auto", "folder", "GradeManagement")
                );
                updatedReply.setImage(uploadResult.get("secure_url").toString());
            } catch (IOException ex) {
                Logger.getLogger(ForumReplyServiceImpl.class.getName())
                        .log(Level.SEVERE, "Cloudinary upload failed", ex);
            }
        }

        return replyRepo.save(updatedReply);
    }


    @Override
    public void deleteReply(int id) {
        replyRepo.deleteById(id);
    }

    @Override
    public boolean isReplyStillEditable(int replyId) {
        ForumReply reply = getReplyById(replyId);
        long minutes = (new Date().getTime() - reply.getCreatedDate().getTime()) / (60 * 1000);
        return minutes < 30;
    }

    @Override
    public boolean checkOwnerPermission(int userId, int replyId) {
        ForumReply reply = getReplyById(replyId);
        return !reply.getUser().getId().equals(userId);
    }
}