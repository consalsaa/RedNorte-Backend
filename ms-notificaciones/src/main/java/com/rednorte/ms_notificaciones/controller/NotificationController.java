package com.rednorte.ms_notificaciones.controller;

import com.rednorte.ms_notificaciones.entity.Notification;
import com.rednorte.ms_notificaciones.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notificaciones")
public class NotificationController {

    @Autowired
    private NotificationRepository notificationRepository;

    /**
     * Obtiene todas las notificaciones pertenecientes al RUT de un paciente determinado, ordenadas por fecha descendente.
     */
    @GetMapping("/paciente/{rut}")
    public ResponseEntity<List<Notification>> obtenerPorPaciente(@PathVariable String rut) {
        return ResponseEntity.ok(notificationRepository.findByRutPacienteOrderByFechaCreacionDesc(rut));
    }

    /**
     * Marca una notificación específica como leída.
     */
    @PutMapping("/{id}/leer")
    public ResponseEntity<Notification> marcarComoLeida(@PathVariable Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Notificación con ID " + id + " no encontrada"));
        notification.setLeido(true);
        return ResponseEntity.ok(notificationRepository.save(notification));
    }
}
