package com.freshtrack.api.notification.service;

import com.freshtrack.api.notification.Notification;
import com.freshtrack.api.notification.NotificationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService implements INotificationService {
    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    public Notification createNotification(Notification notification) {
        return notificationRepository.save(notification);
    }

    @Override
    public Notification getNotificationById(Long id) {
        return notificationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found: " + id));
    }

    @Override
    public List<Notification> getAllNotificationsByUser(Long userId) {
        return notificationRepository.findAllByUserId(userId);
    }

    @Override
    public Notification updateNotification(Long id, Notification notification) {
        Notification existing = notificationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found: " + id));
        existing.setTitle(notification.getTitle());
        existing.setMessage(notification.getMessage());
        existing.setTimestamp(notification.getTimestamp());
        existing.setRead(notification.getRead());
        existing.setType(notification.getType());
        existing.setInviterName(notification.getInviterName());
        existing.setInviterEmail(notification.getInviterEmail());
        existing.setFamilyName(notification.getFamilyName());
        return notificationRepository.save(existing);
    }

    @Override
    public void markAsRead(Long[] ids) {
        List<Long> idList = List.of(ids);
        notificationRepository.markAsRead(idList);
    }

    @Override
    public void deleteNotification(Long id) {
        if (!notificationRepository.existsById(id)) {
            throw new IllegalArgumentException("Notification not found: " + id);
        }
        notificationRepository.deleteById(id);
    }
}

