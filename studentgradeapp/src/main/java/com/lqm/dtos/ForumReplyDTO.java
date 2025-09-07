/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.lqm.dtos;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.lqm.models.ForumPost;
import com.lqm.models.ForumReply;
import java.util.Date;

import com.lqm.models.User;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

/**
 *
 * @author Le Quang Minh
 */
@Setter
@Getter
public class ForumReplyDTO {

    private int id;

    private String content;

    private String image;

    private Date createdDate;

    private Date updatedDate;

    private User user;

    private Integer parentId;

    @JsonIgnore
    private MultipartFile file;

    public ForumReplyDTO() {
    }

    public ForumReplyDTO(ForumReply reply) {
        this.id = reply.getId();
        this.content = reply.getContent();
        this.image = reply.getImage();
        this.createdDate = reply.getCreatedDate();
        this.updatedDate = reply.getUpdatedDate();
        this.user = reply.getUser();
        this.parentId = reply.getParent() != null ? reply.getParent().getId() : null;
    }

}
