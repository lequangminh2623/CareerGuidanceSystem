/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.lqm.dtos;

import java.util.List;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author Le Quang Minh
 */
@Setter
@Getter
public class TranscriptDTO {
    
    private String classroomName;
    private String academicTerm;
    private String courseName;
    private String lecturerName;
    private String gradeStatus;

    @Valid
    private List<GradeDTO> grades;

    public List<GradeDTO> getStudents() {
        return getGrades();
    }
    public void setStudents(List<GradeDTO> grades) {
        this.setGrades(grades);
    }

}
