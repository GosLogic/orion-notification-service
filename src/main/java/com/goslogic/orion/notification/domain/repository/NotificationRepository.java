package com.goslogic.orion.notification.domain.repository;

import com.goslogic.orion.notification.domain.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByTenantExternalIdOrderBySentAtDesc(String tenantExternalId);

    List<Notification> findByUserExternalIdAndTenantExternalIdOrderBySentAtDesc(
            String userExternalId, String tenantExternalId);

    long countByTenantExternalId(String tenantExternalId);
}
