/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.lqm.dtos;

import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author Le Quang Minh
 */
@Setter
@Getter
public class GradeClusterResultDTO {
    private Integer studentId;
    private String studentCode;
    private String fullName;
    private int cluster;
    private String courseName;


}
