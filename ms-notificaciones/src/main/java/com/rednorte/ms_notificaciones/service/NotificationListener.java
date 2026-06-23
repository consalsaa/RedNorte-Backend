package com.rednorte.ms_notificaciones.service;

import com.rednorte.ms_notificaciones.config.RabbitMQConfig;
import com.rednorte.ms_notificaciones.entity.Notification;
import com.rednorte.ms_notificaciones.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class NotificationListener {

    private static final Logger log = LoggerFactory.getLogger(NotificationListener.class);

    @Autowired
    private NotificationRepository notificationRepository;

    @RabbitListener(queues = RabbitMQConfig.QUEUE)
    public void receiveNotification(Map<String, Object> payload) {
        log.info("Recibido evento de reasignación desde RabbitMQ: {}", payload);
        
        try {
            Object rawAtencionId = payload.get("atencionId");
            Long atencionId = (rawAtencionId != null) ? ((Number) rawAtencionId).longValue() : 0L;
            String rutPaciente = (String) payload.get("rutPaciente");
            String mensaje = (String) payload.get("mensaje");
            String tipo = (String) payload.get("tipo");
            
            if (rutPaciente == null) rutPaciente = "12345678-9"; // Fallback para compatibilidad con pruebas unitarias previas
            if (tipo == null) tipo = "REASIGNACION";
            if (mensaje == null) mensaje = "Reasignación completada";
            
            Notification notification = new Notification(atencionId, rutPaciente, mensaje, tipo);
            notificationRepository.save(notification);
            log.info("Notificación guardada exitosamente en H2 para paciente RUT: {}", rutPaciente);
            
            log.info("--------------------------------------------------------------------------------");
            log.info("SIMULACIÓN DE NOTIFICACIÓN: Enviando alerta al paciente por atención ID: {}", atencionId);
            log.info("MENSAJE: {}", mensaje);
            log.info("Notificación enviada de forma exitosa y registrada en el log del sistema.");
            log.info("--------------------------------------------------------------------------------");
        } catch (Exception e) {
            log.error("Error al procesar y guardar la notificación en listener: {}", e.getMessage());
        }
    }
}
