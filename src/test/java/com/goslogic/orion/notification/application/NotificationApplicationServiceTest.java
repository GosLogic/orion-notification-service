package com.goslogic.orion.notification.application;

import com.goslogic.orion.notification.domain.model.Channel;
import com.goslogic.orion.notification.domain.model.Notification;
import com.goslogic.orion.notification.domain.model.NotificationType;
import com.goslogic.orion.notification.domain.repository.NotificationRepository;
import com.goslogic.orion.notification.infrastructure.email.EmailAdapter;
import com.goslogic.orion.notification.infrastructure.template.TemplateManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests de la capa de aplicación del Notification Service.
 * <p>
 * Estas pruebas garantizan la <strong>Trazabilidad de alertas críticas y el
 * aislamiento multi-tenant en la comunicación</strong>:
 * persistencia del historial, envío vía EmailAdapter, renderizado por
 * TemplateManager y normalización de tipos.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class NotificationApplicationServiceTest {

    @Mock NotificationRepository notificationRepository;
    @Mock TemplateManager templateManager;
    @Mock EmailAdapter emailAdapter;

    NotificationApplicationService service;

    @BeforeEach
    void setUp() {
        service = new NotificationApplicationService(
                notificationRepository, templateManager, emailAdapter);
        when(notificationRepository.save(any())).thenAnswer(inv -> {
            Notification n = inv.getArgument(0);
            n.setId(1L);
            return n;
        });
    }

    private NotificationApplicationService.DispatchCommand cmd() {
        return new NotificationApplicationService.DispatchCommand(
                "driver-demo",
                "tenant-demo",
                "ALARM",
                "EMAIL",
                "Alerta de pánico",
                "El conductor driver-demo activó el botón de pánico en ruta.",
                "orion.incidents.panic",
                "panic-001"
        );
    }

    @Test
    void dispatch_persiste_notificacion_y_envia_email() {
        Notification result = service.dispatch(cmd());

        assertThat(result.getUserExternalId()).isEqualTo("driver-demo");
        assertThat(result.getTenantExternalId()).isEqualTo("tenant-demo");
        assertThat(result.getType()).isEqualTo(NotificationType.ALARM);
        assertThat(result.getChannel()).isEqualTo(Channel.EMAIL);
        assertThat(result.getTitle()).isEqualTo("Alerta de pánico");
        assertThat(result.isRead()).isFalse();

        verify(notificationRepository).save(any(Notification.class));
        verify(emailAdapter).send(
                eq("driver-demo@orion-demo.local"),
                eq("Alerta de pánico"),
                eq("El conductor driver-demo activó el botón de pánico en ruta."));
        verify(templateManager, never()).render(any(), any());
    }

    @Test
    void dispatch_usa_template_si_title_y_message_vacios() {
        when(templateManager.render(NotificationType.WARNING, null))
                .thenReturn(new TemplateManager.RenderedTemplate("Advertencia", "Mensaje generado"));

        NotificationApplicationService.DispatchCommand cmd =
                new NotificationApplicationService.DispatchCommand(
                        "driver-demo", "tenant-demo", "WARNING", "EMAIL",
                        null, null, null, null);

        Notification result = service.dispatch(cmd);

        assertThat(result.getTitle()).isEqualTo("Advertencia");
        assertThat(result.getMessage()).isEqualTo("Mensaje generado");
        verify(templateManager).render(NotificationType.WARNING, null);
    }

    @Test
    void listByTenant_devuelve_historial() {
        Notification n = new Notification("driver-demo", "tenant-demo", "T", "M",
                NotificationType.INFO, Channel.EMAIL);
        when(notificationRepository.findByTenantExternalIdOrderBySentAtDesc("tenant-demo"))
                .thenReturn(List.of(n));

        List<Notification> result = service.listByTenant("tenant-demo");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUserExternalId()).isEqualTo("driver-demo");
    }

    @Test
    void parseType_normaliza_casing() {
        assertThat(NotificationApplicationService.parseType("alarm"))
                .isEqualTo(NotificationType.ALARM);
    }
}
