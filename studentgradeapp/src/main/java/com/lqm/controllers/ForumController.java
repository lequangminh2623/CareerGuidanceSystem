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
        model.addAttribute("users", userRepo.findByRoleIn(List.of("ROLE_TEACHER", "ROLE_STUDENT")));
        model.addAttribute("classrooms", classroomService.getClassrooms(null, Pageable.unpaged()));
    }

    @GetMapping
    public String listPosts(Model model, @RequestParam Map<String, String> params) {
        int pageNumber = 1;

        String pageParam = params.get("page");
        if (pageParam != null && !pageParam.isEmpty()) {
            try {
                pageNumber = Integer.parseInt(pageParam);
                if (pageNumber < 1) pageNumber = 1;
            } catch (NumberFormatException ignored) {}
        }

        Pageable pageable = PageRequest.of(pageNumber - 1, PageSize.FORUM_POST_PAGE_SIZE);
        Page<ForumPost> page = forumPostService.getForumPosts(params, pageable);

        model.addAttribute("forumPosts", page.getContent());
        model.addAttribute("currentPage", pageNumber);
        model.addAttribute("totalPages", page.getTotalPages());
        model.addAttribute("kw", params.get("kw"));

        return "forum/forum-post-list";
    }

    @GetMapping("/add")
    public String showCreateForm(Model model) {
        model.addAttribute("forumPost", new ForumPost());
        return "forum/forum-post-form";
    }

    @PostMapping
    public String saveForumPost(
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

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePost(@PathVariable int id) {
        forumPostService.deleteForumPostById(id);
    }

    //REPLY
    @GetMapping("/{id}/replies")
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

    @GetMapping("/{postId}/replies/{replyId}/child-replies")
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
