package com.notification.services.impl;

import com.common.services.IRedisService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.notification.data.Notification;
import com.notification.services.INotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class NotificationService implements INotificationService {

    private static final String REDIS_KEY_PREFIX = "notifications:"; // Prefix cho từng user
    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);
    private static final long EXPIRATION_TIME = 60L * 24 * 60 * 60 * 1000; // 60 ngày tính bằng millisecond
    //
//    @Autowired
//    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private SimpMessagingTemplate messagingTemplate; // Gửi thông báo qua WebSocket
    @Autowired
    private IRedisService redisService;
    @Autowired
    private ObjectMapper objectMapper;

    // Lưu thông báo cho người dùng
    @Override
    public void saveNotification(String userId, String message) {
        String id = UUID.randomUUID().toString(); // Tạo ID thông báo
        Notification notification = new Notification(id, message, String.valueOf(System.currentTimeMillis()));

        // Key Redis của người dùng
        String userKey = REDIS_KEY_PREFIX + userId;

        // Thêm thông báo vào danh sách của người dùng
        // redisTemplate.opsForList().rightPush(userKey, notification);
        try {
            redisService.saveNotification(userKey,notification);
            messagingTemplate.convertAndSend("/topic/notifications/"+userId , notification);
        }
        catch (Exception e){
            log.error(e.getMessage());
        }

    }

    // Lấy tất cả thông báo của người dùng
    @Override
    public List<String> getAllNotifications(String userId) {
        String userKey = REDIS_KEY_PREFIX + userId;

        // Lấy toàn bộ danh sách thông báo
//        return redisTemplate.opsForList().range(userKey, 0, -1);
        return redisService.getAllNotifications(userKey);
    }

    @Override
    public List<String> getAllNotificationsPageable(String userId,int page,int limit) {
        String userKey = REDIS_KEY_PREFIX + userId;

        // Lấy toàn bộ danh sách thông báo
//        return redisTemplate.opsForList().range(userKey, 0, -1);
        return redisService.getAllNotificationsPageable(userKey,page,limit);
    }

    // Xóa một thông báo cụ thể của người dùng
    @Override
    public void deleteNotification(String userId, String notificationId) {
        String userKey = REDIS_KEY_PREFIX + userId;

        // Lấy tất cả thông báo
        List<String> redisData =redisService.getAllNotifications(userKey);
//                redisTemplate.opsForList().range(userKey, 0, -1);
        ObjectMapper objectMapper = new ObjectMapper();

        List<Notification> notifications = new ArrayList<>();
        for (String json : redisData) {
            try {
                // Convert JSON string to Notification object
                Notification notification = objectMapper.readValue(json, Notification.class);
                notifications.add(notification);
            } catch (Exception e) {
                // Handle exception (e.g., log the error)
                log.error(e.getMessage());
            }
        }
        // Tìm thông báo cần xóa
        if (notifications != null) {
            for (Object obj : notifications) {
                if (obj instanceof Notification) {
                    Notification notification = (Notification) obj;

                    if (notification.getId().equals(notificationId)) {
                        // Xóa thông báo cụ thể
//                        redisTemplate.opsForList().remove(userKey, 1, notification);
                        try {
                            redisService.deleteNotification(userKey,notification);
                        }catch(Exception e){log.error(e.getMessage());}

                        break;
                    }
                }
            }
        }
    }

    // Xóa toàn bộ thông báo của người dùng
    @Override
    public void deleteAllNotifications(String userId) {
        String userKey = REDIS_KEY_PREFIX + userId;

        // Xóa danh sách thông báo của người dùng
//        redisTemplate.delete(userKey);
        redisService.deleteAllNotifications(userKey);
    }

    // 2. Xóa thông báo quá hạn
    public void deleteOldNotifications(String userKey) {
        // Lấy tất cả thông báo từ Redis List
        List<String> notifications = redisService.getAllNotifications(userKey);

        // Thời gian hiện tại
        long currentTime = Instant.now().toEpochMilli();

        // Duyệt qua từng thông báo và kiểm tra thời gian
        for (String notificationJson : notifications) {
            // Chuyển chuỗi JSON thành đối tượng Notification
            Notification notification = null;
//                    objectMapper.readValue(notificationJson, Notification.class);
            try {
                // Convert JSON string to Notification object
                notification = objectMapper.readValue(notificationJson, Notification.class);
                // Kiểm tra thời gian hết hạn (60 ngày)
                long notificationTime = Long.parseLong(notification.getTimestamp());
                if (currentTime - notificationTime > EXPIRATION_TIME) {
                    // Nếu quá hạn, xóa phần tử khỏi List Redis
                    try {
                        redisService.deleteNotification(userKey,notification);
                    }catch(Exception e){log.error(e.getMessage());}
//                redisTemplate.opsForList().remove(userKey, 1, notificationJson);
                }
            } catch (Exception e) {
                // Handle exception (e.g., log the error)
                log.error(e.getMessage());
            }


        }

        System.out.println("Old notifications removed for user: " + userKey);
    }
    // 3. Lên lịch tự động xóa thông báo cũ mỗi ngày vào lúc 2:00 AM
    @Scheduled(cron = "0 0 2 * * ?")
    public void scheduledDeleteOldNotifications()  {
        // Lấy tất cả userKeys (giả sử bạn có phương thức lấy tất cả user keys từ Redis hoặc database)
        List<String> userKeys = getAllUserKeys(); // Bạn cần cài đặt phương thức này

        // Xóa thông báo cũ cho tất cả người dùng
        for (String userKey : userKeys) {
            deleteOldNotifications(userKey);
        }

        System.out.println("Scheduled cleanup of old notifications completed.");
    }
    private List<String> getAllUserKeys() {

        return new ArrayList<>(redisService.getAllUserKeys("notifications:*"));  // Giả sử các keys là "notifications:userId"
    }
}
