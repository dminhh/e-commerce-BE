package com.prod.facades.impl;

import com.common.DTO.ResponseObject;
import com.prod.facades.IReviewFacade;
import com.prod.facades.data.ReviewInfo;
import com.prod.models.products.Review;
import com.prod.services.elk.IESProductService;
import com.prod.services.products.IProductService;
import com.prod.services.products.IReviewService;
import com.prod.utils.ConvertListPage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
public class ReviewFacade implements IReviewFacade {
    @Autowired
    private IReviewService reviewService;
    @Autowired
    private ConvertListPage<Review> convertListPage;
    @Autowired
    private IProductService productService;
    @Autowired
    private IESProductService service;

    @Override
    public ResponseObject<Review> createReview(ReviewInfo review, int userId, String name) {
        Review newReview = reviewService.createReview(Review.builder()
                .product_id(review.getProduct_id())
                .user_id(userId)
                .value(review.getValue())
                .user_name(name)
                .content(Objects.equals(review.getContent(), "") ? "" : review.getContent())
                .build());
        productService.updateProductWithReviews(review.getProduct_id(), newReview);
        try {
            service.updateScore(newReview.getProduct_id() + "", Math.ceil(review.getValue()));
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return ResponseObject.<Review>builder()
                .message("Tao review thanh cong")
                .isSuccess(true)
                .data(newReview)
                .build();
    }

    @Override
    public ResponseObject<Review> updateReview(ReviewInfo review, int userId, String name) {
        Optional<Review> review1 = reviewService.getReviewByProductIdAndUserId(review.getProduct_id(), userId);
        if (review1.isPresent()) {
            review1.get().setUpdated_at(LocalDateTime.now());
            review1.get().setContent(Objects.equals(review.getContent(), "") ? "" : review.getContent());
            return ResponseObject.<Review>builder()
                    .message("Cap nhat review thanh cong")
                    .data(reviewService.createReview(review1.get()))
                    .build();
        } else
            return ResponseObject.<Review>builder()
                    .message("Khong tim thay review theo product id va user id")
                    .build();
    }

    @Override
    public ResponseObject<Review> getReviewById(int id) {
        Optional<Review> review = reviewService.getReviewById(id);
        if (review.isPresent()) {
            return ResponseObject.<Review>builder()
                    .isSuccess(true)
                    .message("Lay review thanh cong")
                    .data(review.get())
                    .build();
        } else return ResponseObject.<Review>builder()
                .message("Khong the tim review")
                .build();
    }

    @Override
    public ResponseObject<Page<Review>> getAllReviewsByProductId(int id, int page, int size, String sortField, String sortDirect) {
        List<Review> review = reviewService.getPageReviewsByProductId(id, page, size, sortField, sortDirect).getContent();
        if (!review.isEmpty()) {
            return ResponseObject.<Page<Review>>builder()
                    .isSuccess(true)
                    .message("Lay danh sach review thanh cong")
                    .data(convertListPage.listToPage(review, page, size))
                    .build();
        } else return ResponseObject.<Page<Review>>builder()
                .message("Khong the tim danh sach review")
                .build();
    }

    @Override
    public ResponseObject<Page<Review>> getAllReviewsByUserId(int id, int page, int size, String sortField, String sortDirect) {
        List<Review> review = reviewService.getPageReviewsByUserId(id, page, size, sortField, sortDirect).getContent();
        if (!review.isEmpty()) {
            return ResponseObject.<Page<Review>>builder()
                    .isSuccess(true)
                    .message("Lay danh sach review thanh cong")
                    .data(convertListPage.listToPage(review, page, size))
                    .build();
        } else return ResponseObject.<Page<Review>>builder()
                .message("Khong the tim danh sach review")
                .build();
    }

    @Override
    public ResponseObject<Page<Review>> getAllReviewsByValueGreaterThan(double value, int page, int size, String sortField, String sortDirect) {
        List<Review> review = reviewService.getPageReviewsByValueGreaterThan(value, page, size, sortField, sortDirect).getContent();
        if (!review.isEmpty()) {
            return ResponseObject.<Page<Review>>builder()
                    .isSuccess(true)
                    .message("Lay danh sach review thanh cong")
                    .data(convertListPage.listToPage(review, page, size))
                    .build();
        } else return ResponseObject.<Page<Review>>builder()
                .message("Khong the tim danh sach review")
                .build();
    }

    @Override
    public ResponseObject<Page<Review>> getAllReviewsByValueLessThan(double value, int page, int size, String sortField, String sortDirect) {
        List<Review> review = reviewService.getPageReviewsByValueLessThan(value, page, size, sortField, sortDirect).getContent();
        if (!review.isEmpty()) {
            return ResponseObject.<Page<Review>>builder()
                    .isSuccess(true)
                    .message("Lay danh sach review thanh cong")
                    .data(convertListPage.listToPage(review, page, size))
                    .build();
        } else return ResponseObject.<Page<Review>>builder()
                .message("Khong the tim danh sach review")
                .build();
    }

    @Override
    public ResponseObject<Page<Review>> getAllReviewsByValueBetween(double min, double max, int page, int size, String sortField, String sortDirect) {
        List<Review> review = reviewService.getPageReviewsByValueBetween(min, max, page, size, sortField, sortDirect).getContent();
        if (!review.isEmpty()) {
            return ResponseObject.<Page<Review>>builder()
                    .isSuccess(true)
                    .message("Lay danh sach review thanh cong")
                    .data(convertListPage.listToPage(review, page, size))
                    .build();
        } else return ResponseObject.<Page<Review>>builder()
                .message("Khong the tim danh sach review")
                .build();
    }

    @Override
    public ResponseObject<Review> getReviewByUserIdAndProductId(int userId, int productId) {
        Optional<Review> review = reviewService.getReviewByProductIdAndUserId(productId, userId);
        if (!review.isPresent()) {
            return ResponseObject.<Review>builder()
                    .message("Khong tim thay review theo user va product")
                    .build();
        } else {
            return ResponseObject.<Review>builder()
                    .isSuccess(true)
                    .message("Lay review theo user va product thanh cong")
                    .data(review.get())
                    .build();
        }
    }
}
