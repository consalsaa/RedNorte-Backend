package com.rednorte.ms_notificaciones.controller;

import com.rednorte.ms_notificaciones.entity.Notification;
import com.rednorte.ms_notificaciones.repository.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationControllerTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationController notificationController;

    @Test
    void testObtenerPorPaciente() {
        String rut = "12345678-9";
        List<Notification> mockList = List.of(
            new Notification(1L, rut, "Mensaje 1", "REASIGNACION"),
            new Notification(2L, rut, "Mensaje 2", "REASIGNACION")
        );

        when(notificationRepository.findByRutPacienteOrderByFechaCreacionDesc(rut)).thenReturn(mockList);

        ResponseEntity<List<Notification>> response = notificationController.obtenerPorPaciente(rut);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertEquals(2, response.getBody().size());
        verify(notificationRepository, times(1)).findByRutPacienteOrderByFechaCreacionDesc(rut);
    }

    @Test
    void testMarcarComoLeidaExito() {
        Long notificationId = 1L;
        Notification mockNotification = new Notification(10L, "12345678-9", "Mensaje", "REASIGNACION");
        mockNotification.setId(notificationId);
        assertFalse(mockNotification.isLeido());

        when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(mockNotification));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(i -> i.getArgument(0));

        ResponseEntity<Notification> response = notificationController.marcarComoLeida(notificationId);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().isLeido());
        verify(notificationRepository, times(1)).findById(notificationId);
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    void testMarcarComoLeidaNoEncontrada() {
        Long notificationId = 999L;
        when(notificationRepository.findById(notificationId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            notificationController.marcarComoLeida(notificationId);
        });

        verify(notificationRepository, times(1)).findById(notificationId);
        verify(notificationRepository, never()).save(any(Notification.class));
    }
}
