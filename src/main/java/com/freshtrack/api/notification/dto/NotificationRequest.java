package com.freshtrack.api.notification.dto;

import com.freshtrack.api.enums.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record NotificationRequest(
        @NotBlank(message = "Title is required") String title,
        @NotBlank(message = "Message is required") String message,
        @NotBlank(message = "Timestamp is required") String timestamp,
        @NotNull(message = "Type is required") NotificationType type,
        String inviterName,
        String inviterEmail,
        String familyName,
        String inviteId,
        String productId,
        String productName
) {}

