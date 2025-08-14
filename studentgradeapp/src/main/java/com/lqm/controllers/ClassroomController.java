/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.lqm.controllers;

import com.lqm.models.*;
import com.lqm.dtos.GradeDTO;
import com.lqm.dtos.TranscriptDTO;
import com.lqm.services.StudentService;
import com.lqm.services.ClassroomService;
import com.lqm.services.CourseService;
import com.lqm.services.GradeDetailService;
import com.lqm.services.SemesterService;
import com.lqm.services.UserService;
import com.lqm.utils.PageSize;
import com.lqm.validators.WebAppValidator;
import jakarta.persistence.PersistenceException;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/classrooms")
public class ClassroomController {

    @Autowired
    private ClassroomService classroomService;

    @Autowired
    private StudentService studentService;

    @Autowired
    private CourseService courseService;

    @Autowired
    private UserService userService;

    @Autowired
    private GradeDetailService gradeDetailService;

    @Autowired
    private SemesterService semesterService;

    @Autowired
    @Qualifier("webAppValidator")
    private WebAppValidator webAppValidator;

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.setValidator(webAppValidator);
    }

    @ModelAttribute
    public void commonAttributes(Model model) {
        model.addAttribute("students", studentService.getStudents(null, null, Pageable.unpaged()).getContent());
        model.addAttribute("courses", courseService.getCourses(null, Pageable.unpaged()).getContent());
        model.addAttribute("semesters", semesterService.getSemesters(null));
        List<User> lecturers = userService.getUsers(Map.of("role", "ROLE_LECTURER"), Pageable.unpaged()).getContent();
        model.addAttribute("lecturers", lecturers);
    }

    @GetMapping("")
    public String listClassrooms(Model model, @RequestParam Map<String, String> params) {
        int pageNumber = 1;

        // Lấy page từ params và validate
        String pageParam = params.get("page");
        if (pageParam != null && !pageParam.isEmpty()) {
            try {
                pageNumber = Integer.parseInt(pageParam);
                if (pageNumber < 1) pageNumber = 1;
            } catch (NumberFormatException ignored) {}
        }

        Pageable pageable = PageRequest.of(
                pageNumber - 1,
                PageSize.CLASSROOM_PAGE_SIZE
        );

        Page<Classroom> classroomPage = classroomService.getClassrooms(params, pageable);

        model.addAttribute("classrooms", classroomPage.getContent());
        model.addAttribute("currentPage", pageNumber);
        model.addAttribute("totalPages", classroomPage.getTotalPages());

        return "/classroom/classroom-list";
    }


    @GetMapping("/add")
    public String addClassroom(Model model) {
        model.addAttribute("transcript", new TranscriptDTO());
        model.addAttribute("classroom", new Classroom());
        return "/classroom/classroom-form";
    }

    private TranscriptDTO buildTranscriptForClassroom(Classroom classroom) {
        List<GradeDTO> gradeDTOList = new ArrayList<>();

        for (Student s : classroom.getStudentSet()) {
            Map<String, Integer> ref = Map.of(
                    "classroomId", classroom.getId(),
                    "studentId", s.getId()
            );

            List<GradeDetail> gradeDetails = gradeDetailService.getGradeDetail(ref);
            GradeDetail gd = !gradeDetails.isEmpty() ? gradeDetails.getFirst() : new GradeDetail();

            if (gd.getExtraGradeSet() == null) {
                gd.setExtraGradeSet(new HashSet<>());
            }

            GradeDTO dto = new GradeDTO();
            dto.setStudentId(s.getId());
            dto.setStudentCode(s.getCode());
            dto.setFullName(s.getUser().getLastName() + " " + s.getUser().getFirstName());
            dto.setMidtermGrade(gd.getMidtermGrade());
            dto.setFinalGrade(gd.getFinalGrade());
            dto.setExtraGrades(
                    gd.getExtraGradeSet().stream()
                            .sorted(Comparator.comparingInt(ExtraGrade::getGradeIndex))
                            .map(ExtraGrade::getGrade)
                            .collect(Collectors.toList())
            );

            gradeDTOList.add(dto);
        }

        TranscriptDTO transcript = new TranscriptDTO();
        transcript.setClassroomName(classroom.getCourse().getName() + " - " + classroom.getName());
        transcript.setCourseName(classroom.getCourse().getName());
        transcript.setAcademicTerm(classroom.getSemester().getSemesterType());
        transcript.setLecturerName(
                classroom.getLecturer().getLastName() + " " + classroom.getLecturer().getFirstName()
        );
        transcript.setGrades(gradeDTOList);

        return transcript;
    }


    @GetMapping("/{id}")
    public String updateClassroom(@PathVariable("id") Integer id, Model model) {
        Classroom classroom = classroomService.getClassroomWithStudents(id);
        TranscriptDTO transcript = buildTranscriptForClassroom(classroom);
        model.addAttribute("classroom", classroom);
        model.addAttribute("transcript", transcript);
        return "/classroom/classroom-form";

    }

    @PostMapping("")
    public String saveClassroomAndStudents(
            @ModelAttribute @Valid Classroom classroom,
            BindingResult bindingResult,
            Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("errorMessage", "Có lỗi xảy ra");

            if (classroom.getId() != null) {
                TranscriptDTO transcript = buildTranscriptForClassroom(classroom);
                model.addAttribute("transcript", transcript);
            }

            model.addAttribute("classroom", classroom);
            return "/classroom/classroom-form";
        }

        Classroom savedClassroom = classroomService.saveClassroom(classroom);
        gradeDetailService.initGradeDetailsForClassroom(savedClassroom);

        return "redirect:/classrooms";
    }

    @PostMapping("/{id}/grades")
    public String saveGrades(@PathVariable("id") Integer classroomId,
            @ModelAttribute("transcript") @Valid TranscriptDTO transcript,
            BindingResult result,
            Model model) {

        if (result.hasErrors()) {
            model.addAttribute("errorMessage", "Có lỗi xảy ra");
            Classroom classroom = classroomService.getClassroomWithStudents(classroomId);
            model.addAttribute("classroom", classroom);
            model.addAttribute("transcript", transcript);
            return "/classroom/classroom-form";
        }
         try {
            gradeDetailService.updateGradesForClassroom(classroomId, transcript.getGrades());
        } catch (IllegalArgumentException e) {
             model.addAttribute("errorMessage", "Không thể có nhiều hơn 3 cột diểm bổ sung");
            Classroom classroom = classroomService.getClassroomWithStudents(classroomId);
            model.addAttribute("classroom", classroom);
            model.addAttribute("transcript", transcript);
            return "/classroom/classroom-form";
        }


        return "redirect:/classrooms";
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteClassroom(@PathVariable("id") Integer id) {
        try {
            classroomService.deleteClassroom(id);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .contentType(MediaType.parseMediaType("text/plain; charset=UTF-8"))
                    .body("Không thể xóa lớp do lớp còn sinh viên.");
        } catch (PersistenceException | ConstraintViolationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .contentType(MediaType.parseMediaType("text/plain; charset=UTF-8"))
                    .body("Không thể xóa lớp do ràng buộc dữ liệu.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.parseMediaType("text/plain; charset=UTF-8"))
                    .body("Đã xảy ra lỗi: " + e.getMessage());
        }
    }

    @DeleteMapping("/{classId}/students/{studentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeStudentFromClass(@PathVariable("classId") Integer classId,
            @PathVariable("studentId") Integer studentId) {
        Map<String, Integer> ref = new HashMap<>();
        ref.put("classroomId", classId);
        ref.put("studentId", studentId);
        List<GradeDetail> gradeDetails = this.gradeDetailService.getGradeDetail(ref);
        if (gradeDetails != null) {
            Integer gradeDetailId = gradeDetails.getFirst().getId();
            classroomService.removeStudentFromClassroom(classId, studentId);
            this.gradeDetailService.deleteGradeDetail(gradeDetailId);
        }
    }
}