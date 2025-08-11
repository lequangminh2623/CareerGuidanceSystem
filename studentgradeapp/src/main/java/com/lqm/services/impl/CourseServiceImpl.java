package com.lqm.services.impl;

import com.lqm.models.Course;
import com.lqm.repositories.CourseRepository;
import com.lqm.services.CourseService;
import com.lqm.specifications.CourseSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class CourseServiceImpl implements CourseService {

    @Autowired
    private CourseRepository courseRepository;

    @Override
    public Page<Course> getCourses(Map<String, String> params, Pageable pageable) {
        // dùng Specification để lọc theo params, tự paginate + sort
        return courseRepository.findAll(
                CourseSpecification.filterByParams(params),
                pageable
        );
    }

    @Override
    public void saveCourse(Course course) {
        courseRepository.save(course);
    }

    @Override
    public Course getCourseById(int id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Course not found: " + id));
    }

    @Override
    public void deleteCourseById(int id) {
        courseRepository.deleteById(id);
    }

    @Override
    public long countCourses(Map<String, String> params) {
        // JpaSpecificationExecutor cung cấp count(Specification)
        return courseRepository.count(
                CourseSpecification.filterByParams(params)
        );
    }

    @Override
    public boolean existCourseByName(String name, Integer excludeId) {
        // nếu excludeId null, truyền -1 để không loại trừ
        Integer exId = (excludeId != null ? excludeId : -1);
        return courseRepository.existsByNameAndIdNot(name, exId);
    }

    @Override
    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

}
