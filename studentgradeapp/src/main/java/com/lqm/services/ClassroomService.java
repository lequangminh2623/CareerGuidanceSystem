/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.lqm.services;

import com.lqm.models.Classroom;
import com.lqm.models.Student;
import com.lqm.models.User;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Le Quang Minh
 */
public interface ClassroomService {

    Page<Classroom> getClassrooms(Map<String, String> params, Pageable pageable);

    Classroom saveClassroom(Classroom classroom);

    Classroom getClassroomById(Integer id);

    void deleteClassroom(Integer id);

    Classroom getClassroomWithStudents(Integer id);

    int countClassroom(Map<String, String> params);

    void removeStudentFromClassroom(int classroomId, int studentId);

    boolean existsDuplicateClassroom(String name, Integer semesterId, Integer courseId, Integer excludeId);

    boolean existsStudentInOtherClassroom(int studentId, int semesterId, int courseId, Integer excludeClassroomId);

    boolean existUserInClassroom(int userId, int classRoomId);

    Page<Classroom> getClassroomsByUser(User user, Map<String, String> params, Pageable pageable);

    int countClassroomsByUser(User user, Map<String, String> params);

    Classroom getClassroomByForumPostId(int id);

    boolean lockClassroomGrades(Integer classroomId);

    boolean checkLecturerPermission(Integer classroomId);

    boolean isLockedClassroom(Integer classroomId);

    void exportGradesToCsv(Integer classroomId, HttpServletResponse response) throws IOException;

    void exportGradesToPdf(Integer classroomId, HttpServletResponse response) throws IOException;

    List<Student> getStudentsInClassroom(Integer classroomId, Map<String, String> params);
}
