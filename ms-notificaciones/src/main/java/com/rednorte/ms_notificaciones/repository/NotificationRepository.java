package com.rednorte.ms_notificaciones.repository;

import com.rednorte.ms_notificaciones.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByRutPacienteOrderByFechaCreacionDesc(String rutPaciente);
}
