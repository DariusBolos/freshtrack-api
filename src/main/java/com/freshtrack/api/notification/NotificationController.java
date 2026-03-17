package com.freshtrack.api.notification;

import com.freshtrack.api.notification.dto.NotificationRequest;
import com.freshtrack.api.notification.dto.NotificationResponse;
import com.freshtrack.api.notification.service.INotificationService;
import com.freshtrack.api.user.User;
import com.freshtrack.api.user.service.IUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final INotificationService notificationService;
    private final IUserService userService;

    @PostMapping
    public ResponseEntity<NotificationResponse> createNotification(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody NotificationRequest request) {
        User user = userService.getUserByEmail(authHeader.substring(7));

        Notification notification = new Notification();
        notification.setTitle(request.title());
        notification.setMessage(request.message());
        notification.setTimestamp(request.timestamp());
        notification.setRead(false);
        notification.setType(request.type());
        notification.setInviterName(request.inviterName());
        notification.setInviterEmail(request.inviterEmail());
        notification.setFamilyName(request.familyName());
        notification.setInviteId(request.inviteId());
        notification.setProductId(request.productId());
        notification.setProductName(request.productName());
        notification.setUser(user);

        Notification saved = notificationService.createNotification(notification);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(saved));
    }

    @GetMapping("/{id}")
    public ResponseEntity<NotificationResponse> getNotification(@PathVariable Long id) {
        Notification notification = notificationService.getNotificationById(id);
        return ResponseEntity.ok(toResponse(notification));
    }

    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getAllNotifications(
            @RequestHeader("Authorization") String authHeader) {
        User user = userService.getUserByEmail(authHeader.substring(7));
        List<NotificationResponse> notifications = notificationService.getAllNotificationsByUser(user.getId())
                .stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(notifications);
    }

    @PutMapping("/{id}")
    public ResponseEntity<NotificationResponse> updateNotification(
            @PathVariable Long id,
            @Valid @RequestBody NotificationRequest request) {
        Notification notification = new Notification();
        notification.setTitle(request.title());
        notification.setMessage(request.message());
        notification.setTimestamp(request.timestamp());
        notification.setType(request.type());
        notification.setInviterName(request.inviterName());
        notification.setInviterEmail(request.inviterEmail());
        notification.setFamilyName(request.familyName());
        notification.setInviteId(request.inviteId());
        notification.setProductId(request.productId());
        notification.setProductName(request.productName());

        Notification updated = notificationService.updateNotification(id, notification);
        return ResponseEntity.ok(toResponse(updated));
    }

    @PutMapping("/markAsRead")
    public ResponseEntity<Void> markNotificationsAsRead(@RequestBody String[] notificationIds) {
        Long[] notificationIdsConverted = Arrays.stream(notificationIds)
                .map(Long::valueOf)
                .toArray(Long[]::new);
        notificationService.markAsRead(notificationIdsConverted);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long id) {
        notificationService.deleteNotification(id);
        return ResponseEntity.noContent().build();
    }

    private NotificationResponse toResponse(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getTimestamp(),
                notification.getRead(),
                notification.getType(),
                notification.getInviterName(),
                notification.getInviterEmail(),
                notification.getFamilyName(),
                notification.getInviteId(),
                notification.getProductId(),
                notification.getProductName()
        );
    }
}
