package com.goslogic.orion.notification.bdd;

import com.goslogic.orion.notification.application.NotificationApplicationService;
import com.goslogic.orion.notification.application.NotificationApplicationService.DispatchCommand;
import com.goslogic.orion.notification.application.exception.ResourceNotFoundException;
import com.goslogic.orion.notification.domain.model.Channel;
import com.goslogic.orion.notification.domain.model.Notification;
import com.goslogic.orion.notification.domain.model.NotificationType;
import com.goslogic.orion.notification.domain.repository.NotificationRepository;
import com.goslogic.orion.notification.infrastructure.email.EmailAdapter;
import com.goslogic.orion.notification.infrastructure.template.TemplateManager;
import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Dado;
import io.cucumber.java.es.Entonces;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Step Definitions BDD (Cucumber / Gherkin en español) del Notification Service.
 * <p>
 * Estas pruebas garantizan la <strong>Trazabilidad de alertas críticas y el
 * aislamiento multi-tenant en la comunicación</strong>.
 * <p>
 * El {@link EmailAdapter} simula la inyección del canal de correo (equivalente a SendGrid)
 * sin acoplar los escenarios a un proveedor externo real.
 */
public class NotificationStepDefinitions {

    private final NotificationRepository notificationRepository = mock(NotificationRepository.class);
    private final TemplateManager templateManager = mock(TemplateManager.class);
    /** Simula la inyección del adaptador de correo (EmailAdapter / SendGrid). */
    private final EmailAdapter emailAdapter = mock(EmailAdapter.class);

    private final NotificationApplicationService notificationService =
            new NotificationApplicationService(notificationRepository, templateManager, emailAdapter);

    private DispatchCommand pendingCommand;
    private Notification dispatched;
    private List<Notification> historial;
    private String tenantActual;
    private Long notificacionAjenaId;
    private String tipoRaw;

    // -------------------------------------------------------------------------
    // Feature: Despacho y Envío de Notificaciones
    // -------------------------------------------------------------------------

    @Dado("una solicitud de alarma con título y mensaje")
    public void dadaSolicitudAlarmaConContenido() {
        pendingCommand = new DispatchCommand(
                "driver-demo",
                "tenant-demo",
                "ALARM",
                "EMAIL",
                "Alerta de pánico",
                "El conductor activó el botón de pánico.",
                "orion.incidents.panic",
                "panic-001"
        );
        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> {
            Notification n = inv.getArgument(0);
            n.setId(1L);
            return n;
        });
    }

    @Cuando("el sistema procesa el despacho")
    public void cuandoElSistemaProcesaElDespacho() {
        dispatched = notificationService.dispatch(pendingCommand);
    }

    @Entonces("se persiste la notificación y se envía el correo electrónico al conductor")
    public void entoncesSePersisteYSeEnviaEmail() {
        assertThat(dispatched).isNotNull();
        assertThat(dispatched.getType()).isEqualTo(NotificationType.ALARM);
        verify(notificationRepository).save(any(Notification.class));
        verify(emailAdapter).send(
                eq("driver-demo@orion-demo.local"),
                eq("Alerta de pánico"),
                eq("El conductor activó el botón de pánico."));
    }

    @Dado("una solicitud sin título ni mensaje")
    public void dadaSolicitudSinContenido() {
        pendingCommand = new DispatchCommand(
                "driver-demo", "tenant-demo", "WARNING", "EMAIL",
                null, null, null, null);
        when(templateManager.render(NotificationType.WARNING, null))
                .thenReturn(new TemplateManager.RenderedTemplate("Advertencia", "Mensaje generado"));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> {
            Notification n = inv.getArgument(0);
            n.setId(2L);
            return n;
        });
    }

    @Cuando("el servicio la procesa")
    public void cuandoElServicioLaProcesa() {
        dispatched = notificationService.dispatch(pendingCommand);
    }

    @Entonces("utiliza el TemplateManager para generar el contenido dinámico correspondiente al tipo de alerta")
    public void entoncesUsaTemplateManager() {
        verify(templateManager).render(NotificationType.WARNING, null);
        assertThat(dispatched.getTitle()).isEqualTo("Advertencia");
        assertThat(dispatched.getMessage()).isEqualTo("Mensaje generado");
    }

    @Dado("un tipo de notificación enviado en minúsculas como {string}")
    public void dadoTipoEnMinusculas(String tipo) {
        this.tipoRaw = tipo;
    }

    @Cuando("el sistema lo interpreta")
    public void cuandoElSistemaLoInterpreta() {
        // La normalización se valida en el Entonces mediante parseType
    }

    @Entonces("normaliza el casing correctamente al enum esperado")
    public void entoncesNormalizaCasing() {
        assertThat(NotificationApplicationService.parseType(tipoRaw))
                .isEqualTo(NotificationType.ALARM);
    }

    // -------------------------------------------------------------------------
    // Feature: Historial de Notificaciones por Tenant
    // -------------------------------------------------------------------------

    @Dado("un tenant autenticado")
    public void dadoUnTenantAutenticado() {
        tenantActual = "tenant-demo";
        Notification n = new Notification("driver-demo", tenantActual, "T", "M",
                NotificationType.INFO, Channel.EMAIL);
        when(notificationRepository.findByTenantExternalIdOrderBySentAtDesc(tenantActual))
                .thenReturn(List.of(n));
    }

    @Cuando("solicita su historial")
    public void cuandoSolicitaSuHistorial() {
        historial = notificationService.listByTenant(tenantActual);
    }

    @Entonces("el sistema devuelve la lista de notificaciones ordenadas de forma descendente")
    public void entoncesDevuelveHistorialDescendente() {
        assertThat(historial).isNotEmpty();
        verify(notificationRepository)
                .findByTenantExternalIdOrderBySentAtDesc(tenantActual);
    }

    @Dado("un usuario del tenant A")
    public void dadoUsuarioTenantA() {
        tenantActual = "tenant-A";
        notificacionAjenaId = 99L;
        Notification ajena = new Notification("user-b", "tenant-B", "Secreto", "Msg",
                NotificationType.ALARM, Channel.EMAIL);
        ajena.setId(notificacionAjenaId);
        when(notificationRepository.findById(notificacionAjenaId))
                .thenReturn(Optional.of(ajena));
    }

    @Cuando("intenta consultar el detalle de una notificación del tenant B")
    public void cuandoConsultaNotificacionDeOtroTenant() {
        // La verificación del aislamiento multi-tenant ocurre en el Entonces
    }

    @Entonces("el sistema bloquea el acceso devolviendo un error {int}")
    public void entoncesBloqueaConError(Integer codigoHttp) {
        assertThat(codigoHttp).isEqualTo(404);
        assertThatThrownBy(() -> notificationService.findById(notificacionAjenaId, tenantActual))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
