package com.notification.controllers;

import com.common.controllers.ControllerUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.notification.data.Notification;
import com.notification.data.SendMessage;
import com.notification.services.impl.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private static final Logger log = LoggerFactory.getLogger(NotificationController.class);
    @Autowired
    private NotificationService notificationService;

    // Tạo thông báo cho một người dùng cụ thể
    @PostMapping("/{userId}")
    public  ResponseEntity<Object>  createNotification(@PathVariable String userId, @RequestBody SendMessage request) {
        try{
            notificationService.saveNotification(userId, request.getMessage());
            return  ControllerUtil.ok(null);
        }catch(Exception e){
            log.error(e.getMessage());
            return ControllerUtil.invalidated(null,"error");
        }
    }

    // Lấy tất cả thông báo của một người dùng cụ thể
    @GetMapping("/{userId}")
    public ResponseEntity<Object> getNotifications(
            @PathVariable String userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit
    ) {
        try {
            List<String> redisData=notificationService.getAllNotificationsPageable(userId,page,limit);
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
            return ControllerUtil.ok(notifications);
        }
        catch (Exception e){
            log.error(e.getMessage());
            return  ControllerUtil.invalidated(null,"Khong lay duoc du lieu");
        }

    }

    // Xóa một thông báo cụ thể của người dùng
    @DeleteMapping("/{userId}/{id}")
    public  ResponseEntity<Object>  deleteNotification(@PathVariable String userId, @PathVariable String id) {
        try {
            notificationService.deleteNotification(userId, id);
            return ControllerUtil.ok("Notification with ID: " + id + " deleted successfully for user: " + userId);
        }
        catch (Exception e){
            log.error(e.getMessage());
            return  ControllerUtil.invalidated(null,"Xóa thất bại");
        }

    }

    // Xóa tất cả thông báo của một người dùng
    @DeleteMapping("/{userId}")
    public  ResponseEntity<Object>  deleteAllNotifications(@PathVariable String userId) {
        try {
            notificationService.deleteAllNotifications(userId);
            return ControllerUtil.ok("All notifications deleted successfully for user: " + userId);
        }
        catch (Exception e){
            log.error(e.getMessage());
            return  ControllerUtil.invalidated(null,"Xóa thất bại");
        }
    }
}
