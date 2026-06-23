package com.rednorte.ms_notificaciones.service;

import com.rednorte.ms_notificaciones.entity.Notification;
import com.rednorte.ms_notificaciones.repository.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationListenerTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationListener notificationListener;

    @Test
    void testReceiveNotificationSuccess() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("atencionId", 123L);
        payload.put("mensaje", "Test Message");
        payload.put("rutPaciente", "12345678-9");
        payload.put("tipo", "REASIGNACION");

        when(notificationRepository.save(any(Notification.class))).thenAnswer(i -> i.getArgument(0));

        assertDoesNotThrow(() -> {
            notificationListener.receiveNotification(payload);
        });

        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    void testReceiveNotificationEmptyPayload() {
        Map<String, Object> payload = new HashMap<>();

        when(notificationRepository.save(any(Notification.class))).thenAnswer(i -> i.getArgument(0));

        assertDoesNotThrow(() -> {
            notificationListener.receiveNotification(payload);
        });

        verify(notificationRepository, times(1)).save(any(Notification.class));
    }
}
