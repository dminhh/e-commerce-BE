package com.prod.services.elk.impl;

import com.prod.JPARepositories.elk.ESProductRepository;
import com.prod.models.elk.ESProducts;
import com.prod.services.elk.IESProductService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@AllArgsConstructor
@Primary
public class ESProductService implements IESProductService {
    @Autowired
    private ESProductRepository elkProductRepository;

    @Override
    public Page<ESProducts> getSortedProducts(int page, int size) {
        // Tạo đối tượng Pageable với số trang và kích thước trang
        Pageable pageable = PageRequest.of(page, size);
        return elkProductRepository.findAllByOrderByScoreDescUpdateAtDesc(pageable);
    }

    @Override
    public ESProducts createProduct(ESProducts product) {
        if (elkProductRepository.existsByDbId(product.getDbId())) {
            return null;
        }

        // Nếu sản phẩm chưa tồn tại, thì lưu vào Elasticsearch
        return elkProductRepository.save(product);
    }

    @Override
    public boolean createListProduct(List<ESProducts> products) {
        try {
            elkProductRepository.saveAll(products);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public ESProducts updateProduct(String dbId, ESProducts updatedProduct) {
    //dbId la product id trong db

        try {
            Optional<ESProducts> existingProductOptional = elkProductRepository.findByDbId(dbId);

            if (existingProductOptional.isPresent()) {
                ESProducts existingProduct = existingProductOptional.get();

                // Cập nhật các trường cần thiết từ updatedProduct
                existingProduct.setTitle(updatedProduct.getTitle());
                existingProduct.setPrice(updatedProduct.getPrice());
                existingProduct.setDiscount(updatedProduct.getDiscount());
                existingProduct.setRating(updatedProduct.getRating());
                existingProduct.setSold(updatedProduct.getSold());
                existingProduct.setImages(updatedProduct.getImages());
                existingProduct.setDbId(updatedProduct.getDbId());  // Cập nhật dbId nếu cần

                // Lưu lại sản phẩm đã cập nhật vào Elasticsearch
                return elkProductRepository.save(existingProduct);
            } else {
                return null;
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            return null;
        }
    }

    @Override
    public ESProducts findProductByDbId(String dbId) {
        try {
            Optional<ESProducts> existingProductOptional = elkProductRepository.findByDbId(dbId);

            return existingProductOptional.orElse(null);
        } catch (Exception e) {
            log.error(e.getMessage());
            return null;
        }
    }

    @Override
    public ESProducts updateScore(String dbId, double scoreChange) {
        try {
            Optional<ESProducts> existingProductOptional = elkProductRepository.findByDbId(dbId);

            if (existingProductOptional.isPresent()) {
                ESProducts existingProduct = existingProductOptional.get();

                // Cập nhật điểm sản phẩm
                double newScore = existingProduct.getScore() + scoreChange;
                existingProduct.setScore(newScore);

                // Lưu lại sản phẩm đã cập nhật score vào Elasticsearch
                return elkProductRepository.save(existingProduct);
            } else {
                return null;
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            return null;
        }
    }
    // ESProductServiceImpl.java
    public Page<ESProducts> searchProducts(String key, int page, int size) {
        // 1. Xử lý null để tránh NullPointerException
        if (key == null) {
            key = "";
        }

        // 2. "Làm sạch" từ khóa: Xóa bỏ ngoặc kép và dấu sao do người dùng nhập
        // Nếu người dùng nhập: "bộ quần áo" -> Sẽ thành: bộ quần áo
        String sanitizedKey = key.replace("\"", "").replace("*", "").trim();

        // 3. Gọi Repository với từ khóa đã làm sạch
        // (Giả sử bạn đang dùng PageRequest để phân trang)
        Pageable pageable = PageRequest.of(page, size);

        return elkProductRepository.findByTitle(sanitizedKey, pageable);
    }
}
