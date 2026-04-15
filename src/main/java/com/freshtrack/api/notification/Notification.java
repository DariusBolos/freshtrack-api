package com.freshtrack.api.notification;

import com.freshtrack.api.enums.NotificationType;
import com.freshtrack.api.user.User;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "notifications")
public class Notification {
    @Id
    @GeneratedValue
    private Long id;
    private String title;
    private String message;
    private String timestamp;
    private Boolean read;
    private String inviterName;
    private String inviterEmail;
    private String familyName;
    private String inviteId;
    private String productId;
    private String productName;

    @Enumerated(EnumType.STRING)
    private NotificationType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public Notification() {}

    public Notification(
            Long id,
            String title,
            String message,
            String timestamp,
            Boolean read,
            String inviterName,
            String inviterEmail,
            String familyName
    ) {
        this.id = id;
        this.title = title;
        this.message = message;
        this.timestamp = timestamp;
        this.read = read;
        this.inviterName = inviterName;
        this.inviterEmail = inviterEmail;
        this.familyName = familyName;
    }
}
