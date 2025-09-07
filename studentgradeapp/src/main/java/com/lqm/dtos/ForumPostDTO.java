/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.lqm.dtos;

import com.lqm.models.ForumPost;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

/**
 *
 * @author Le Quang Minh
 */
@Setter
@Getter
public class ForumPostDTO {

    private String title;

    private String content;

    private String image;

    private MultipartFile file;

    public ForumPostDTO() {
    }

    public ForumPostDTO(ForumPost post) {
        this.title = post.getTitle();
        this.content = post.getContent();
        this.image = post.getImage();
        this.file = post.getFile();
    }

}
