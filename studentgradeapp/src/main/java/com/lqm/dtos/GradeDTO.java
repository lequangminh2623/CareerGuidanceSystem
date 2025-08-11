/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.lqm.dtos;

import com.lqm.validators.ValidExtraGrades;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 *
 * @author Le Quang Minh
 */
@Setter
@Getter
public class GradeDTO {

    private Integer studentId;
    private String studentCode;
    private String fullName;
    
    @DecimalMin(value = "0.0")
    @DecimalMax(value = "10.0")
    private Double midtermGrade;
    
    @DecimalMin(value = "0.0")
    @DecimalMax(value = "10.0")
    private Double finalGrade;
    @ValidExtraGrades
    private List<Double> extraGrades;
    
    public GradeDTO() {
        
    }

    public GradeDTO(Integer studentId, Double midtermGrade, Double finalGrade, List<Double> extraGrades) {
        this.studentId = studentId;
        this.midtermGrade = midtermGrade;
        this.finalGrade = finalGrade;
        this.extraGrades = extraGrades;
    }
    
    public GradeDTO(Integer studentId, String studentCode, String fullName, Double midtermGrade, Double finalGrade, List<Double> extraGrades) {
        this.studentId = studentId;
        this.studentCode = studentCode;
        this.fullName = fullName;
        this.midtermGrade = midtermGrade;
        this.finalGrade = finalGrade;
        this.extraGrades = extraGrades;
    }

}
