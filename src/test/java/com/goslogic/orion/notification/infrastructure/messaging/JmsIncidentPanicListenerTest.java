package com.goslogic.orion.notification.infrastructure.messaging;

import com.goslogic.orion.notification.application.NotificationApplicationService;
import com.goslogic.orion.notification.application.NotificationApplicationService.DispatchCommand;
import com.goslogic.orion.notification.infrastructure.config.JmsConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class JmsIncidentPanicListenerTest {

    @Mock NotificationApplicationService notificationService;

    JmsIncidentPanicListener listener;

    @BeforeEach
    void setUp() {
        listener = new JmsIncidentPanicListener(notificationService);
    }

    @Test
    void onPanic_despacha_notificacion_alarm_al_gestor() {
        Map<String, Object> payload = Map.of(
                "external_id", "panic-001",
                "type", "OTHER",
                "tenant_id", "tenant-demo",
                "driver_id", "driver-demo",
                "is_panic", true
        );

        listener.onPanic(payload);

        ArgumentCaptor<DispatchCommand> captor = ArgumentCaptor.forClass(DispatchCommand.class);
        verify(notificationService).dispatch(captor.capture());

        DispatchCommand cmd = captor.getValue();
        assertThat(cmd.userExternalId()).isEqualTo("manager-demo");
        assertThat(cmd.tenantExternalId()).isEqualTo("tenant-demo");
        assertThat(cmd.type()).isEqualTo("ALARM");
        assertThat(cmd.channel()).isEqualTo("EMAIL");
        assertThat(cmd.title()).isEqualTo("Alerta de pánico");
        assertThat(cmd.message()).contains("driver-demo");
        assertThat(cmd.message()).contains("panic-001");
        assertThat(cmd.sourceEvent()).isEqualTo(JmsConfig.TOPIC_INCIDENT_PANIC);
        assertThat(cmd.referenceId()).isEqualTo("panic-001");
    }
}
