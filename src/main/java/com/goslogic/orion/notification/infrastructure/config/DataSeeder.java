package com.goslogic.orion.notification.infrastructure.config;

import com.goslogic.orion.notification.domain.model.Channel;
import com.goslogic.orion.notification.domain.model.Notification;
import com.goslogic.orion.notification.domain.model.NotificationType;
import com.goslogic.orion.notification.domain.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Datos demo alineados con el ecosistema Orion (tenant-demo, driver-demo).
 */
@Component
@Profile("!test")
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final NotificationRepository notificationRepository;

    public DataSeeder(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (notificationRepository.countByTenantExternalId("tenant-demo") > 0) {
            log.info("[DataSeeder] Datos demo ya existentes — omitiendo seed");
            return;
        }

        Notification demo = new Notification(
                "driver-demo",
                "tenant-demo",
                "Bienvenido",
                "Bienvenido al sistema Orion. Esta es una notificación demo.",
                NotificationType.INFO,
                Channel.EMAIL
        );
        notificationRepository.save(demo);

        log.info("[DataSeeder] Notificación demo creada para driver-demo (tenant-demo)");
    }
}
