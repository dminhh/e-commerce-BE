package com.prod.facades;

import com.common.DTO.ResponseObject;
import com.prod.facades.data.ReviewInfo;
import com.prod.models.products.Review;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;


@Service
public interface IReviewFacade {
    ResponseObject<Review> createReview(ReviewInfo review, int userId, String name);
    ResponseObject<Review> updateReview(ReviewInfo review, int userId, String name);
    ResponseObject<Review> getReviewById(int id);
    ResponseObject<Page<Review>> getAllReviewsByProductId(int id, int page, int size, String sortField, String sortDirect);
    ResponseObject<Page<Review>> getAllReviewsByUserId(int id, int page, int size, String sortField, String sortDirect);
    ResponseObject<Page<Review>> getAllReviewsByValueGreaterThan(double value, int page, int size, String sortField, String sortDirect);
    ResponseObject<Page<Review>> getAllReviewsByValueLessThan(double value, int page, int size, String sortField, String sortDirect);
    ResponseObject<Page<Review>> getAllReviewsByValueBetween(double min, double max, int page, int size, String sortField, String sortDirect);
    ResponseObject<Review> getReviewByUserIdAndProductId(int userId, int productId);
}
