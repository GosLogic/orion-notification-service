package com.goslogic.orion.notification.application;

import com.goslogic.orion.notification.application.exception.ResourceNotFoundException;
import com.goslogic.orion.notification.domain.model.Channel;
import com.goslogic.orion.notification.domain.model.Notification;
import com.goslogic.orion.notification.domain.model.NotificationType;
import com.goslogic.orion.notification.domain.repository.NotificationRepository;
import com.goslogic.orion.notification.infrastructure.email.EmailAdapter;
import com.goslogic.orion.notification.infrastructure.template.TemplateManager;
import com.goslogic.orion.notification.infrastructure.template.TemplateManager.RenderedTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class NotificationApplicationService {

    private final NotificationRepository notificationRepository;
    private final TemplateManager templateManager;
    private final EmailAdapter emailAdapter;

    public NotificationApplicationService(NotificationRepository notificationRepository,
                                          TemplateManager templateManager,
                                          EmailAdapter emailAdapter) {
        this.notificationRepository = notificationRepository;
        this.templateManager = templateManager;
        this.emailAdapter = emailAdapter;
    }

    /**
     * Despacha una notificación: renderiza plantilla, persiste historial y envía por el canal configurado.
     */
    public Notification dispatch(DispatchCommand cmd) {
        NotificationType type = parseType(cmd.type());
        Channel channel = parseChannel(cmd.channel());

        String title = cmd.title();
        String message = cmd.message();

        if (title == null || title.isBlank() || message == null || message.isBlank()) {
            RenderedTemplate rendered = templateManager.render(type, message);
            title = (title != null && !title.isBlank()) ? title : rendered.title();
            message = (message != null && !message.isBlank()) ? message : rendered.message();
        }

        Notification notification = new Notification(
                cmd.userExternalId(),
                cmd.tenantExternalId(),
                title,
                message,
                type,
                channel
        );
        notificationRepository.save(notification);

        if (channel == Channel.EMAIL) {
            String recipient = resolveEmailRecipient(cmd.userExternalId());
            emailAdapter.send(recipient, title, message);
        }

        return notification;
    }

    @Transactional(readOnly = true)
    public List<Notification> listByTenant(String tenantExternalId) {
        return notificationRepository.findByTenantExternalIdOrderBySentAtDesc(tenantExternalId);
    }

    @Transactional(readOnly = true)
    public Notification findById(Long id, String tenantExternalId) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Notificación no encontrada: " + id));
        if (!notification.getTenantExternalId().equals(tenantExternalId)) {
            throw new ResourceNotFoundException("Notificación no encontrada: " + id);
        }
        return notification;
    }

    static NotificationType parseType(String raw) {
        if (raw == null || raw.isBlank()) {
            return NotificationType.INFO;
        }
        return NotificationType.valueOf(raw.trim().toUpperCase().replace("-", "_"));
    }

    static Channel parseChannel(String raw) {
        if (raw == null || raw.isBlank()) {
            return Channel.EMAIL;
        }
        return Channel.valueOf(raw.trim().toUpperCase().replace("-", "_"));
    }

    /**
     * Resolución simplificada: en iter 1 el destinatario se deriva del user_external_id.
     * Iter 2: consultar IAM por email real del usuario.
     */
    private String resolveEmailRecipient(String userExternalId) {
        return userExternalId + "@orion-demo.local";
    }

    public record DispatchCommand(
            String userExternalId,
            String tenantExternalId,
            String type,
            String channel,
            String title,
            String message,
            String sourceEvent,
            String referenceId
    ) {}
}
