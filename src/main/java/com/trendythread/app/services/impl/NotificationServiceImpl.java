package com.trendythread.app.services.impl;

import com.trendythread.app.dto.NotificationDto;
import com.trendythread.app.entities.Blogger;
import com.trendythread.app.entities.Notification;
import com.trendythread.app.exceptions.ResourceNotFoundException;
import com.trendythread.app.repositories.BloggersRepository;
import com.trendythread.app.repositories.NotificationRepository;
import com.trendythread.app.services.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private BloggersRepository bloggersRepository;

    @Override
    @Transactional
    public void createNotification(Integer recipientBloggerId, String type, String message, Integer postId) {
        if (recipientBloggerId == null) {
            return;
        }

        Blogger recipient = bloggersRepository.findById(recipientBloggerId).orElse(null);
        if (recipient == null) {
            log.warn("createNotification - recipient bloggerId={} not found, skipping", recipientBloggerId);
            return;
        }

        Notification notification = new Notification();
        notification.setRecipient(recipient);
        notification.setType(type);
        notification.setMessage(message);
        notification.setPostId(postId);
        notification.setRead(false);

        notificationRepository.save(notification);
        log.debug("createNotification - created {} notification for bloggerId={}", type, recipientBloggerId);
    }

    @Override
    public List<NotificationDto> getMyNotifications(String email, int limit) {
        return notificationRepository
                .findByRecipientEmailOrderByCreatedAtDesc(email, PageRequest.of(0, limit, Sort.by("createdAt").descending()))
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public long getUnreadCount(String email) {
        return notificationRepository.countByRecipientEmailAndReadFalse(email);
    }

    @Override
    @Transactional
    public void markAsRead(Integer notificationId, String email) {
        Notification notification = notificationRepository.findByIdAndRecipientEmail(notificationId, email)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", "id", notificationId));
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void markAllAsRead(String email) {
        notificationRepository.markAllAsReadForEmail(email);
    }

    private NotificationDto toDto(Notification notification) {
        return new NotificationDto(
                notification.getId(),
                notification.getType(),
                notification.getMessage(),
                notification.getPostId(),
                notification.isRead(),
                notification.getCreatedAt()
        );
    }
}
