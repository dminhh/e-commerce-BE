package com.prod.RAG.controllers;

import com.common.DTO.ResponseObject;
import com.prod.RAG.model.VisualSearchResult;
import com.prod.RAG.services.IVisualSearchService;
import com.prod.facades.data.ProductInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/visual-search")
public class VisualSearchController {

    @Autowired
    private IVisualSearchService visualSearchService;

    @PostMapping
    public ResponseEntity<ResponseObject<List<ProductInfo>>> searchByImage(
            @RequestParam("image") MultipartFile image,
            @RequestParam(defaultValue = "10") int top_k
    ) {
        // Validate image
        if (image == null || image.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    ResponseObject.<List<ProductInfo>>builder()
                            .message("Ảnh không được để trống.")
                            .isSuccess(false)
                            .build()
            );
        }

        // Validate file size (max 5MB)
        if (image.getSize() > 5 * 1024 * 1024) {
            return ResponseEntity.badRequest().body(
                    ResponseObject.<List<ProductInfo>>builder()
                            .message("Kích thước ảnh phải nhỏ hơn 5MB.")
                            .isSuccess(false)
                            .build()
            );
        }

        // Validate file type (accept all image types except SVG)
        String contentType = image.getContentType();
        if (contentType == null || !contentType.startsWith("image/") || contentType.equals("image/svg+xml")) {
            return ResponseEntity.badRequest().body(
                    ResponseObject.<List<ProductInfo>>builder()
                            .message("Chỉ hỗ trợ file ảnh (JPEG, PNG...).")
                            .isSuccess(false)
                            .build()
            );
        }

        // Validate top_k (0-10)
        if (top_k < 0 || top_k > 10) {
            return ResponseEntity.badRequest().body(
                    ResponseObject.<List<ProductInfo>>builder()
                            .message("top_k phải trong khoảng 0-10.")
                            .isSuccess(false)
                            .build()
            );
        }

        log.info("Received visual search request: file={}, size={}, type={}, top_k={}",
                image.getOriginalFilename(), image.getSize(), contentType, top_k);

        // Call service
        VisualSearchResult result = visualSearchService.searchByImage(image, top_k);

        if (result.getProducts() == null || result.getProducts().isEmpty()) {
            return ResponseEntity.ok().body(
                    ResponseObject.<List<ProductInfo>>builder()
                            .message(result.getMessage())
                            .isSuccess(false)
                            .build()
            );
        } else {
            return ResponseEntity.ok().body(
                    ResponseObject.<List<ProductInfo>>builder()
                            .data(result.getProducts())
                            .message(result.getMessage())
                            .isSuccess(true)
                            .build()
            );
        }
    }
}
