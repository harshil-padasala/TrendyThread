package com.trendythread.app.services;

import com.trendythread.app.dto.NotificationDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface NotificationService {

    /**
     * Creates a notification for a recipient. Silently does nothing if
     * recipientBloggerId is null (e.g. a post with no owner) - callers should
     * still skip calling this when recipient == actor to avoid self-notifications.
     */
    void createNotification(Integer recipientBloggerId, String type, String message, Integer postId);

    List<NotificationDto> getMyNotifications(String email, int limit);

    long getUnreadCount(String email);

    void markAsRead(Integer notificationId, String email);

    void markAllAsRead(String email);
}
