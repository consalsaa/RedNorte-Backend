package com.rednorte.ms_notificaciones.service;

import com.rednorte.ms_notificaciones.config.RabbitMQConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class NotificationListener {

    private static final Logger log = LoggerFactory.getLogger(NotificationListener.class);

    @RabbitListener(queues = RabbitMQConfig.QUEUE)
    public void receiveNotification(Map<String, Object> payload) {
        log.info("Recibido evento de reasignación desde RabbitMQ: {}", payload);
        
        Object atencionId = payload.get("atencionId");
        Object mensaje = payload.get("mensaje");
        
        log.info("--------------------------------------------------------------------------------");
        log.info("SIMULACIÓN DE NOTIFICACIÓN: Enviando alerta al paciente por atención ID: {}", atencionId);
        log.info("MENSAJE: {}", mensaje);
        log.info("Notificación enviada de forma exitosa y registrada en el log del sistema.");
        log.info("--------------------------------------------------------------------------------");
    }
}
