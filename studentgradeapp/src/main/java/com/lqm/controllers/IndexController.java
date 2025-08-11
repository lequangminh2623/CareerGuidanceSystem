/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.lqm.controllers;

import com.lqm.models.GradeDetail;
import com.lqm.dtos.SemesterAnalysisResult;
import com.lqm.services.GradeDetailService;
import com.lqm.services.SemesterService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;

/**
 *
 * @author Le Quang Minh
 */
@Controller
@ControllerAdvice
public class IndexController {

    @Autowired
    private GradeDetailService gradeDetailService;
    
    @Autowired
    private SemesterService semesterService;

    @ModelAttribute
    public void requestURI(HttpServletRequest request, Model model) {
        model.addAttribute("requestURI", request.getRequestURI());
    }

    @ModelAttribute
    public void contextPath(HttpServletRequest request, Model model) {
        model.addAttribute("contextPath", request.getContextPath());
    }

    @GetMapping("/")
    public String index(@RequestParam(name = "semesterId", required = false, defaultValue = "1") int semesterId, Model model) {
        List<GradeDetail> gradeDetails = gradeDetailService.getGradeDetailsBySemester(semesterId);
        SemesterAnalysisResult result = gradeDetailService.analyzeSemester(gradeDetails);
        model.addAttribute("result", result);
        model.addAttribute("semesters", semesterService.getSemesters(null));
        model.addAttribute("semesterId", semesterId);
        return "index";
    }

}
