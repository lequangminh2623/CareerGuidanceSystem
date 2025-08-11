package com.lqm.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.lqm.models.ForumPost;
import com.lqm.models.ForumReply;
import com.lqm.models.User;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;

import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
/**
 *
 * @author Le Quang Minh
 */
@Setter
@Getter
public class ForumPostDetailDTO {

    private int id;

    private String title;

    private String content;

    private String image;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private Date createdDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private Date updatedDate;

    private User user;

    private Set<ForumReply> forumReplies;

    public ForumPostDetailDTO(ForumPost forumPost) {
        this.id = forumPost.getId();
        this.title = forumPost.getTitle();
        this.content = forumPost.getContent();
        this.image = forumPost.getImage();
        this.createdDate = forumPost.getCreatedDate();
        this.updatedDate = forumPost.getUpdatedDate();
        this.user = forumPost.getUser();
        this.forumReplies = forumPost.getForumReplySet();
    }

    public ForumPostDetailDTO() {
    }

    public ForumPostDetailDTO(ForumPost post, Page<ForumReply> repliesPage) {
        this.id = post.getId();
        this.title = post.getTitle();
        this.content = post.getContent();
        this.image = post.getImage();
        this.createdDate = post.getCreatedDate();
        this.updatedDate = post.getUpdatedDate();
        this.user = post.getUser();
        this.forumReplies = Set.copyOf(repliesPage.getContent()); // hoặc new HashSet<>(...)
    }

}
