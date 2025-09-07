/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.lqm.dtos;

import com.lqm.models.Classroom;
import com.lqm.models.Course;
import com.lqm.models.Semester;
import com.lqm.models.User;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author Le Quang Minh
 */
@Setter
@Getter
public class ClassroomDTO {

    private Integer id;

    private String name;

    private String gradeStatus;

    private Course course;

    private Semester semester;

    private User teacher;

    public ClassroomDTO() {
    }

    public ClassroomDTO(Classroom classroom) {
        this.id = classroom.getId();
        this.name = classroom.getName();
        this.gradeStatus = classroom.getGradeStatus();
        this.course = classroom.getCourse();
        this.semester = classroom.getSemester();
        this.teacher = classroom.getTeacher();
    }

}
