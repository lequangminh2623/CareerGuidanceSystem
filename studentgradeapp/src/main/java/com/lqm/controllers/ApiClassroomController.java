package com.lqm.controllers;

import com.lqm.models.Classroom;
import com.lqm.models.ForumPost;
import com.lqm.models.User;
import com.lqm.dtos.ClassroomDTO;
import com.lqm.dtos.ForumPostDTO;
import com.lqm.dtos.GradeDTO;
import com.lqm.dtos.TranscriptDTO;
import com.lqm.services.ClassroomService;
import com.lqm.services.ForumPostService;
import com.lqm.services.GradeDetailService;
import com.lqm.services.UserService;
import com.lqm.utils.PageSize;
import com.lqm.validators.WebAppValidator;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/secure/classrooms")
@CrossOrigin
public class ApiClassroomController {

    @Autowired
    private GradeDetailService gradeDetailService;

    @Autowired
    private ClassroomService classroomService;

    @Autowired
    private UserService userService;

    @Autowired
    private ForumPostService forumPostService;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    @Qualifier("webAppValidator")
    private WebAppValidator webAppValidator;

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.setValidator(webAppValidator);
    }

    private boolean isUnauthorized(Integer classroomId) {
        return !classroomService.checkLecturerPermission(classroomId);
    }

    private ResponseEntity<String> unauthorizedResponse(String message) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .contentType(MediaType.parseMediaType("text/plain; charset=UTF-8"))
                .body(message);
    }

    @GetMapping("/{classroomId}/grades")
    public ResponseEntity<?> getGradeSheet(@PathVariable Integer classroomId, @RequestParam Map<String, String> params) {
        if (isUnauthorized(classroomId)) return unauthorizedResponse("Bạn không phải giảng viên phụ trách lớp này.");

        try {
            TranscriptDTO sheet = gradeDetailService.getTranscriptForClassroom(classroomId, params);
            return ResponseEntity.ok(sheet);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.badRequest().contentType(MediaType.TEXT_PLAIN).body("Lỗi: " + e.getMessage());
        }
    }

    @PatchMapping("/{classroomId}/lock")
    public ResponseEntity<?> lockTranscript(@PathVariable Integer classroomId) {
        if (isUnauthorized(classroomId)) return unauthorizedResponse("Bạn không phải giảng viên phụ trách lớp này.");
        if (classroomService.isLockedClassroom(classroomId)) return unauthorizedResponse("Bảng điểm đã được khóa trước đó.");
        if (!classroomService.lockClassroomGrades(classroomId)) return unauthorizedResponse("Chưa nhập đủ điểm cho tất cả sinh viên.");

        return ResponseEntity.ok("Điểm của lớp " + classroomId + " khóa thành công!");
    }

    @PostMapping("/{classroomId}/grades")
    public ResponseEntity<String> saveGrades(@PathVariable Integer classroomId,
                                             @RequestBody @Valid List<GradeDTO> gradeRequests,
                                             BindingResult result) {
        if (result.hasErrors()) return ResponseEntity.badRequest().body("Lỗi: Điểm phải nằm trong khoảng từ 0 đến 10");
        if (isUnauthorized(classroomId)) return unauthorizedResponse("Bạn không phải giảng viên phụ trách lớp này.");
        if (classroomService.isLockedClassroom(classroomId)) return unauthorizedResponse("Bảng điểm đã khóa.");

        try {
            gradeDetailService.updateGradesForClassroom(classroomId, gradeRequests);
            return ResponseEntity.ok("Lưu điểm thành công!");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        }
    }

    @PostMapping("/{classroomId}/grades/import")
    public ResponseEntity<String> importCsv(@PathVariable Integer classroomId,
                                            @RequestParam("file") MultipartFile file) {
        if (isUnauthorized(classroomId)) return unauthorizedResponse("Bạn không phải giảng viên phụ trách lớp này.");
        if (classroomService.isLockedClassroom(classroomId)) return unauthorizedResponse("Bảng điểm đã khóa.");

        try {
            gradeDetailService.uploadGradesFromCsv(classroomId, file);
            return ResponseEntity.ok("Lưu điểm thành công!");
        } catch (IOException | IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi: " + e.getMessage());
        }
    }

    @GetMapping("/{classroomId}/grades/export/csv")
    public ResponseEntity<String> exportCsv(@PathVariable Integer classroomId, HttpServletResponse response) throws IOException {
        if (isUnauthorized(classroomId)) return unauthorizedResponse("Bạn không phải giảng viên phụ trách lớp này.");
        if (!classroomService.isLockedClassroom(classroomId)) return unauthorizedResponse("Bảng điểm chưa khóa.");

        classroomService.exportGradesToCsv(classroomId, response);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{classroomId}/grades/export/pdf")
    public ResponseEntity<String> exportPdf(@PathVariable Integer classroomId, HttpServletResponse response) throws IOException {
        if (isUnauthorized(classroomId)) return unauthorizedResponse("Bạn không phải giảng viên phụ trách lớp này.");
        if (!classroomService.isLockedClassroom(classroomId)) return unauthorizedResponse("Bảng điểm chưa khóa.");

        classroomService.exportGradesToPdf(classroomId, response);
        return ResponseEntity.ok().build();
    }

    @GetMapping("")
    public ResponseEntity<?> getClassrooms(
            @RequestParam Map<String, String> params,
            @PageableDefault(size = PageSize.CLASSROOM_PAGE_SIZE) Pageable pageable) {

        User user = userService.getCurrentUser();
        if (user == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        Page<Classroom> classroomPage = classroomService.getClassroomsByUser(user, params, pageable);

        List<ClassroomDTO> classroomsDto = classroomPage.getContent().stream()
                .map(ClassroomDTO::new)
                .collect(Collectors.toList());

        return ResponseEntity.ok(Map.of(
                "content", classroomsDto,
                "totalPages", classroomPage.getTotalPages()
        ));
    }

    @GetMapping("/{classroomId}/forums")
    public ResponseEntity<?> getForumPosts(
            @PathVariable int classroomId,
            @RequestParam Map<String, String> params,
            @PageableDefault(size = PageSize.FORUM_POST_PAGE_SIZE) Pageable pageable) {

        User user = userService.getCurrentUser();
        if (forumPostService.checkForumPostPermission(user.getId(), classroomId)) {
            return unauthorizedResponse("Bạn không có quyền truy cập");
        }

        // Đảm bảo classroom id được thêm vào params để filter
        params.put("classroom", String.valueOf(classroomId));

        Page<ForumPost> postPage = forumPostService.getForumPosts(params, pageable);

        return ResponseEntity.ok(
                Map.of(
                        "content", postPage.getContent(),
                        "totalPages", postPage.getTotalPages()
                )
        );
    }


    @PostMapping("/{classroomId}/forums")
    public ResponseEntity<?> addForumPost(@PathVariable int classroomId,
                                          @ModelAttribute @Valid ForumPostDTO dto,
                                          BindingResult result) {
        if (result.hasErrors()) {
            List<Map<String, String>> errors = result.getFieldErrors().stream()
                    .map(err -> Map.of(
                            "field", err.getField(),
                            "message", Optional.ofNullable(err.getDefaultMessage())
                                    .orElse(messageSource.getMessage(Objects.requireNonNull(err.getCode()), null, Locale.ITALY))
                    )).collect(Collectors.toList());
            return ResponseEntity.badRequest().body(errors);
        }

        User user = userService.getCurrentUser();
        if (forumPostService.checkForumPostPermission(user.getId(), classroomId)) {
            return unauthorizedResponse("Bạn không có quyền truy cập");
        }

        ForumPost post = new ForumPost();
        post.setTitle(dto.getTitle());
        post.setContent(dto.getContent());
        post.setFile(dto.getFile());
        post.setUser(user);
        post.setClassroom(classroomService.getClassroomById(classroomId));

        return ResponseEntity.status(HttpStatus.CREATED).body(forumPostService.saveForumPost(post));
    }
}
