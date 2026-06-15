package com.goslogic.orion.notification.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "notification_history")
@Getter
@Setter
@NoArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_external_id", nullable = false, length = 100)
    private String userExternalId;

    @Column(name = "tenant_external_id", nullable = false, length = 100)
    private String tenantExternalId;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NotificationType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Channel channel = Channel.EMAIL;

    @Column(name = "is_read", nullable = false)
    private boolean read = false;

    @Column(name = "sent_at", nullable = false, updatable = false)
    private LocalDateTime sentAt;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @PrePersist
    void prePersist() {
        if (sentAt == null) {
            sentAt = LocalDateTime.now();
        }
    }

    public Notification(String userExternalId,
                        String tenantExternalId,
                        String title,
                        String message,
                        NotificationType type,
                        Channel channel) {
        this.userExternalId = userExternalId;
        this.tenantExternalId = tenantExternalId;
        this.title = title;
        this.message = message;
        this.type = type;
        this.channel = channel != null ? channel : Channel.EMAIL;
    }
}
