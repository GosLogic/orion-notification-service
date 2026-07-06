package com.goslogic.orion.notification.infrastructure.messaging;

import com.goslogic.orion.notification.application.NotificationApplicationService;
import com.goslogic.orion.notification.application.NotificationApplicationService.DispatchCommand;
import com.goslogic.orion.notification.infrastructure.config.JmsConfig;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Consume alertas de pánico del topic JMS y despacha notificación al gestor de flota (demo iter-1).
 */
@Component
public class JmsIncidentPanicListener {

    /** Destinatario demo iter-1 — gestor del tenant (IAM seeder). */
    private static final String DEMO_FLEET_MANAGER = "manager-demo";

    private final NotificationApplicationService notificationService;

    public JmsIncidentPanicListener(NotificationApplicationService notificationService) {
        this.notificationService = notificationService;
    }

    @JmsListener(destination = JmsConfig.TOPIC_INCIDENT_PANIC)
    public void onPanic(Map<String, Object> payload) {
        String externalId = stringVal(payload.get("external_id"));
        String tenantId = stringVal(payload.get("tenant_id"));
        String driverId = stringVal(payload.get("driver_id"));

        String message = String.format(
                "El conductor %s activó el botón de pánico (incidente %s).",
                driverId,
                externalId
        );
        notificationService.dispatch(new DispatchCommand(
                DEMO_FLEET_MANAGER,
                tenantId,
                "ALARM",
                "EMAIL",
                "Alerta de pánico",
                message,
                JmsConfig.TOPIC_INCIDENT_PANIC,
                externalId
        ));
    }

    private static String stringVal(Object value) {
        return value != null ? value.toString() : "";
    }
}
