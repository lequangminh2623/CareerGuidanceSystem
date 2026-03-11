package com.lqm.admin_service.controllers;

import com.lqm.admin_service.clients.PostClient;
import com.lqm.admin_service.clients.ReplyClient;
import com.lqm.admin_service.clients.UserClient;
import com.lqm.admin_service.dtos.AdminReplyRequestDTO;
import com.lqm.admin_service.dtos.AdminReplyResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller
@RequestMapping("/replies")
@RequiredArgsConstructor
public class ReplyController {

    private final UserClient userClient;
    private final ReplyClient forumClient;
    private final PostClient postClient;

    @ModelAttribute("commonData")
    public void populateCommon(Model model) {
        Map<String, String> roleParams = Map.of("role", "ROLE_TEACHER,ROLE_STUDENT");
        model.addAttribute("users", userClient.getUsers(List.of(), roleParams).getContent());
    }

    @GetMapping
    public String listReplies(Model model, @RequestParam Map<String, String> params) {
        Page<AdminReplyResponseDTO> replyDTOPage = forumClient.getAllReplies(params);

        model.addAttribute("replies", replyDTOPage);
        model.addAttribute("kw", params.get("kw"));

        return "/forum/reply-list";
    }

    @GetMapping("/{replyId}/child-replies")
    public ResponseEntity<Page<AdminReplyResponseDTO>> getChildReplies(@PathVariable("replyId") UUID replyId,
                                                                       @RequestParam Map<String, String> params) {
        Page<AdminReplyResponseDTO> childReplyDTOPage = forumClient.getChildReplies(replyId, params);

        return ResponseEntity.ok(childReplyDTOPage);
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("replies", forumClient.getAllReplies(Map.of()).getContent());
        model.addAttribute("posts", postClient.getPosts(Map.of()).getContent());
        model.addAttribute("reply", AdminReplyRequestDTO.builder().build());

        return "/forum/reply-form";
    }

    @GetMapping("/{id}")
    public String showEditForm(@PathVariable UUID id, Model model) {
        AdminReplyRequestDTO replyDTO = forumClient.getReplyRequestById(id);
        List<AdminReplyResponseDTO> replies = forumClient.getAllReplies(Map.of())
                .getContent()
                .stream()
                .filter(r -> !r.id().equals(id))
                .toList();

        model.addAttribute("posts", postClient.getPosts(Map.of()).getContent());
        model.addAttribute("replies", replies);
        model.addAttribute("reply", replyDTO);

        return "/forum/reply-form";
    }

    @PostMapping
    public String saveReply(@ModelAttribute("reply") @Valid AdminReplyRequestDTO dto,
                            BindingResult bindingResult,
                            Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("errorMessage", "Có lỗi xảy ra");
            return "/forum/reply-form";
        }
        forumClient.saveReply(dto);

        return "redirect:/replies";
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteReply(@PathVariable UUID id) {
        forumClient.deleteReply(id);
    }
}