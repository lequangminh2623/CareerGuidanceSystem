/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.lqm.controllers;

import com.lqm.models.Course;
import com.lqm.services.CourseService;
import com.lqm.utils.PageSize;
import com.lqm.validators.WebAppValidator;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class CourseController {

    @Autowired
    private CourseService courseService;

    @Autowired
    @Qualifier("webAppValidator")
    private WebAppValidator webAppValidator;

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.setValidator(webAppValidator);
    }

    @GetMapping("/courses")
    public String listCourses(Model model, @RequestParam Map<String, String> params) {
        int pageNumber = 0; // mặc định trang đầu tiên (0-based)

        // Lấy số trang từ params (người dùng nhập 1-based)
        String pageParam = params.get("page");
        if (pageParam != null && !pageParam.isEmpty()) {
            try {
                pageNumber = Integer.parseInt(pageParam) - 1;
                if (pageNumber < 0) pageNumber = 0;
            } catch (NumberFormatException ignored) {}
        }

        Pageable pageable = PageRequest.of(pageNumber, PageSize.COURSE_PAGE_SIZE);

        Page<Course> coursePage = courseService.getCourses(params, pageable);

        model.addAttribute("courses", coursePage.getContent());
        model.addAttribute("currentPage", pageNumber + 1);
        model.addAttribute("totalPages", coursePage.getTotalPages());
        model.addAttribute("kw", params.get("kw"));

        return "/course/course-list";
    }


    @GetMapping("courses/add")
    public String addCoures(Model model) {
        Course course = new Course();
        model.addAttribute("course", course);
        return "course/course-form";
    }

    @PostMapping("/courses")
    public String saveCourse(@ModelAttribute("course") @Valid Course course, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("errorMessage", "Có lỗi xảy ra");
            return "/course/course-form";
        }

        this.courseService.saveCourse(course);
        return "redirect:/courses";

    }

    @GetMapping("/courses/{id}")
    public String updateCourse(Model model, @PathVariable(value = "id") int id) {
        Course course = this.courseService.getCourseById(id);
        model.addAttribute("course", course);
        return "/course/course-form";
    }

    @DeleteMapping("/courses/{id}")
    public ResponseEntity<String> deleteCourse(@PathVariable("id") int id) {
        try {
            this.courseService.deleteCourseById(id);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .contentType(MediaType.parseMediaType("text/plain; charset=UTF-8"))
                    .body("Không thể xóa môn học này.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.parseMediaType("text/plain; charset=UTF-8"))
                    .body("Đã xảy ra lỗi: " + e.getMessage());
        }
    }

}
