package com.lqm.services;

import com.lqm.models.ForumReply;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ForumReplyService {

    Page<ForumReply> getTopLevelReplies(int forumPostId, String keyword, Pageable pageable);

    Page<ForumReply> getChildReplies(int forumPostId, int parentId, Pageable pageable);

    Page<ForumReply> getAllReplies(String keyword, Integer userId, Pageable pageable);

    ForumReply getReplyById(int id);

    ForumReply saveReply(ForumReply reply);

    void deleteReply(int id);

    boolean isReplyStillEditable(int replyId);

    boolean checkOwnerPermission(int userId, int replyId);
}