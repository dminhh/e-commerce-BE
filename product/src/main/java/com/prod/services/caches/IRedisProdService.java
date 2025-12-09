package com.prod.services.caches;

import com.prod.models.products.Review;
import com.prod.redis.AccountRedis;
import com.prod.redis.UserRedis;
import org.springframework.stereotype.Service;

@Service
public interface IRedisProdService {
    AccountRedis getAccount(int accountId);
    UserRedis getUserByAccount(int accountId);
    Review getReviewByUserIdAndProductId(int userId, int productId);
    void pushReview(Review review);
}
