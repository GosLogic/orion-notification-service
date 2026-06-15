package com.goslogic.orion.notification.infrastructure.email;

public interface EmailAdapter {

    void send(String to, String subject, String body);
}
