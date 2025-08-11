/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.lqm.controllers;

import com.lqm.models.User;
import com.lqm.dtos.GradeDetailDTO;
import com.lqm.services.GradeDetailService;
import com.lqm.services.UserService;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author Le Quang Minh
 */
@RestController
@RequestMapping("/api/secure/grades")
@CrossOrigin
public class ApiGradeController {

    @Autowired
    private GradeDetailService gradeDetailService;

    @Autowired
    private UserService userService;

    @GetMapping("/student")
    public ResponseEntity<?> getStudentGrades(Authentication authentication, @RequestParam Map<String, String> params) {
        User currentUser = this.userService.getCurrentUser();

        if (currentUser.getStudent() == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .contentType(MediaType.parseMediaType("text/plain; charset=UTF-8"))
                    .body("Bạn không có quyền truy cập");
        }

        List<GradeDetailDTO> gradeSheet = gradeDetailService.getGradesByStudent(currentUser.getStudent().getId(), params);
        return ResponseEntity.ok(gradeSheet);

    }
}
