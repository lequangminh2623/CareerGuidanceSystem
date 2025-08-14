// ApiForumController.java
package com.lqm.controllers;

import com.lqm.dtos.ForumPostDTO;
import com.lqm.dtos.ForumPostDetailDTO;
import com.lqm.dtos.ForumReplyDTO;
import com.lqm.models.Classroom;
import com.lqm.models.ForumPost;
import com.lqm.models.ForumReply;
import com.lqm.models.User;
import com.lqm.services.ClassroomService;
import com.lqm.services.ForumPostService;
import com.lqm.services.ForumReplyService;
import com.lqm.services.UserService;
import com.lqm.utils.PageSize;
import com.lqm.validators.WebAppValidator;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/secure/forums")
@CrossOrigin
public class ApiForumController {

    @Autowired
    private UserService userService;

    @Autowired
    private ForumPostService forumPostService;

    @Autowired
    private ClassroomService classroomService;

    @Autowired
    private ForumReplyService forumReplyService;

    @Autowired
    private WebAppValidator webAppValidator;

    @Autowired
    private MessageSource messageSource;

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.setValidator(webAppValidator);
    }

    @GetMapping
    public ResponseEntity<Page<ForumPostDetailDTO>> listForumPosts(
            @RequestParam Map<String, String> params) {

        int pageNumber = 1;

        // Lấy và validate tham số page
        String pageParam = params.get("page");
        if (pageParam != null && !pageParam.isEmpty()) {
            try {
                pageNumber = Integer.parseInt(pageParam);
                if (pageNumber < 1) pageNumber = 1;
            } catch (NumberFormatException ignored) {}
        }

        Pageable pageable = PageRequest.of(
                pageNumber - 1,
                PageSize.FORUM_POST_PAGE_SIZE,
                Sort.by("id").descending()
        );

        Page<ForumPost> posts = forumPostService.getForumPosts(params, pageable);
        Page<ForumPostDetailDTO> dtoPage = posts.map(ForumPostDetailDTO::new);

        return ResponseEntity.ok(dtoPage);
    }


    @GetMapping(path = "/{postId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getForumPostDetail(
            @PathVariable int postId,
            @RequestParam Map<String, String> params,
            @PageableDefault(size = 10) Pageable pageable) {
        User user = userService.getCurrentUser();
        Classroom classroom = classroomService.getClassroomByForumPostId(postId);
        if (classroom == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Không tìm thấy bài đăng"));
        }
        if (!forumPostService.checkForumPostPermission(user.getId(), classroom.getId())) {
            return ResponseEntity.status(403).body(Map.of("error", "Bạn không có quyền truy cập"));
        }
        Optional<ForumPost> optPost = forumPostService.getForumPostById(postId);
        if (optPost.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Không tìm thấy bài đăng"));
        }
        ForumPost post = optPost.get();
        Page<ForumReply> replies = forumReplyService.getTopLevelReplies(postId, params.get("kw"), pageable);
        ForumPostDetailDTO detail = new ForumPostDetailDTO(post, replies);
        return ResponseEntity.ok(detail);
    }

    @PatchMapping(path = "/{postId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updateForumPost(
            @PathVariable int postId,
            @Valid @RequestBody ForumPostDTO dto,
            BindingResult errors,
            Locale locale) {
        if (errors.hasErrors()) {
            var errs = errors.getFieldErrors().stream()
                    .map(e -> Map.of("field", e.getField(),
                            "message", messageSource.getMessage(e, locale)))
                    .collect(Collectors.toList());
            return ResponseEntity.badRequest().body(errs);
        }
        User user = userService.getCurrentUser();
        Classroom classroom = classroomService.getClassroomByForumPostId(postId);
        if (classroom == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Không tìm thấy bài đăng"));
        }
        if (!forumPostService.checkForumPostPermission(user.getId(), classroom.getId()) ||
                !forumPostService.checkOwnerForumPostPermission(user.getId(), postId)) {
            return ResponseEntity.status(403).body(Map.of("error", "Bạn không có quyền truy cập"));
        }
        if (!forumPostService.isPostStillEditable(postId)) {
            return ResponseEntity.status(403).body(Map.of("error", "Bạn không thể chỉnh sửa được nữa"));
        }
        ForumPost post = optPost(postId);
        post.setTitle(dto.getTitle());
        post.setContent(dto.getContent());
        post.setFile(dto.getFile());
        ForumPost updated = forumPostService.saveForumPost(post);
        return ResponseEntity.ok(new ForumPostDetailDTO(updated));
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<?> deleteForumPost(@PathVariable int postId) {
        User user = userService.getCurrentUser();
        Classroom classroom = classroomService.getClassroomByForumPostId(postId);
        if (classroom == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Không tìm thấy bài đăng"));
        }
        if (!forumPostService.checkForumPostPermission(user.getId(), classroom.getId()) ||
                !forumPostService.checkOwnerForumPostPermission(user.getId(), postId)) {
            return ResponseEntity.status(403).body(Map.of("error", "Bạn không có quyền truy cập"));
        }
        if (!forumPostService.isPostStillEditable(postId)) {
            return ResponseEntity.status(403).body(Map.of("error", "Bạn không thể chỉnh sửa được nữa"));
        }
        forumPostService.deleteForumPostById(postId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping(path = "/{postId}/replies/{replyId}/child-replies")
    public ResponseEntity<Page<ForumReplyDTO>> getChildReplies(
            @PathVariable int postId,
            @PathVariable int replyId,
            @RequestParam Map<String, String> params,
            @PageableDefault(size = 10) Pageable pageable) {
        User user = userService.getCurrentUser();
        Classroom classroom = classroomService.getClassroomByForumPostId(postId);
        if (classroom == null) {
            return ResponseEntity.badRequest().body(Page.empty());
        }
        if (!forumPostService.checkForumPostPermission(user.getId(), classroom.getId())) {
            return ResponseEntity.status(403).body(Page.empty());
        }
        Page<ForumReply> replies = forumReplyService.getChildReplies(postId, replyId, pageable);
        Page<ForumReplyDTO> dtoPage = replies.map(ForumReplyDTO::new);
        return ResponseEntity.ok(dtoPage);
    }

    @PostMapping(path = "/{postId}/replies", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createReply(
            @PathVariable int postId,
            @Valid @RequestBody ForumReplyDTO dto,
            BindingResult errors,
            Locale locale) {
        if (errors.hasErrors()) {
            var errs = errors.getFieldErrors().stream()
                    .map(e -> Map.of("field", e.getField(),
                            "message", messageSource.getMessage(e, locale)))
                    .collect(Collectors.toList());
            return ResponseEntity.badRequest().body(errs);
        }
        User user = userService.getCurrentUser();
        Classroom classroom = classroomService.getClassroomByForumPostId(postId);
        if (classroom == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Không tìm thấy bài đăng"));
        }
        if (!forumPostService.checkForumPostPermission(user.getId(), classroom.getId())) {
            return ResponseEntity.status(403).body(Map.of("error", "Bạn không có quyền truy cập"));
        }
        ForumReply reply = forumReplyService.saveReply(dto.toEntity(postId, user));
        return ResponseEntity.status(201).body(new ForumReplyDTO(reply));
    }

    @PatchMapping(path = "/{postId}/replies/{replyId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updateReply(
            @PathVariable int postId,
            @PathVariable int replyId,
            @Valid @RequestBody ForumReplyDTO dto,
            BindingResult errors,
            Locale locale) {
        if (errors.hasErrors()) {
            var errs = errors.getFieldErrors().stream()
                    .map(e -> Map.of("field", e.getField(),
                            "message", messageSource.getMessage(e, locale)))
                    .collect(Collectors.toList());
            return ResponseEntity.badRequest().body(errs);
        }
        User user = userService.getCurrentUser();
        Classroom classroom = classroomService.getClassroomByForumPostId(postId);
        if (classroom == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Không tìm thấy bài đăng"));
        }
        if (!forumPostService.checkForumPostPermission(user.getId(), classroom.getId()) ||
                forumReplyService.checkOwnerPermission(user.getId(), replyId)) {
            return ResponseEntity.status(403).body(Map.of("error", "Bạn không có quyền truy cập"));
        }
        if (forumReplyService.isReplyStillEditable(replyId)) {
            return ResponseEntity.status(403).body(Map.of("error", "Bạn không thể chỉnh sửa được nữa"));
        }
        ForumReply reply = forumReplyService.getReplyById(replyId);
        reply.setContent(dto.getContent());
        reply.setFile(dto.getFile());
        ForumReply updated = forumReplyService.saveReply(reply);
        return ResponseEntity.ok(new ForumReplyDTO(updated));
    }

    @DeleteMapping(path = "/{postId}/replies/{replyId}")
    public ResponseEntity<?> deleteReply(
            @PathVariable int postId,
            @PathVariable int replyId) {
        User user = userService.getCurrentUser();
        Classroom classroom = classroomService.getClassroomByForumPostId(postId);
        if (classroom == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Không tìm thấy bài đăng"));
        }
        if (!forumPostService.checkForumPostPermission(user.getId(), classroom.getId()) ||
                forumReplyService.checkOwnerPermission(user.getId(), replyId)) {
            return ResponseEntity.status(403).body(Map.of("error", "Bạn không có quyền truy cập"));
        }
        if (forumReplyService.isReplyStillEditable(replyId)) {
            return ResponseEntity.status(403).body(Map.of("error", "Bạn không thể chỉnh sửa được nữa"));
        }
        forumReplyService.deleteReply(replyId);
        return ResponseEntity.noContent().build();
    }

    private ForumPost optPost(int postId) {
        return forumPostService.getForumPostById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
    }
}
