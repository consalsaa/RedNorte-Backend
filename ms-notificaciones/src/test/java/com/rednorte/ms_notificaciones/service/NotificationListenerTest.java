package com.rednorte.ms_notificaciones.service;

import org.junit.jupiter.api.Test;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class NotificationListenerTest {

    private final NotificationListener notificationListener = new NotificationListener();

    @Test
    void testReceiveNotificationSuccess() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("atencionId", 123L);
        payload.put("mensaje", "Test Message");

        assertDoesNotThrow(() -> {
            notificationListener.receiveNotification(payload);
        });
    }

    @Test
    void testReceiveNotificationEmptyPayload() {
        Map<String, Object> payload = new HashMap<>();

        assertDoesNotThrow(() -> {
            notificationListener.receiveNotification(payload);
        });
    }
}
