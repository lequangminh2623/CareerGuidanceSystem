package com.lqm.admin_service.clients;

import com.lqm.admin_service.dtos.AdminReplyRequestDTO;
import com.lqm.admin_service.dtos.AdminReplyResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@FeignClient(name = "api-gateway", path = "/forum-service/api/internal/admin/replies", contextId = "replyClient")
public interface ReplyClient {

    @GetMapping
    Page<AdminReplyResponseDTO> getAllReplies(@RequestParam Map<String, String> params);

    @GetMapping("/{id}")
    AdminReplyRequestDTO getReplyRequestById(@PathVariable("id") UUID id);

    @GetMapping("/{id}/children")
    Page<AdminReplyResponseDTO> getChildReplies(@PathVariable("id") UUID replyId,
                                                @RequestParam Map<String, String> params);

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    void saveReply(@ModelAttribute AdminReplyRequestDTO dto);

    @DeleteMapping("/{id}")
    void deleteReply(@PathVariable("id") UUID id);
}