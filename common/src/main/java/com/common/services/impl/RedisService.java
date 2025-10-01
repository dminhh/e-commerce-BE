package com.common.services.impl;

import com.common.services.IRedisService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
@Primary
public class RedisService implements IRedisService {
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    @Override
    public String get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    @Override
    public void save(String key, String value) {
        redisTemplate.opsForValue().set(key, value);
    }

    @Override
    public void saveWithTimeLine(String key, String value, long time) {
        redisTemplate.opsForValue().set(key, value, time, TimeUnit.SECONDS);
    }

    @Override
    public boolean isKeyALive(String key) {
        return get(key) != null;
    }

    @Override
    public void delete(String key) {
        redisTemplate.delete(key);
    }


//    nguyen code
    @Override
    public void saveNotification(String userKey, Object notification) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        redisTemplate.opsForList().rightPush(userKey, objectMapper.writeValueAsString(notification));
    }

    @Override
    public List<String> getAllNotifications(String userKey) {
        return redisTemplate.opsForList().range(userKey, 0, -1);
    }

    @Override
    public List<String> getAllNotificationsPageable(String userKey, int page, int pageSize) {
//        return redisTemplate.opsForList().range(userKey, 0, -1);
        long totalSize = redisTemplate.opsForList().size(userKey);
        if (totalSize == 0) {
            return new ArrayList<>();
        }

        // Tính toán start và end
        long start = Math.max(0, totalSize - (page * pageSize)); // Vị trí bắt đầu
        long end = Math.max(0, totalSize - ((page - 1) * pageSize) - 1); // Vị trí kết thúc

        // Điều chỉnh để không vượt quá tổng phần tử
        end = Math.min(totalSize - 1, end);

        // Lấy danh sách từ Redis
        List<String> data = redisTemplate.opsForList().range(userKey, start, end);

        // Đảo ngược danh sách để từ mới nhất đến cũ nhất
        if (data != null) {
            Collections.reverse(data);
        }

        return data;
    }

    @Override
    public void deleteNotification(String userKey, Object notification) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        // Chuyển đổi notification thành chuỗi JSON
        String notificationString = objectMapper.writeValueAsString(notification);

        // Xóa phần tử từ danh sách
        redisTemplate.opsForList().remove(userKey, 1, notificationString);
//        redisTemplate.opsForList().remove(userKey, 1, notification);
    }

    @Override
    public void deleteAllNotifications(String userKey) {
        redisTemplate.delete(userKey);
    }

    @Override
    public Set<String> getAllUserKeys(String key) {
        return redisTemplate.keys(key);
    }

}
