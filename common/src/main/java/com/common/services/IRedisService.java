package com.common.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public interface IRedisService {
    String get(String key);
    void save(String key, String value);
    void saveWithTimeLine(String key, String value, long time);
    boolean isKeyALive(String key);
    void delete(String key);
    void saveNotification(String userKey, Object notification) throws JsonProcessingException;
    List<String>  getAllNotifications(String userKey);
    List<String>  getAllNotificationsPageable(String userKey, int page, int pageSize);
    void deleteNotification(String userKey, Object notification) throws JsonProcessingException;
    void deleteAllNotifications(String userId);
    Set<String> getAllUserKeys(String key);
}
