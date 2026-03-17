package com.freshtrack.api.notification.service;

import com.freshtrack.api.notification.Notification;

import java.util.List;

public interface INotificationService {
    Notification createNotification(Notification notification);
    Notification getNotificationById(Long id);
    List<Notification> getAllNotificationsByUser(Long userId);
    Notification updateNotification(Long id, Notification notification);
    void markAsRead(Long[] ids);
    void deleteNotification(Long id);
}

