package com.trendythread.app.controllers;

import com.trendythread.app.dto.NotificationDto;
import com.trendythread.app.services.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@Tag(
        name = "Notifications",
        description = "In-app notifications for comments, replies, and new followers. Requires authentication."
)
@RestController
@RequestMapping("/api/v1/notifications")
@Slf4j
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @Operation(summary = "List My Notifications", security = @SecurityRequirement(name = "Bearer"))
    @GetMapping
    public ResponseEntity<List<NotificationDto>> getMyNotifications(
            @RequestParam(value = "limit", defaultValue = "20") int limit,
            Principal principal) {
        log.info("GET /api/v1/notifications - request received: user={}, limit={}", principal.getName(), limit);
        return ResponseEntity.ok(notificationService.getMyNotifications(principal.getName(), limit));
    }

    @Operation(summary = "Unread Notification Count", security = @SecurityRequirement(name = "Bearer"))
    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(Principal principal) {
        long count = notificationService.getUnreadCount(principal.getName());
        return ResponseEntity.ok(Map.of("unreadCount", count));
    }

    @Operation(summary = "Mark Notification Read", security = @SecurityRequirement(name = "Bearer"))
    @PutMapping("/{notificationId}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Integer notificationId, Principal principal) {
        notificationService.markAsRead(notificationId, principal.getName());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Mark All Notifications Read", security = @SecurityRequirement(name = "Bearer"))
    @PutMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(Principal principal) {
        notificationService.markAllAsRead(principal.getName());
        return ResponseEntity.ok().build();
    }
}
