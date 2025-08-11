package com.lqm.services;

import com.lqm.models.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface CourseService {

    Page<Course> getCourses(Map<String, String> params, Pageable pageable);

    void saveCourse(Course course);

    Course getCourseById(int id);

    void deleteCourseById(int id);

    long countCourses(Map<String, String> params);

    boolean existCourseByName(String name, Integer excludeId);

    List<Course> getAllCourses();

}
