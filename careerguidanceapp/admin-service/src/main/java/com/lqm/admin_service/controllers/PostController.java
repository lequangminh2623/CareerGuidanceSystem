package com.lqm.admin_service.controllers;

import com.lqm.admin_service.clients.PostClient;
import com.lqm.admin_service.clients.SectionClient;
import com.lqm.admin_service.clients.UserClient;
import com.lqm.admin_service.dtos.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

    private final UserClient userClient;
    private final SectionClient sectionClient;
    private final PostClient postClient;

    // API TÌM KIẾM NGƯỜI ĐĂNG
    @GetMapping("/users/search")
    @ResponseBody
    public Map<String, Object> searchUsers(
            @RequestParam(value = "term", required = false, defaultValue = "") String term,
            @RequestParam(value = "page", required = false, defaultValue = "1") int page) {

        Map<String, String> searchParams = new HashMap<>();
        searchParams.put("role", "ROLE_TEACHER,ROLE_STUDENT");
        searchParams.put("page", String.valueOf(page > 0 ? page - 1 : 0));

        if (!term.isBlank()) {
            searchParams.put("kw", term);
        }

        Page<UserResponseDTO> userPage = userClient.getUsers(List.of(), searchParams);

        List<Map<String, Object>> results = userPage.getContent().stream().map(u -> {
            Map<String, Object> item = new HashMap<>();
            item.put("id", u.id().toString());
            item.put("text", u.lastName() + " " + u.firstName());
            return item;
        }).toList();

        return Map.of(
                "results", results,
                "hasMore", !userPage.isLast()
        );
    }

    // API TÌM KIẾM DIỄN ĐÀN (SECTION)
    @GetMapping("/sections/search")
    @ResponseBody
    public Map<String, Object> searchSections(
            @RequestParam(value = "term", required = false, defaultValue = "") String term,
            @RequestParam(value = "page", required = false, defaultValue = "1") int page) {

        Map<String, String> searchParams = new HashMap<>();
        searchParams.put("page", String.valueOf(page > 0 ? page - 1 : 0));

        if (!term.isBlank()) {
            searchParams.put("kw", term);
        }

        Page<SectionResponseDTO> sectionPage = sectionClient.getSectionResponses(List.of(), searchParams);

        List<Map<String, Object>> results = sectionPage.getContent().stream().map(f -> {
            Map<String, Object> item = new HashMap<>();
            item.put("id", f.id().toString());
            // Format text giống hệt yêu cầu của bạn
            String text = String.format("%s - %s (%s, %s %s)",
                    f.subjectName(), f.classroomName(), f.gradeName(), f.semesterName(), f.yearName());
            item.put("text", text);
            return item;
        }).toList();

        return Map.of(
                "results", results,
                "hasMore", !sectionPage.isLast()
        );
    }

    @GetMapping
    public String listPosts(Model model, @RequestParam Map<String, String> params) {

        Page<AdminPostResponseDTO> postDTOPage = postClient.getPosts(params);

        model.addAttribute("posts", postDTOPage);
        model.addAttribute("params", params);
        String ownerIdStr = params.get("ownerId");
        if (ownerIdStr != null && !ownerIdStr.isBlank()) {
            try {
                UserResponseDTO selectedOwner = userClient.getUserResponseById(UUID.fromString(ownerIdStr));
                model.addAttribute("selectedOwner", selectedOwner);
            } catch (Exception e) {
                // Bỏ qua nếu lỗi hoặc ID không tồn tại
            }
        }

        // 2. Nếu có lọc theo Diễn đàn -> Lấy đúng 1 Section đó về
        String sectionIdStr = params.get("sectionId");
        if (sectionIdStr != null && !sectionIdStr.isBlank()) {
            try {
                SectionResponseDTO selectedSection = sectionClient.getSectionResponseById(UUID.fromString(sectionIdStr));
                model.addAttribute("selectedSection", selectedSection);
            } catch (Exception e) {
                // Bỏ qua
            }
        }

        return "forum/post-list";
    }

    @GetMapping("/add")
    public String showCreateForm(Model model) {
        model.addAttribute("post", AdminPostRequestDTO.builder().build());

        return "forum/post-form";
    }

    @PostMapping
    public String savePost(@ModelAttribute("post") @Valid AdminPostRequestDTO dto,
                           BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "post-form";
        }
        postClient.savePost(dto);

        return "redirect:/forums";
    }

    @GetMapping("/{id}")
    public String showUpdateForm(@PathVariable UUID id, Model model) {
        AdminPostRequestDTO postDTO = postClient.getPostRequestById(id);
        model.addAttribute("post", postDTO);

        return "forum/post-form";
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePost(@PathVariable UUID id) {
        postClient.deletePost(id);
    }

    @GetMapping("/{id}/replies")
    public String getForumReplies(Model model,
                                  @PathVariable("id") UUID postId,
                                  @RequestParam Map<String, String> params) {
        Page<AdminReplyResponseDTO> replyDTOPage = postClient.getReplies(postId, params);

        model.addAttribute("replies", replyDTOPage);
        model.addAttribute("params", params);

        return "/forum/reply-list";
    }
}