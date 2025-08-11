/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.lqm.dtos;

import com.lqm.models.Student;
import com.lqm.models.User;
import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

/**
 *
 * @author Le Quang Minh
 */
@Setter
@Getter
public class UserDTO {

    @Basic(optional = false)
    @Size(min = 1, max = 255)
    private String firstName;

    @Basic(optional = false)
    @Size(min = 1, max = 255)
    @Column(name = "last_name")
    private String lastName;

    @Pattern(regexp = "^[A-Za-z0-9._%+-]+@ou\\.edu\\.vn$", message = "{user.email.invalid}")
    private String email;

    private String password;

    private String avatar;

    @Size(min = 10, max = 10, message = "{user.student.code.size}")
    private String code;

    private MultipartFile file;

    public UserDTO() {
    }

    public User toEntity() {
        User user = new User();
        user.setFirstName(this.firstName);
        user.setLastName(this.lastName);
        user.setEmail(this.email);
        user.setPassword(this.password);
        user.setAvatar(this.avatar);
        user.setFile(this.file);

        if (this.code != null && !this.code.isEmpty()) {
            Student student = new Student();
            student.setCode(this.code);
            student.setUser(user);
            user.setStudent(student);
        }

        return user;
    }


}
