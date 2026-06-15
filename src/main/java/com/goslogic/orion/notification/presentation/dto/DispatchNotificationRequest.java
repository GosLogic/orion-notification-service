package com.goslogic.orion.notification.presentation.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.goslogic.orion.notification.domain.model.Channel;
import com.goslogic.orion.notification.domain.model.Notification;
import com.goslogic.orion.notification.domain.model.NotificationType;
import jakarta.validation.constraints.NotBlank;

public record DispatchNotificationRequest(
        @NotBlank @JsonProperty("user_id") String userId,
        String type,
        String channel,
        String title,
        String message,
        @JsonProperty("source_event") String sourceEvent,
        @JsonProperty("reference_id") String referenceId
) {}
