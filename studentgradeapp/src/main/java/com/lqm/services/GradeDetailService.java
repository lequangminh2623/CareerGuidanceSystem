/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.lqm.services;

import com.lqm.dtos.*;
import com.lqm.models.Classroom;
import com.lqm.models.GradeDetail;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Nullable;

/**
 *
 * @author Le Quang Minh
 */
public interface GradeDetailService {

    List<GradeDetail> getGradeDetail(Map<String, Integer> params);

    void deleteGradeDetail(Integer id);

    void saveGradeDetail(GradeDetail gd);

    void saveGradesForStudent(Integer studentId, Integer classroomId, Double midtermGrade, Double finalGrade, List<Double> extraGrades);

    boolean existsByStudentAndCourseAndSemester(Integer studentId, Integer courseId, Integer semesterId, Integer excludeId);

    boolean existsByGradeDetailIdAndGradeIndex(Integer gradeDetailId, Integer gradeIndex, Integer currentExtraGradeId);

    TranscriptDTO getTranscriptForClassroom(Integer classroomId, Map<String, String> params);

    void updateGradesForClassroom(Integer classroomId, List<GradeDTO> gradeRequests);

    void uploadGradesFromCsv(Integer classroomId, MultipartFile file) throws IOException;

    List<GradeDTO> getGradesByClassroom(Integer classroomId);

    List<GradeDetailDTO> getGradesByStudent(Integer userId, Map<String, String> params);
    
    List<GradeDetail> getGradeDetailsBySemester(Integer semesterId);

    SemesterAnalysisResult analyzeSemester(List<GradeDetail> gradeDetails);
    
    List<GradeDetail> getGradeDetailsByTeacherAndSemester(Integer teacherId, Integer semesterId);
    
    void initGradeDetailsForClassroom(Classroom classroom);

    OrientationDTO getSubjectAveragesForStudent(Integer studentId);
}
