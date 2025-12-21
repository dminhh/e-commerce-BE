package com.prod.controllers;

import com.common.DTO.ResponseObject;
import com.prod.services.ICloudinaryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/upload")
public class UploadController {

    @Autowired
    private ICloudinaryService cloudinaryService;

    @PostMapping("/image")
    public ResponseEntity<?> uploadImage(@RequestParam("fileInput") MultipartFile file) {
        try {
            if (file == null || file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ResponseObject.builder()
                                .message("File is empty or null")
                                .build());
            }

            String imageUrl = cloudinaryService.uploadImage(file);

            return ResponseEntity.ok()
                    .body(Map.of("fileUrl", imageUrl));

        } catch (IllegalArgumentException e) {
            log.error("Invalid file: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ResponseObject.builder()
                            .message(e.getMessage())
                            .build());
        } catch (Exception e) {
            log.error("Failed to upload image", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResponseObject.builder()
                            .message("Failed to upload image: " + e.getMessage())
                            .build());
        }
    }

    @DeleteMapping("/image")
    public ResponseEntity<?> deleteImage(@RequestParam String publicId) {
        try {
            if (publicId == null || publicId.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ResponseObject.builder()
                                .message("Public ID is required")
                                .build());
            }

            cloudinaryService.deleteImage(publicId);
            return ResponseEntity.ok()
                    .body(ResponseObject.builder()
                            .message("Delete image successfully")
                            .build());
        } catch (Exception e) {
            log.error("Failed to delete image: {}", publicId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResponseObject.builder()
                            .message("Failed to delete image: " + e.getMessage())
                            .build());
        }
    }
}
