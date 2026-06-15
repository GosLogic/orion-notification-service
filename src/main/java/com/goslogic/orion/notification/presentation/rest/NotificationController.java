package com.goslogic.orion.notification.presentation.rest;

import com.goslogic.orion.notification.application.NotificationApplicationService;
import com.goslogic.orion.notification.application.NotificationApplicationService.DispatchCommand;
import com.goslogic.orion.notification.domain.model.Notification;
import com.goslogic.orion.notification.presentation.dto.DispatchNotificationRequest;
import com.goslogic.orion.notification.presentation.dto.NotificationResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/notification")
@Tag(name = "Notifications", description = "Despacho de alertas y historial de notificaciones")
public class NotificationController {

    private final NotificationApplicationService notificationService;

    public NotificationController(NotificationApplicationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping("/dispatch")
    @Operation(summary = "Despachar notificación (endpoint interno REST)")
    public ResponseEntity<NotificationResponse> dispatch(
            @Valid @RequestBody DispatchNotificationRequest req,
            @RequestHeader("X-Tenant-Id") String tenantExternalId) {
        DispatchCommand cmd = new DispatchCommand(
                req.userId(),
                tenantExternalId,
                req.type(),
                req.channel(),
                req.title(),
                req.message(),
                req.sourceEvent(),
                req.referenceId()
        );
        Notification notification = notificationService.dispatch(cmd);
        return ResponseEntity.ok(NotificationResponse.sent(notification));
    }

    @GetMapping
    @Operation(summary = "Listar historial de notificaciones del tenant (back-office)")
    public ResponseEntity<List<NotificationResponse>> listByTenant(
            @RequestHeader("X-Tenant-Id") String tenantExternalId) {
        List<NotificationResponse> responses = notificationService.listByTenant(tenantExternalId)
                .stream()
                .map(NotificationResponse::from)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener notificación por ID")
    public ResponseEntity<NotificationResponse> findById(
            @PathVariable Long id,
            @RequestHeader("X-Tenant-Id") String tenantExternalId) {
        return ResponseEntity.ok(NotificationResponse.from(
                notificationService.findById(id, tenantExternalId)));
    }
}
