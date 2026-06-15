package com.goslogic.orion.notification.infrastructure.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Implementación stub de EmailAdapter que registra el envío en el log.
 * Sustituir por SendGridEmailAdapter cuando la API key esté disponible.
 */
@Component
public class LoggingEmailAdapter implements EmailAdapter {

    private static final Logger log = LoggerFactory.getLogger(LoggingEmailAdapter.class);

    @Override
    public void send(String to, String subject, String body) {
        log.info("[EMAIL] to={} subject={} body={}", to, subject, body);
    }
}
