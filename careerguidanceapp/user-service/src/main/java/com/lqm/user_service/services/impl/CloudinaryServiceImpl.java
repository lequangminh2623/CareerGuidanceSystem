package com.lqm.user_service.services.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.lqm.user_service.services.CloudinaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CloudinaryServiceImpl implements CloudinaryService {

    private final Cloudinary cloudinary;
    private final MessageSource messageSource;

    @Override
    public String uploadFile(MultipartFile file) {
        if (file == null || file.isEmpty()) return null;

        try {
            Map<?, ?> res = cloudinary.uploader().upload(file.getBytes(),
                    ObjectUtils.asMap(
                            "resource_type", "auto",
                            "folder", "CareerGuidanceSystem/Avatar"
                    ));
            return res.get("secure_url").toString();
        } catch (IOException ex) {
            throw new RuntimeException(messageSource.getMessage("error", null, Locale.getDefault()), ex);
        }
    }

    @Override
    public void deleteFile(String url) {
        String publicId = extractPublicId(url);
        if (publicId == null || "CareerGuidanceSystem/Avatar/download_kys5gs".equals(publicId)) return;

        try {
            Map<?, ?> res = cloudinary.uploader().destroy(
                    publicId,
                    ObjectUtils.asMap("resource_type", "image")
            );

            res.get("result");
        } catch (Exception ex) {
            throw new RuntimeException(messageSource.getMessage("error", null, Locale.getDefault()), ex);
        }
    }


    private String extractPublicId(String url) {
        if (url == null || url.isBlank()) return null;

        try {
            // Xóa query string nếu có
            String path = url.split("\\?")[0];

            // Lấy phần sau /upload/
            int uploadIndex = path.indexOf("/upload/");
            if (uploadIndex == -1) return null;

            path = path.substring(uploadIndex + 8); // 8 là độ dài "/upload/"

            // Nếu có version, bỏ nó (v12345/...)
            if (path.startsWith("v")) {
                int slashIndex = path.indexOf('/');
                if (slashIndex != -1) {
                    path = path.substring(slashIndex + 1);
                }
            }

            // Bỏ đuôi file (.png, .jpg, .jpeg, .gif, ...)
            int dotIndex = path.lastIndexOf('.');
            if (dotIndex != -1) {
                path = path.substring(0, dotIndex);
            }

            return path;
        } catch (Exception e) {
            return null;
        }
    }

}