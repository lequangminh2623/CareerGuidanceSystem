package com.lqm.user_service.services;

import org.springframework.web.multipart.MultipartFile;

public interface CloudinaryService {
    String uploadFile(MultipartFile file);
    void deleteFile(String url);
}
