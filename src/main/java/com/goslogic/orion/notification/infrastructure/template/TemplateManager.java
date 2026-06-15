package com.goslogic.orion.notification.infrastructure.template;

import com.goslogic.orion.notification.domain.model.NotificationType;
import org.springframework.stereotype.Component;

@Component
public class TemplateManager {

    public record RenderedTemplate(String title, String message) {}

    /**
     * Renderiza asunto y cuerpo según el tipo de notificación.
     * Si el mensaje del evento viene vacío, usa un texto por defecto.
     */
    public RenderedTemplate render(NotificationType type, String eventMessage) {
        String body = (eventMessage != null && !eventMessage.isBlank())
                ? eventMessage
                : defaultBody(type);

        return switch (type) {
            case ALARM -> new RenderedTemplate("Alerta crítica", body);
            case WARNING -> new RenderedTemplate("Advertencia", body);
            case SUCCESS -> new RenderedTemplate("Operación exitosa", body);
            case INFO -> new RenderedTemplate("Información", body);
        };
    }

    private String defaultBody(NotificationType type) {
        return switch (type) {
            case ALARM -> "Se ha registrado una alerta crítica en el sistema Orion.";
            case WARNING -> "Se ha registrado una advertencia en el sistema Orion.";
            case SUCCESS -> "Operación completada correctamente.";
            case INFO -> "Notificación informativa del sistema Orion.";
        };
    }
}
