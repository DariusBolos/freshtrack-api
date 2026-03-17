package com.freshtrack.api.notification.dto;

import com.freshtrack.api.enums.NotificationType;

public record NotificationResponse(
        Long id,
        String title,
        String message,
        String timestamp,
        Boolean read,
        NotificationType type,
        String inviterName,
        String inviterEmail,
        String familyName,
        String inviteId,
        String productId,
        String productName
) {}

