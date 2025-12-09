package com.prod.services.caches.impl;

import com.common.utils.ConvertJson;
import com.prod.models.products.Review;
import com.prod.redis.AccountRedis;
import com.prod.redis.UserRedis;
import com.prod.services.caches.IRedisProdService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@Primary
public class RedisInfoService implements IRedisProdService {
    @Autowired
    private com.common.services.impl.RedisService redisService;
    @Autowired
    private ConvertJson convertJson;
    @Override
    public AccountRedis getAccount(int account_id) {
        try {
            String key = "account_id:" + account_id;
            String value = redisService.get(key);
            if(value == null) {
                log.info("Account {} not found", account_id);
                return null;
            } else {
                log.info("Account {} found", account_id);
                return convertJson.convertFromJson(value, AccountRedis.class);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            return null;
        }
    }

    @Override
    public UserRedis getUserByAccount(int accountId) {
        try {
            String key = "account_id:" + accountId + ":user";
            String value = redisService.get(key);
            if(value == null) {
                log.info("User with account {} not found", accountId);
                return null;
            } else {
                log.info("User with account {} found", accountId);
                return convertJson.convertFromJson(value, UserRedis.class);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            return null;
        }
    }

    @Override
    public Review getReviewByUserIdAndProductId(int userId, int productId) {
        try {
            String key = "user_id:" + userId + ":product_id" + productId + ":review";
            String value = redisService.get(key);
            if(value != null) {
                return convertJson.convertFromJson(value, Review.class);
            }
            return null;
        } catch (Exception e) {
            log.error(e.getMessage());
            return null;
        }
    }

    @Override
    public void pushReview(Review review) {
        try {
            String key = "user_id:" + review.getUser_id() + ":product_id" + review.getProduct_id() + ":review";
            String value = convertJson.convertToJson(review);
            redisService.save(key, value);
        } catch (Exception e){
            log.error(e.getMessage());
        }
    }
}
