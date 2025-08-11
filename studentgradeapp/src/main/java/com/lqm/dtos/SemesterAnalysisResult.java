/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.lqm.dtos;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

/**
 *
 * @author Le Quang Minh
 */
@Setter
@Getter
public class SemesterAnalysisResult {

    private int totalStudents;

    private int weakStudents;

    private double weakRatio;

    private List<String> criticalCourses; // môn có tỷ lệ yếu > 30%

    private Map<String, Double> courseWeakRatios; // course name -> % yếu

    private List<GradeClusterResultDTO> weakStudentList;


}
