package com.prod.services.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.prod.services.ICloudinaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CloudinaryService implements ICloudinaryService {

    private final Cloudinary cloudinary;

    @Override
    public String uploadImage(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty or null");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("File must be an image");
        }

        try {
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
            String url = (String) uploadResult.get("secure_url");
            log.info("Image uploaded successfully: {}", url);
            return url;
        } catch (IOException e) {
            log.error("Failed to upload image to Cloudinary", e);
            throw new IOException("Failed to upload image to Cloudinary: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteImage(String publicId) throws IOException {
        if (publicId == null || publicId.trim().isEmpty()) {
            throw new IllegalArgumentException("Public ID is empty or null");
        }

        try {
            Map result = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            log.info("Image deleted successfully: {}", publicId);
        } catch (IOException e) {
            log.error("Failed to delete image from Cloudinary: {}", publicId, e);
            throw new IOException("Failed to delete image from Cloudinary: " + e.getMessage(), e);
        }
    }
}
