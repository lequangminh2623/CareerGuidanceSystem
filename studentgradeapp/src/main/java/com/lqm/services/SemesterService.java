/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.lqm.services;

import com.lqm.models.Semester;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Le Quang Minh
 */
public interface SemesterService {

    List<Semester> getSemestersByAcademicYearId(int id, Map<String, String> params);

    List<Semester> getSemesters(Map<String, String> params);

    Semester saveSemester(Semester semester);

    Semester getSemesterById(int id);

    void deleteSemesterById(int id);

    boolean existSemesterByTypeAndAcademicYearId(String type, Integer semesterId, Integer yearId);
}
