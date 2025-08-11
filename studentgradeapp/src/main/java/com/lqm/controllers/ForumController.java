package com.lqm.controllers;

import com.lqm.models.ForumPost;
import com.lqm.models.ForumReply;
import com.lqm.dtos.ForumReplyDTO;
import com.lqm.repositories.UserRepository;
import com.lqm.services.ClassroomService;
import com.lqm.services.ForumPostService;
import com.lqm.services.ForumReplyService;
import com.lqm.services.UserService;
import com.lqm.utils.PageSize;
import com.lqm.validators.WebAppValidator;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * MVC controller for forum posts and replies.
 */
@Controller
@RequestMapping("/forums")
public class ForumController {

    @Autowired
    private ForumPostService forumPostService;

    @Autowired
    private ForumReplyService forumReplyService;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private ClassroomService classroomService;

    @Autowired
    @Qualifier("webAppValidator")
    private WebAppValidator webAppValidator;

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.setValidator(webAppValidator);
    }

    @ModelAttribute("commonData")
    public void populateCommon(Model model) {
        model.addAttribute("users", userRepo.findByRoleIn(List.of("ROLE_LECTURER", "ROLE_STUDENT")));
        // classrooms pagination for sidebar/filter
        Pageable classroomPageable = Pageable.ofSize(10);
        model.addAttribute("classrooms", classroomService.getClassrooms(null, classroomPageable));
    }

    @GetMapping
    public String listPosts(
            Model model,
            @RequestParam Map<String, String> params,
            @PageableDefault(size = 5, sort = "id", direction = org.springframework.data.domain.Sort.Direction.DESC)
            Pageable pageable) {
        Page<ForumPost> page = forumPostService.getForumPosts(params, pageable);
        model.addAttribute("forumPosts", page.getContent());
        model.addAttribute("page", page);
        model.addAttribute("params", params);
        return "forum/forum-post-list";
    }

    @GetMapping("/add")
    public String showCreateForm(Model model) {
        model.addAttribute("forumPost", new ForumPost());
        return "forum/forum-post-form";
    }

    @PostMapping
    public String createPost(
            @ModelAttribute("forumPost") @Valid ForumPost forumPost,
            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "forum/forum-post-form";
        }
        forumPostService.saveForumPost(forumPost);
        return "redirect:/forums";
    }

    @GetMapping("/{id}")
    public String showUpdateForm(@PathVariable int id, Model model) {
        forumPostService.getForumPostById(id)
                .ifPresent(post -> model.addAttribute("forumPost", post));
        return "forum/forum-post-form";
    }

    @PostMapping("/{id}")
    public String updatePost(
            @PathVariable int id,
            @ModelAttribute("forumPost") @Valid ForumPost forumPost,
            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "forum/forum-post-form";
        }
        forumPost.setId(id);
        forumPostService.saveForumPost(forumPost);
        return "redirect:/forums";
    }

    @PostMapping("/{id}/delete")
    public String deletePost(@PathVariable int id) {
        forumPostService.deleteForumPostById(id);
        return "redirect:/forums";
    }

    //REPLY
    @GetMapping("/forums/{id}/replies")
    public String getForumReplies(Model model,
                                  @PathVariable("id") int forumPostId,
                                  @RequestParam Map<String, String> params) {
        Optional<ForumPost> forumPost = forumPostService.getForumPostById(forumPostId);
        if (forumPost.isPresent()) {
            int page = Integer.parseInt(params.getOrDefault("page", "1")) - 1;
            String keyword = params.get("kw");

            Pageable pageable = PageRequest.of(page, PageSize.FORUM_REPLY_PAGE_SIZE);
            Page<ForumReply> repliesPage = forumReplyService.getTopLevelReplies(forumPostId, keyword, pageable);

            model.addAttribute("replies", repliesPage.getContent());
            model.addAttribute("forumPost", forumPost.get());
            model.addAttribute("kw", keyword);
            model.addAttribute("currentPage", repliesPage.getNumber() + 1);
            model.addAttribute("totalPages", repliesPage.getTotalPages());
        }

        return "/forum/forum-reply-list";
    }

    @GetMapping("/forums/{postId}/replies/{replyId}/child-replies")
    public ResponseEntity<List<ForumReplyDTO>> getReplies(@PathVariable("postId") int postId,
                                                          @PathVariable("replyId") int replyId,
                                                          @RequestParam Map<String, String> params) {
        int page = Integer.parseInt(params.getOrDefault("page", "1")) - 1;
        Pageable pageable = PageRequest.of(page, PageSize.FORUM_REPLY_PAGE_SIZE);

        Page<ForumReply> childReplies = forumReplyService.getChildReplies(postId, replyId, pageable);
        List<ForumReplyDTO> repliesDto = childReplies.getContent()
                .stream()
                .map(ForumReplyDTO::new)
                .collect(Collectors.toList());

        return ResponseEntity.ok(repliesDto);
    }


}
