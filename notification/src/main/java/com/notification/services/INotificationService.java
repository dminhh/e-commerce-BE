package com.notification.services;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface INotificationService {
    public void saveNotification(String userId, String message);

    public List<String> getAllNotifications(String userId);
    List<String> getAllNotificationsPageable(String userId,int page,int limit);
    public void deleteNotification(String userId, String notificationId);
    public void deleteAllNotifications(String userId);
}
