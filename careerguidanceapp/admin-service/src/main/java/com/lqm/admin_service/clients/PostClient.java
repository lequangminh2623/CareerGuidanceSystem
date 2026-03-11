package com.lqm.admin_service.clients;

import com.lqm.admin_service.dtos.AdminPostRequestDTO;
import com.lqm.admin_service.dtos.AdminPostResponseDTO;
import com.lqm.admin_service.dtos.AdminReplyResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@FeignClient(name = "api-gateway", path = "/forum-service/api/internal/admin/posts", contextId = "postClient")
public interface PostClient {

    @GetMapping
    Page<AdminPostResponseDTO> getPosts(@RequestParam Map<String, String> params);

    @GetMapping("/{id}")
    AdminPostRequestDTO getPostRequestById(@PathVariable("id") UUID id);

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    void savePost(@ModelAttribute AdminPostRequestDTO dto);

    @DeleteMapping("/{id}")
    void deletePost(@PathVariable("id") UUID id);

    @GetMapping("/{id}/replies")
    Page<AdminReplyResponseDTO> getReplies(@PathVariable("id") UUID postId,
                                           @RequestParam Map<String, String> params);
}
