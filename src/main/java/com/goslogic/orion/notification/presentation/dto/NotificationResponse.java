package com.goslogic.orion.notification.presentation.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.goslogic.orion.notification.domain.model.Channel;
import com.goslogic.orion.notification.domain.model.Notification;
import com.goslogic.orion.notification.domain.model.NotificationType;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record NotificationResponse(
        String id,
        String status,
        @JsonProperty("user_id") String userExternalId,
        String title,
        String message,
        NotificationType type,
        Channel channel,
        @JsonProperty("is_read") Boolean read,
        @JsonProperty("sent_at") String sentAt
) {

    public static NotificationResponse sent(Notification notification) {
        return new NotificationResponse(
                String.valueOf(notification.getId()),
                "SENT",
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }

    public static NotificationResponse from(Notification notification) {
        return new NotificationResponse(
                String.valueOf(notification.getId()),
                null,
                notification.getUserExternalId(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getType(),
                notification.getChannel(),
                notification.isRead(),
                notification.getSentAt() != null ? notification.getSentAt().toString() : null
        );
    }
}
