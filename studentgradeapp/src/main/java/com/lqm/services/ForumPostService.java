package com.lqm.services;

import com.lqm.models.ForumPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ForumPostService {

    Page<ForumPost> getForumPosts(Map<String, String> params, Pageable pageable);

    Optional<ForumPost> getForumPostById(int id);

    ForumPost saveForumPost(ForumPost forumPost);

    void deleteForumPostById(int id);

    boolean checkForumPostPermission(int userId, int classroomId);

    boolean checkOwnerForumPostPermission(int userId, int forumPostId);

    boolean isPostStillEditable(int forumPostId);

}