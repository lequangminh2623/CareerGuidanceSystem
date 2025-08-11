/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.lqm.dtos;

import com.lqm.models.GradeDetail;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author Le Quang Minh
 */
@Setter
@Getter
public class GradeDetailDTO {

    private String classroomName;
    private GradeDetail gradeDetail;

    public GradeDetailDTO() {
    }

    public GradeDetailDTO(GradeDetail gradeDetail, String classroomName) {
        this.gradeDetail = gradeDetail;
        this.classroomName = classroomName;
    }

}
