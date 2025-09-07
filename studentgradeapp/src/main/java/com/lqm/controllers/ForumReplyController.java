// src/main/java/com/lqm/controllers/ForumReplyController.java
package com.lqm.controllers;

import com.lqm.models.ForumPost;
import com.lqm.models.ForumReply;
import com.lqm.models.User;
import com.lqm.repositories.UserRepository;
import com.lqm.services.ForumReplyService;
import com.lqm.services.ForumPostService;
import com.lqm.services.UserService;
import com.lqm.utils.PageSize;
import com.lqm.validators.WebAppValidator;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
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
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
public class ForumReplyController {

    @Autowired
    private ForumReplyService forumReplyService;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private ForumPostService forumPostService;

    @Autowired
    @Qualifier("webAppValidator")
    private WebAppValidator webAppValidator;
    @Autowired
    private UserService userService;

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.setValidator(webAppValidator);
    }

    @ModelAttribute
    public void addCommonAttributes(Model model) {
        List<String> roles = List.of("ROLE_TEACHER", "ROLE_STUDENT");
        model.addAttribute("users", userRepo.findByRoleIn(roles));
        model.addAttribute("forumPosts", forumPostService.getForumPosts(null, Pageable.unpaged()));
    }

    @GetMapping("/replies")
    public String listReplies(Model model, @RequestParam Map<String, String> params) {
        int pageNumber = 1;

        // Lấy page từ params và validate
        String pageParam = params.get("page");
        if (pageParam != null && !pageParam.isEmpty()) {
            try {
                pageNumber = Integer.parseInt(pageParam);
                if (pageNumber < 1) pageNumber = 1;
            } catch (NumberFormatException ignored) {}
        }

        Pageable pageable = PageRequest.of(pageNumber - 1, PageSize.FORUM_REPLY_PAGE_SIZE);

        Page<ForumReply> replyPage = forumReplyService.getAllReplies(params, pageable);

        model.addAttribute("replies", replyPage.getContent());
        model.addAttribute("currentPage", pageNumber);
        model.addAttribute("totalPages", replyPage.getTotalPages());
        model.addAttribute("kw", params.get("kw"));

        return "/forum/reply-list";
    }


    @GetMapping("/replies/add")
    public String showAddForm(Model model) {
        List<ForumReply> replies = this.forumReplyService.getAllReplies(null, Pageable.unpaged()).getContent();
        model.addAttribute("replies", replies);
        model.addAttribute("reply", new ForumReply());
        return "/forum/reply-form";
    }

    @GetMapping("/replies/{id}")
    public String showEditForm(@PathVariable int id, Model model) {
        ForumReply reply = forumReplyService.getReplyById(id);
        List<ForumReply> replies = this.forumReplyService.getAllReplies(null, Pageable.unpaged())
                .getContent()
                .stream()
                .filter(r -> !r.getId().equals(id)) // loại bỏ reply hiện tại
                .toList();        model.addAttribute("replies", replies);
        model.addAttribute("reply", reply);
        return "/forum/reply-form";
    }

    @PostMapping("/replies")
    public String saveReply(
            @ModelAttribute("reply") @Valid ForumReply reply,
            BindingResult bindingResult,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("errorMessage", "Có lỗi xảy ra");
            return "/forum/reply-form";
        }

        forumReplyService.saveReply(reply);
        return "redirect:/replies";
    }

    @DeleteMapping("/replies/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteReply(@PathVariable int id) {
        forumReplyService.deleteReply(id);
    }
}
